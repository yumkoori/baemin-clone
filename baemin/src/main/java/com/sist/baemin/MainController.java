package com.sist.baemin;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.repository.UserAddressRepository;
import com.sist.baemin.user.domain.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping("/api")
public class MainController {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @GetMapping("/main")
    public String mainPage(@AuthenticationPrincipal CustomUserDetails userDetails,
                           Model model,
                           @CookieValue(value = "Authorization", required = false) String jwtToken) {
        if (userDetails != null) {
            model.addAttribute("email", userDetails.getUsername());
            // 기본 주소가 없으면 온보딩으로 유도
            Long userId = userDetails.getUserId();
            boolean hasAddress = !userAddressRepository.findByUser_UserId(userId).isEmpty();
            if (!hasAddress) {
                return "redirect:/api/onboarding/address";
            }
        } else {
            System.out.println("사용자 이메일 시큐리티에 안담김");
            model.addAttribute("email", null);
        }

        // 로그인 제공자 식별 (카카오 토큰 클레임 존재 여부로 판별)
        String provider = null;
        try {
            if (jwtToken != null && !jwtToken.isBlank()) {
                String kakaoAccess = jwtUtil.extractClaimAsString(jwtToken, "kakao_access_token");
                provider = (kakaoAccess != null && !kakaoAccess.isBlank()) ? "kakao" : "google";
            }
        } catch (Exception ignored) {
        }
        model.addAttribute("provider", provider);
        return "html/main";
    }
    
    @GetMapping("/cart/page")
    public String cartPage(@AuthenticationPrincipal CustomUserDetails userDetails, 
                          Model model) {
        // 로그인하지 않은 사용자 처리
        if (userDetails == null) {
            return "redirect:/api/login";
        }
        
        model.addAttribute("title", "장바구니");
        return "html/cart";
    }
} 