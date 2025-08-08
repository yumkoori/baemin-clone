package com.sist.baemin.order.service;

import com.sist.baemin.order.dto.CartItemRequestDto;
import com.sist.baemin.order.dto.CartItemResponseDto;
import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.repository.CartRepository;
import com.sist.baemin.order.repository.CartItemRepository;
import com.sist.baemin.menu.domain.MenuEntity;
import com.sist.baemin.menu.service.MenuService;
import com.sist.baemin.store.domain.StoreEntity;
import com.sist.baemin.store.service.StoreService;
import com.sist.baemin.user.domain.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuService menuService;
    private final StoreService storeService;
    
    // 장바구니에 메뉴 추가
    public CartItemResponseDto addToCart(UserEntity user, CartItemRequestDto request) {
        // 1. 메뉴 정보 조회
        MenuEntity menu = menuService.getMenuEntityById(request.getMenuId());
        if (menu == null) {
            throw new RuntimeException("메뉴를 찾을 수 없습니다.");
        }
        
        // 2. 가게 정보 조회
        StoreEntity store = storeService.getStoreEntityById(request.getStoreId());
        if (store == null) {
            throw new RuntimeException("가게를 찾을 수 없습니다.");
        }
        
        // 3. 사용자별 장바구니 조회 또는 생성
        CartEntity cart = getOrCreateCart(user, store);
        
        // 4. CartItem 생성
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        cartItem.setMenu(menu);
        cartItem.setQuantity(request.getQuantity());
        
        CartItemEntity savedCartItem = cartItemRepository.save(cartItem);
        
        // 5. 응답 데이터 생성
        CartItemResponseDto response = new CartItemResponseDto();
        response.setCartItemId(savedCartItem.getCartItemId());
        response.setTotalItems(request.getQuantity());
        response.setTotalAmount(menu.getPrice() * request.getQuantity());
        
        return response;
    }
    
    // 사용자별 장바구니 조회 또는 생성
    private CartEntity getOrCreateCart(UserEntity user, StoreEntity store) {
        // 1. 해당 사용자의 해당 가게 장바구니 조회
        Optional<CartEntity> existingCart = cartRepository.findByUserAndStore(user, store);
        
        // 2. 있으면 반환, 없으면 새로 생성
        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            CartEntity newCart = new CartEntity();
            newCart.setUser(user);
            newCart.setStore(store);
            newCart.setCreatedAt(LocalDateTime.now());
            return cartRepository.save(newCart);
        }
    }
} 