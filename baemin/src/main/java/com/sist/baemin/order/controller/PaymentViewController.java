package com.sist.baemin.order.controller;

import com.sist.baemin.order.domain.OrderEntity;
import com.sist.baemin.order.domain.OrderItemsEntity;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.repository.PaymentRepository;
import com.sist.baemin.order.repository.OrderItemsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 결제 관련 뷰 컨트롤러
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class PaymentViewController {

    private final PaymentRepository paymentRepository;
    private final OrderItemsRepository orderItemsRepository;

    /**
     * 결제 완료 페이지 (/order/complete)
     * 포트원 결제 완료 후 리다이렉트되는 페이지
     */
    @GetMapping("/complete")
    public String orderComplete(
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            Model model
    ) {
        log.info("결제 완료 페이지 접근 - paymentId: {}, code: {}, message: {}", paymentId, code, message);
        
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        
        // 결제 성공 여부 확인 (code가 없으면 성공)
        boolean isSuccess = code == null || code.isEmpty();
        model.addAttribute("isSuccess", isSuccess);
        
        if (isSuccess && paymentId != null) {
            log.info("결제 성공 - paymentId: {}", paymentId);
            
            // 결제 정보 조회
            Optional<PaymentEntity> paymentOpt = paymentRepository.findByPortonePaymentId(paymentId);
            if (paymentOpt.isPresent()) {
                PaymentEntity payment = paymentOpt.get();
                OrderEntity order = payment.getOrder();
                
                if (order != null) {
                    // 주문 정보
                    model.addAttribute("orderNo", order.getOrderId());
                    model.addAttribute("storeName", order.getStore() != null ? order.getStore().getStoreName() : "가게명");
                    model.addAttribute("merchantUid", paymentId);
                    
                    // 주문 항목 조회 (Repository 사용)
                    List<OrderItemsEntity> orderItems = orderItemsRepository.findByOrder_OrderId(order.getOrderId());
                    List<Map<String, Object>> orderItemsList = new ArrayList<>();
                    Long menuAmount = 0L;
                    
                    if (orderItems != null && !orderItems.isEmpty()) {
                        for (OrderItemsEntity item : orderItems) {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("menuName", item.getMenuName());
                            itemMap.put("quantity", item.getQuantity());
                            itemMap.put("basePrice", item.getMenu() != null ? item.getMenu().getPrice() : 0);
                            
                            // 라인별 총액 (가격 * 수량)
                            Long lineTotal = item.getPrice() != null ? item.getPrice().longValue() : 0L;
                            itemMap.put("lineTotal", lineTotal);
                            
                            orderItemsList.add(itemMap);
                            menuAmount += lineTotal;
                        }
                    }
                    
                    model.addAttribute("orderItems", orderItemsList);
                    model.addAttribute("menuAmount", menuAmount);
                    
                    // 배달팁 (PaymentEntity에서 계산: 총액 - 메뉴금액)
                    Long paymentAmount = payment.getPaymentPrice() != null ? payment.getPaymentPrice() : 0L;
                    Long deliveryTip = paymentAmount - menuAmount;
                    if (deliveryTip < 0) deliveryTip = 0L;
                    model.addAttribute("deliveryTip", deliveryTip);
                    
                    // 할인 금액 (현재는 0으로 설정, 필요시 추가)
                    Long discountTotal = 0L;
                    model.addAttribute("discountTotal", discountTotal);
                    
                    // 최종 결제 금액
                    model.addAttribute("paymentAmount", paymentAmount);
                    
                    // 결제 방법
                    String payMethod = payment.getPaymentMethod();
                    model.addAttribute("payMethod", payMethod != null ? payMethod : "카드");
                    
                    log.info("결제 정보 조회 완료 - orderId: {}, menuAmount: {}, deliveryTip: {}, paymentAmount: {}", 
                            order.getOrderId(), menuAmount, deliveryTip, paymentAmount);
                } else {
                    log.warn("주문 정보를 찾을 수 없음 - paymentId: {}", paymentId);
                    setDefaultValues(model);
                }
            } else {
                log.warn("결제 정보를 찾을 수 없음 - paymentId: {}", paymentId);
                setDefaultValues(model);
            }
        } else {
            log.warn("결제 실패/취소 - paymentId: {}, code: {}, message: {}", paymentId, code, message);
            setDefaultValues(model);
        }
        
        return "html/complete";
    }
    
    /**
     * 기본값 설정
     */
    private void setDefaultValues(Model model) {
        model.addAttribute("orderNo", "");
        model.addAttribute("storeName", "가게명");
        model.addAttribute("merchantUid", "");
        model.addAttribute("orderItems", new ArrayList<>());
        model.addAttribute("menuAmount", 0L);
        model.addAttribute("deliveryTip", 0L);
        model.addAttribute("discountTotal", 0L);
        model.addAttribute("paymentAmount", 0L);
        model.addAttribute("payMethod", "카드");
    }
}

