package com.sist.baemin.user.controller;

import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {
    
    private final CouponService couponService;
    
    /**
     * 쿠폰 이벤트 페이지 (정적 페이지)
     */
    @GetMapping("/event")
    public String couponEventPage(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "redirect:/api/login";
        }
        return "html/coupon-event";
    }
    
    /**
     * 쿠폰 발급 처리
     */
    @PostMapping("/issue")
    @ResponseBody
    public String issueCoupon(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 로그인 체크
        if (userDetails == null) {
            return "로그인이 필요합니다.";
        }
        
        try {
            // 쿠폰 발급
            couponService.issueCouponToUser(userDetails.getUserId());
            return "쿠폰이 발급되었습니다!";
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        } catch (IllegalStateException e) {
            return e.getMessage();
        } catch (Exception e) {
            return "쿠폰 발급 중 오류가 발생했습니다.";
        }
    }
}

