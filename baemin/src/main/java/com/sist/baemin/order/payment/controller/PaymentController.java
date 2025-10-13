package com.sist.baemin.order.payment.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final ObjectMapper objectMapper = new ObjectMapper();

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

            String transactionType = webhookData.path("type").asText();
            String paymentId = webhookData.path("data").path("paymentId").asText();
            String status = webhookData.path("data").path("status").asText();

            log.info("Transaction Type: {}", transactionType);
            log.info("Payment ID: {}", paymentId);
            log.info("Status: {}", status);

            // TODO: 실제 비즈니스 로직 구현
            // 1. paymentId로 주문 조회
            // 2. 결제 상태 업데이트
            // 3. 주문 상태 변경
            // 4. 재고 차감 등

            switch (status) {
                case "PAID":
                    log.info("결제 완료: {}", paymentId);
                    // 결제 완료 처리
                    break;
                case "CANCELLED":
                    log.info("결제 취소: {}", paymentId);
                    // 결제 취소 처리
                    break;
                case "FAILED":
                    log.info("결제 실패: {}", paymentId);
                    // 결제 실패 처리
                    break;
                default:
                    log.warn("알 수 없는 상태: {}", status);
            }

            // 포트원에게 200 응답 (필수)
            return ResponseEntity.ok(Map.of("received", true));

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
            Integer amount = (Integer) payload.get("totalAmount");

            // TODO: 실제 검증 로직
            // 1. 주문 금액 검증
            // 2. 재고 확인
            // 3. 중복 결제 체크

            log.info("결제 승인: paymentId={}, amount={}", paymentId, amount);

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
}
