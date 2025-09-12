package com.sist.baemin.order.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import com.sist.baemin.order.payment.domain.PaymentEntity;
import com.sist.baemin.order.payment.service.PaymentService;
import com.sist.baemin.order.dto.OrderViewDto;
import com.sist.baemin.order.service.OrderService;
import com.sist.baemin.order.dto.CartDataDto;
import com.sist.baemin.order.dto.CartResponseDto;
import com.sist.baemin.order.service.CartResponseService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Log4j2
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final CartResponseService cartResponseService;

    // 장바구니 페이지에서 cartId만으로 결제 페이지 호출
    @GetMapping("/form")
    public String orderPageFromCart(
            @RequestParam(value = "cartId", required = false) String cartId,
            Model model
    ) {
        // cartId가 있는 경우, DB에서 장바구니 데이터 조회
        if (cartId != null && !cartId.isEmpty()) {
            try {
                // cartId에서 실제 ID 추출 (cart_ 접두사 제거)
                String actualCartIdStr = cartId.replace("cart_", "");
                Long actualCartId = Long.parseLong(actualCartIdStr);
                
                // CartResponseService를 사용하여 장바구니 데이터 조회
                CartResponseDto cartData = cartResponseService.getCartResponseByCartId(actualCartId);
                
                if (cartData != null) {
                    // CartResponseDto에서 데이터 추출
                    model.addAttribute("cartData", cartData);
                    model.addAttribute("price", cartData.getTotalAmount());
                    model.addAttribute("deliveryFee", cartData.getDeliveryFee());
                    model.addAttribute("discount", cartData.getDiscountAmount());
                    model.addAttribute("paymentAmount", cartData.getFinalAmount());
                    
                    // 부가 정보도 모델로 전달
                    model.addAttribute("cartId", cartData.getCartId());
                    model.addAttribute("storeId", cartData.getStoreId());
                    model.addAttribute("storeName", cartData.getStoreName());
                    model.addAttribute("minOrderAmount", cartData.getMinOrderAmount());
                    model.addAttribute("isOrderable", cartData.getIsOrderable());
                    
                    // complete.html에서 사용할 데이터를 세션에 저장
                    model.addAttribute("cartTotalAmount", cartData.getTotalAmount());
                    model.addAttribute("cartDeliveryFee", cartData.getDeliveryFee());
                    model.addAttribute("cartDiscountAmount", cartData.getDiscountAmount());
                    model.addAttribute("cartFinalAmount", cartData.getFinalAmount());
                }
            } catch (Exception e) {
                log.error("Failed to retrieve cart data by cartId", e);
            }
        }
        
        return "html/form";
    }

    

    /**
     * 결제 모듈 성공 콜백에서 리다이렉트되는 완료 페이지.
     */
    @GetMapping("/complete")
    public String complete(
            @RequestParam("imp_uid") String impUid,
            @RequestParam("merchant_uid") String merchantUid,
            @RequestParam("amount") Long amount,
            @RequestParam(value = "pg", required = false) String pg,
            @RequestParam(value = "pay_method", required = false) String payMethod,
            @RequestParam(value = "cartItemOptionId", required = false) Long cartItemOptionId,
            @RequestParam(value = "totalAmount", required = false) Long totalAmount,
            @RequestParam(value = "deliveryFee", required = false) Long deliveryFee,
            @RequestParam(value = "discountAmount", required = false) Long discountAmount,
            @RequestParam(value = "finalAmount", required = false) Long finalAmount,
            Model model
    ) {
        log.info("Payment complete callback: imp_uid={}, merchant_uid={}, amount={}, pg={}, pay_method={}",
                impUid, merchantUid, amount, pg, payMethod);

        PaymentEntity payment = paymentService.createPayment(null, amount, "PAID");

        model.addAttribute("impUid", impUid);
        model.addAttribute("merchantUid", merchantUid);
        model.addAttribute("amount", amount);
        model.addAttribute("pg", pg);
        model.addAttribute("payMethod", payMethod);
        model.addAttribute("paymentId", payment.getPaymentId());

        // cart.html에서 넘어온 데이터 사용
        if (totalAmount != null) {
            model.addAttribute("menuAmount", totalAmount);
        }
        if (deliveryFee != null) {
            model.addAttribute("deliveryTip", deliveryFee);
        }
        if (discountAmount != null) {
            model.addAttribute("discountTotal", discountAmount);
        }
        if (finalAmount != null) {
            model.addAttribute("paymentAmount", finalAmount);
        }

        if (cartItemOptionId != null) {
            OrderViewDto dto = orderService.buildOrderViewByCartItemOptionId(cartItemOptionId);
            // cart.html 데이터가 없으면 기존 로직 사용
            if (totalAmount == null) {
                model.addAttribute("menuAmount", dto.getMenuAmount());
            }
            if (deliveryFee == null) {
                model.addAttribute("deliveryTip", dto.getDeliveryTip());
            }
            if (discountAmount == null) {
                model.addAttribute("discountTotal", dto.getDiscountTotal());
            }
            if (finalAmount == null) {
                model.addAttribute("paymentAmount", dto.getPaymentAmount());
            }
            model.addAttribute("storeName", dto.getStoreName());
            model.addAttribute("orderNo", dto.getOrderNo());
            model.addAttribute("orderItems", dto.getItems());
        }

        return "html/complete";
    }
}
