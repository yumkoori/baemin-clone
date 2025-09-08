package com.sist.baemin.order.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.order.dto.CartAddRequestDto;
import com.sist.baemin.order.dto.CartResponseDto;
import com.sist.baemin.order.dto.CartUpdateRequestDto;
import com.sist.baemin.order.dto.CartItemOptionsUpdateDto;
import com.sist.baemin.order.service.CartDbService;
import com.sist.baemin.order.service.CartResponseService;
import com.sist.baemin.user.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class CartController {
    
    /**
     * 인증 관련 예외를 처리하여 적절한 응답 반환
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResultDto<Object>> handleAuthenticationException(RuntimeException e) {
        if (e.getMessage().contains("로그인이 필요한")) {
            log.warn("인증 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ResultDto<>(401, e.getMessage(), null));
        }
        // 다른 RuntimeException은 500으로 처리
        log.error("서버 오류: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResultDto<>(500, "서버 오류가 발생했습니다.", null));
    }
    
    private final CartDbService cartDbService;
    private final CartResponseService cartResponseService;
    
    /**
     * SecurityContext에서 현재 인증된 사용자의 userId 추출
     * 비인증 사용자는 예외 발생
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
            && authentication.getPrincipal() instanceof CustomUserDetails) {
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUser().getUserId();
            log.info("SecurityContext에서 userId 추출 성공: {}", userId);
            return userId;
        }
        
        // 인증되지 않은 경우 예외 발생
        log.warn("비인증 사용자의 장바구니 접근 시도");
        throw new RuntimeException("로그인이 필요한 서비스입니다.");
    }
    
    @GetMapping
    public ResponseEntity<ResultDto<CartResponseDto>> getCart() {
        Long userId = getCurrentUserId();
        log.info("장바구니 조회 요청 - userId: {}", userId);
        
        CartResponseDto cart = cartResponseService.getCartResponse(userId);
        return ResponseEntity.ok(new ResultDto<>(200, "장바구니 조회 성공", cart));
    }
    
    @PostMapping("/items")
    public ResponseEntity<ResultDto<Map<String, Object>>> addToCart(
            @RequestBody CartAddRequestDto request) {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 추가 요청 - userId: {}, storeId: {}, menuId: {}", 
                userId, request.getStoreId(), request.getMenuId());
        
        // 기존 장바구니 확인
        boolean needConfirmation = cartDbService.checkCartConflict(userId, request.getStoreId());
        
        if (needConfirmation) {
            // 다른 가게의 장바구니가 존재하므로 사용자 확인 필요
            Map<String, Object> response = new HashMap<>();
            response.put("needConfirmation", true);
            response.put("message", "다른 가게의 장바구니가 존재합니다. 기존 장바구니를 비우고 새로운 가게의 메뉴를 추가하시겠습니까?");
            
            // success를 false로 설정하기 위해 resultCode를 409(Conflict)로 설정
            return ResponseEntity.ok(new ResultDto<>(409, "확인 필요", response));
        }
        
        // 실제 DB에 저장
        Long cartItemId = cartDbService.addToCartDb(userId, request);
        
        // 업데이트된 장바구니 정보 조회
        CartResponseDto cart = cartResponseService.getCartResponse(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("needConfirmation", false);
        response.put("cartItemId", cartItemId);
        response.put("totalItems", cart.getItems().size());
        response.put("totalAmount", cart.getFinalAmount());
        
        return ResponseEntity.ok(new ResultDto<>(201, "장바구니에 추가되었습니다.", response));
    }
    
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ResultDto<Map<String, Object>>> updateCartItem(
            @PathVariable Long cartItemId, 
            @RequestBody CartUpdateRequestDto request) {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 수량 변경 요청 - userId: {}, cartItemId: {}, quantity: {}", 
                userId, cartItemId, request.getQuantity());
        
        // 실제 DB 업데이트
        cartDbService.updateCartItemQuantity(cartItemId, request.getQuantity());
        
        // 업데이트된 장바구니 정보 조회
        CartResponseDto cart = cartResponseService.getCartResponse(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        response.put("newQuantity", request.getQuantity());
        response.put("totalItems", cart.getItems().size());
        response.put("totalAmount", cart.getFinalAmount());
        
        return ResponseEntity.ok(new ResultDto<>(200, "장바구니 아이템이 업데이트되었습니다.", response));
    }
    
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ResultDto<Map<String, Object>>> deleteCartItem(
            @PathVariable Long cartItemId) {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 아이템 삭제 요청 - userId: {}, cartItemId: {}", userId, cartItemId);
        
        // 실제 DB 삭제
        cartDbService.deleteCartItem(cartItemId);
        
        // 업데이트된 장바구니 정보 조회
        CartResponseDto cart = cartResponseService.getCartResponse(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        response.put("totalItems", cart.getItems().size());
        response.put("totalAmount", cart.getFinalAmount());
        
        return ResponseEntity.ok(new ResultDto<>(200, "장바구니 아이템이 삭제되었습니다.", response));
    }
    
    @DeleteMapping
    public ResponseEntity<ResultDto<Void>> clearCart() {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 전체 삭제 요청 - userId: {}", userId);
        
        // 실제 DB 전체 삭제
        cartDbService.clearCart(userId);
        
        return ResponseEntity.ok(new ResultDto<>(200, "장바구니가 비워졌습니다.", null));
    }
    
    @PutMapping("/items/{cartItemId}/options")
    public ResponseEntity<ResultDto<Map<String, Object>>> updateCartItemOptions(
            @PathVariable Long cartItemId,
            @RequestBody CartItemOptionsUpdateDto request) {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 옵션 변경 요청 - userId: {}, cartItemId: {}", userId, cartItemId);
        
        // 옵션 변경
        cartDbService.updateCartItemOptions(cartItemId, request.getOptions());
        
        // 업데이트된 장바구니 정보 반환
        CartResponseDto cart = cartResponseService.getCartResponse(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("cartItemId", cartItemId);
        response.put("updatedOptions", request.getOptions());
        response.put("totalItems", cart.getItems().size());
        response.put("totalAmount", cart.getFinalAmount());
        
        return ResponseEntity.ok(new ResultDto<>(200, "장바구니 옵션이 변경되었습니다.", response));
    }
    
    @PostMapping("/items/confirm")
    public ResponseEntity<ResultDto<Map<String, Object>>> addToCartWithConfirmation(
            @RequestBody CartAddRequestDto request) {
        
        Long userId = getCurrentUserId();
        log.info("장바구니 추가 확인 요청 - userId: {}, storeId: {}, menuId: {}", 
                userId, request.getStoreId(), request.getMenuId());
        
        try {
            // 기존 장바구니 삭제 및 새로운 장바구니 생성
            cartDbService.clearCartAndCreateNew(userId, request.getStoreId());
            
            // 실제 DB에 저장
            Long cartItemId = cartDbService.addToCartDb(userId, request);
            
            // 업데이트된 장바구니 정보 조회
            CartResponseDto cart = cartResponseService.getCartResponse(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cartItemId", cartItemId);
            response.put("totalItems", cart.getItems().size());
            response.put("totalAmount", cart.getFinalAmount());
            
            return ResponseEntity.ok(new ResultDto<>(201, "장바구니에 추가되었습니다.", response));
        } catch (Exception e) {
            log.error("장바구니 추가 중 오류 발생: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResultDto<>(500, "장바구니 추가 중 오류가 발생했습니다: " + e.getMessage(), null));
        }
    }
}