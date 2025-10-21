package com.sist.baemin.user.controller;

import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * 남은 쿠폰 수량 조회 (실시간)
     */
    @GetMapping("/remaining")
    @ResponseBody
    public Map<String, Object> getRemainingCount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            long currentStock = couponService.getCurrentCouponStock();
            long maxStock = couponService.getMaxCouponStock();
            long remaining = Math.max(0, maxStock - currentStock); // 음수 방지
            
            result.put("success", true);
            result.put("remaining", remaining);
            result.put("maxStock", maxStock);
            result.put("currentStock", currentStock);
            result.put("percentage", currentStock >= maxStock ? 100 : (int)((double)currentStock / maxStock * 100));
            result.put("isSoldOut", currentStock >= maxStock);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("remaining", 100);
            result.put("maxStock", 100);
            result.put("currentStock", 0);
            result.put("message", "수량 조회 실패");
        }
        
        return result;
    }
    
    /**
     * 쿠폰 재고 초기화 (관리자용 - 테스트/리셋)
     * 주의: 실제 운영에서는 권한 체크 필요!
     */
    @PostMapping("/reset-stock")
    @ResponseBody
    public Map<String, Object> resetCouponStock() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            couponService.resetCouponStock();
            result.put("success", true);
            result.put("message", "쿠폰 재고가 초기화되었습니다.");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "재고 초기화 실패: " + e.getMessage());
        }
        
        return result;
    }
}

