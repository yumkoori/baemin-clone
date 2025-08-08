package com.sist.baemin.order.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.order.dto.CartItemRequestDto;
import com.sist.baemin.order.dto.CartItemResponseDto;
import com.sist.baemin.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody CartItemRequestDto request) {
        try {
            CartItemResponseDto response = cartService.addToCart(request);
            
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