package com.sist.baemin.user.controller;

import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.dto.FavoriteResponseDto;
import com.sist.baemin.user.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {
    
    private final FavoriteService favoriteService;
    
    // 찜 목록 페이지 (뷰 - HTML 반환)
    @GetMapping("/page")
    public String getFavoritesPage(
            @RequestParam(defaultValue = "store") String type,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자는 로그인 페이지로 리다이렉트
            return "redirect:/api/login";
        }
        
        Long userId = userDetails.getUserId();
        FavoriteResponseDto favorites = favoriteService.getFavorites(userId, type);
        model.addAttribute("favorites", favorites);
        
        return "html/favorites";
    }
}