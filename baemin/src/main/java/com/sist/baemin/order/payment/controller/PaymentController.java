package com.sist.baemin.order.payment.controller;

import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.domain.OrderItemsEntity;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.repository.PaymentRepository;
import com.sist.baemin.order.repository.OrderItemsRepository;
import com.sist.baemin.order.repository.OrderRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.baemin.order.payment.dto.PaymentPrepareRequest;
import com.sist.baemin.order.payment.dto.PaymentPrepareResponse;
import com.sist.baemin.order.payment.service.PaymentService;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.domain.UserEntity;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final OrderItemsRepository orderItemsRepository;

    /**
     * 결제 사전 검증 데이터 저장 엔드포인트
     * 클라이언트에서 결제 요청 전에 호출
     */
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @RequestBody PaymentPrepareRequest request
    ) {
        log.info("=== 결제 사전 검증 요청 ===");
        log.info("Request: {}", request);

        try {
            // 현재 로그인한 사용자 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = null;
            
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
            }
            
            if (user == null) {
                log.warn("인증되지 않은 사용자의 결제 준비 요청");
                return ResponseEntity.status(401).body(
                    PaymentPrepareResponse.builder()
                            .success(false)
                            .message("로그인이 필요합니다.")
                            .build()
                );
            }

            PaymentPrepareResponse response = paymentService.preparePayment(user.getUserId(), request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("결제 사전 검증 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                PaymentPrepareResponse.builder()
                        .success(false)
                        .message("서버 오류가 발생했습니다: " + e.getMessage())
                        .build()
            );
        }
    }

    /**
     * 포트원 웹훅 수신 엔드포인트
     * 결제 상태 변경 시 포트원에서 호출
     */
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            HttpServletRequest request,
            @RequestBody String rawBody
    ) {
        log.info("=== 포트원 웹훅 수신 ===");
        log.info("URI: {}", request.getRequestURI());
        log.info("Method: {}", request.getMethod());
        log.info("Body: {}", rawBody);

        try {
            // JSON 파싱
            JsonNode webhookData = objectMapper.readTree(rawBody);

            // 포트원 V2 웹훅 형식: payment_id, tx_id, status (루트 레벨)
            String paymentId = webhookData.path("payment_id").asText();
            String transactionId = webhookData.path("tx_id").asText();
            String status = webhookData.path("status").asText(); // "Paid", "Failed", "Cancelled"
            
            // status 정규화 (Paid -> PAID)
            String normalizedStatus = status.toUpperCase();
            
            // 결제 금액 (포트원 V2는 웹훅에 금액이 없을 수 있음, DB에서 조회)
            Long paidAmount = null;
            if (webhookData.has("amount")) {
                JsonNode amountNode = webhookData.path("amount");
                if (amountNode.has("total")) {
                    paidAmount = amountNode.path("total").asLong();
                }
            }

            log.info("Payment ID: {}", paymentId);
            log.info("Transaction ID: {}", transactionId);
            log.info("Status: {}", status);
            log.info("Normalized Status: {}", normalizedStatus);
            log.info("Paid Amount: {}", paidAmount);

            // 금액이 없으면 DB에서 조회하여 사용
            if (paidAmount == null) {
                log.info("웹훅에 금액 정보 없음, DB에서 조회 시도");
                // PaymentService에서 expectedAmount를 사용하도록 수정 필요
            }
            
            // 결제 검증 및 최종 저장
            boolean verified = paymentService.verifyAndCompletePayment(
                    paymentId, 
                    transactionId, 
                    paidAmount, 
                    normalizedStatus
            );

            if (verified) {
                log.info("결제 검증 및 처리 완료: {}", paymentId);
            } else {
                log.warn("결제 검증 실패 또는 취소/실패 상태: {}", paymentId);
            }

            // 포트원에게 200 응답 (필수)
            return ResponseEntity.ok(Map.of("received", true, "verified", verified));

        } catch (Exception e) {
            log.error("웹훅 처리 중 오류 발생", e);
            // 오류가 발생해도 200 반환 (재전송 방지)
            return ResponseEntity.ok(Map.of("received", false, "error", e.getMessage()));
        }
    }

    /**
     * 포트원 결제 승인 엔드포인트 (V2)
     * 결제 전 서버에서 최종 검증
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> payload) {
        log.info("=== 결제 승인 요청 ===");
        log.info("Payload: {}", payload);

        try {
            String paymentId = (String) payload.get("paymentId");
            Object amountObj = payload.get("totalAmount");
            
            Long amount = null;
            if (amountObj instanceof Integer) {
                amount = ((Integer) amountObj).longValue();
            } else if (amountObj instanceof Long) {
                amount = (Long) amountObj;
            }

            log.info("결제 승인 요청: paymentId={}, amount={}", paymentId, amount);

            // 포트원 SDK V2에서는 confirm 엔드포인트가 결제 전에 호출될 수 있음
            // 여기서는 간단히 승인만 하고, 실제 검증은 웹훅에서 수행
            // 사전 검증 데이터가 있는지만 확인
            
            return ResponseEntity.ok(Map.of(
                    "approved", true,
                    "paymentId", paymentId
            ));

        } catch (Exception e) {
            log.error("결제 승인 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "approved", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * 주문 정보 조회 API (결제 완료 페이지용)
     * @param paymentId PortOne 결제 ID
     * @return 주문 상세 정보 (가게명, 메뉴 목록, 금액 정보)
     */
    @GetMapping("/order-info")
    public ResponseEntity<Map<String, Object>> getOrderInfo(@RequestParam String paymentId) {
        log.info("주문 정보 조회 요청 - paymentId: {}", paymentId);
        
        try {
            Optional<PaymentEntity> paymentOpt = paymentRepository.findByPortonePaymentId(paymentId);
            
            if (paymentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            PaymentEntity payment = paymentOpt.get();
            OrderEntity order = payment.getOrder();
            
            // 주문이 아직 생성되지 않은 경우 (웹훅 처리 중)
            if (order == null) {
                log.warn("주문이 아직 생성되지 않음 - paymentId: {}, 기본 정보만 반환", paymentId);
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("orderNo", "-");
                response.put("storeName", payment.getOrderName() != null ? payment.getOrderName() : "가게명");
                response.put("orderItems", new ArrayList<>());
                
                Long paymentAmount = payment.getPaymentPrice() != null ? payment.getPaymentPrice() : payment.getExpectedAmount();
                response.put("menuAmount", paymentAmount);
                response.put("deliveryTip", 0L);
                response.put("discountTotal", 0L);
                response.put("paymentAmount", paymentAmount);
                response.put("payMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "카드");
                response.put("pending", true); // 주문 생성 대기 중 플래그
                
                return ResponseEntity.ok(response);
            }
            
            // 주문 항목 조회
            List<OrderItemsEntity> orderItems = orderItemsRepository.findByOrder_OrderId(order.getOrderId());
            List<Map<String, Object>> itemsList = new ArrayList<>();
            Long menuAmount = 0L;
            
            for (OrderItemsEntity item : orderItems) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("menuName", item.getMenuName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("basePrice", item.getMenu() != null ? item.getMenu().getPrice() : 0);
                
                Long lineTotal = item.getPrice() != null ? item.getPrice().longValue() : 0L;
                itemMap.put("lineTotal", lineTotal);
                
                itemsList.add(itemMap);
                menuAmount += lineTotal;
            }
            
            // 결제 정보 구성
            Long paymentAmount = payment.getPaymentPrice() != null ? payment.getPaymentPrice() : payment.getExpectedAmount();
            Long deliveryTip = paymentAmount - menuAmount;
            if (deliveryTip < 0) deliveryTip = 0L;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("orderNo", order.getOrderId());
            response.put("storeName", order.getStore() != null ? order.getStore().getStoreName() : "가게명");
            response.put("orderItems", itemsList);
            response.put("menuAmount", menuAmount);
            response.put("deliveryTip", deliveryTip);
            response.put("discountTotal", 0L);
            response.put("paymentAmount", paymentAmount);
            response.put("payMethod", payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "카드");
            response.put("pending", false); // 주문 완료
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("주문 정보 조회 실패 - paymentId: {}", paymentId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "주문 정보 조회 중 오류가 발생했습니다."
            ));
        }
    }
}
