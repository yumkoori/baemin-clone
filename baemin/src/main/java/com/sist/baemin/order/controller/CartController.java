package com.sist.baemin.order.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.order.dto.CartItemRequestDto;
import com.sist.baemin.order.dto.CartItemResponseDto;
import com.sist.baemin.order.service.CartService;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
    // 장바구니에 메뉴 추가
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            Authentication authentication, 
            @RequestBody CartItemRequestDto request) {
        try {
            // 현재 로그인된 사용자 정보 추출
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            UserEntity currentUser = userDetails.getUser();
            
            CartItemResponseDto response = cartService.addToCart(currentUser, request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "장바구니에 추가되었습니다.");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("data", null);
            
            return ResponseEntity.badRequest().body(result);
        }
    }
} 