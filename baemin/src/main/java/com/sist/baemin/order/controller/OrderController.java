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

@Controller
@Log4j2
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PaymentService paymentService;
    private final OrderService orderService;

    @PostMapping("/form")
    public String orderPageFromCart(
            @RequestParam(value = "totalAmount", required = false, defaultValue = "0") Long totalAmount,
            @RequestParam(value = "deliveryFee", required = false, defaultValue = "0") Long deliveryFee,
            @RequestParam(value = "discountAmount", required = false, defaultValue = "0") Long discountAmount,
            @RequestParam(value = "finalAmount", required = false, defaultValue = "0") Long finalAmount,
            @RequestParam(value = "cartId", required = false) String cartId,
            @RequestParam(value = "storeId", required = false) String storeId,
            @RequestParam(value = "storeName", required = false) String storeName,
            @RequestParam(value = "minOrderAmount", required = false) Long minOrderAmount,
            @RequestParam(value = "isOrderable", required = false) Boolean isOrderable,
            @RequestParam(value = "cartItemOptionIds", required = false) String cartItemOptionIds,
            Model model
    ) {
        // cart.html에서 넘어온 값들을 form.html에서 사용하는 모델 키로 매핑
        model.addAttribute("price", totalAmount);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("discount", discountAmount);
        model.addAttribute("paymentAmount", finalAmount);

        // 부가 정보도 모델로 전달(필요 시 form.html이나 추후 로직에서 사용 가능)
        model.addAttribute("cartId", cartId);
        model.addAttribute("storeId", storeId);
        model.addAttribute("storeName", storeName);
        model.addAttribute("minOrderAmount", minOrderAmount);
        model.addAttribute("isOrderable", isOrderable);
        model.addAttribute("cartItemOptionIds", cartItemOptionIds);
        return "html/form";
    }

    @GetMapping("/form")
    public String orderPage(
            @RequestParam(value = "cartItemOptionId", required = false) Long cartItemOptionId,
            @RequestParam(value = "deliveryfee", required = false) Long deliveryFee,
            @RequestParam(value = "price", required = false) Long price,
            @RequestParam(value = "discount", required = false, defaultValue = "0") Long discount,
            Model model
    ) {
        if (cartItemOptionId != null) {
            OrderViewDto dto = orderService.buildOrderViewByCartItemOptionId(cartItemOptionId);

            model.addAttribute("cartItemOptionId", cartItemOptionId);
            model.addAttribute("price", (long) dto.getMenuAmount());
            model.addAttribute("deliveryFee", (long) dto.getDeliveryTip());
            model.addAttribute("discount", (long) dto.getDiscountTotal());
            model.addAttribute("paymentAmount", (long) dto.getPaymentAmount());

            // 선택적으로 상세 표시할 수 있도록 추가 모델 세팅
            model.addAttribute("storeName", dto.getStoreName());
            model.addAttribute("menuName", dto.getItems().isEmpty() ? null : dto.getItems().get(0).getMenuName());
            model.addAttribute("quantity", dto.getItems().isEmpty() ? 1 : dto.getItems().get(0).getQuantity());
            return "html/form";
        }

        long safeDelivery = deliveryFee == null ? 0L : deliveryFee;
        long safePrice = price == null ? 0L : price;
        long safeDiscount = discount == null ? 0L : discount;
        long paymentAmount = safePrice + safeDelivery - safeDiscount;

        model.addAttribute("price", safePrice);
        model.addAttribute("deliveryFee", safeDelivery);
        model.addAttribute("discount", safeDiscount);
        model.addAttribute("paymentAmount", paymentAmount);
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

        if (cartItemOptionId != null) {
            OrderViewDto dto = orderService.buildOrderViewByCartItemOptionId(cartItemOptionId);
            model.addAttribute("menuAmount", dto.getMenuAmount());
            model.addAttribute("deliveryTip", dto.getDeliveryTip());
            model.addAttribute("discountTotal", dto.getDiscountTotal());
            model.addAttribute("paymentAmount", dto.getPaymentAmount());
            model.addAttribute("storeName", dto.getStoreName());
            model.addAttribute("orderNo", dto.getOrderNo());
            model.addAttribute("orderItems", dto.getItems());
        }

        return "html/complete";
    }
}
