package com.sist.baemin.order.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 결제 관련 뷰 컨트롤러
 */
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class PaymentViewController {

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
        
        if (isSuccess) {
            log.info("결제 성공 - paymentId: {}", paymentId);
        } else {
            log.warn("결제 실패/취소 - paymentId: {}, code: {}, message: {}", paymentId, code, message);
        }
        
        return "html/complete";
    }
}


