package com.sist.baemin.order.service;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.dto.CartResponseDto;
import com.sist.baemin.order.repository.CartRepository;
import com.sist.baemin.order.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartResponseService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    
    /**
     * DB에서 조회한 Cart 데이터를 CartResponseDto로 변환
     */
    public CartResponseDto getCartResponse(Long userId) {
        Optional<CartEntity> cartOpt = cartRepository.findByUserId(userId);
        
        if (cartOpt.isEmpty()) {
            // 빈 장바구니 반환
            return new CartResponseDto(
                null, null, null, 
                new ArrayList<>(), 0, 3000, 0, 3000, 15000, false
            );
        }
        
        CartEntity cart = cartOpt.get();
        return convertToDto(cart);
    }
    
    /**
     * cartId로 DB에서 조회한 Cart 데이터를 CartResponseDto로 변환
     */
    public CartResponseDto getCartResponseByCartId(Long cartId) {
        Optional<CartEntity> cartOpt = cartRepository.findByCartId(cartId);
        
        if (cartOpt.isEmpty()) {
            // 빈 장바구니 반환
            return new CartResponseDto(
                null, null, null, 
                new ArrayList<>(), 0, 3000, 0, 3000, 15000, false
            );
        }
        
        CartEntity cart = cartOpt.get();
        return convertToDto(cart);
    }
    
    /**
     * CartEntity를 CartResponseDto로 변환
     */
    private CartResponseDto convertToDto(CartEntity cart) {
        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cart.getCartId());
        
        // CartItem -> CartItemDto 변환
        List<CartResponseDto.CartItemDto> itemDtos = new ArrayList<>();
        int totalMenuAmount = 0;
        
        for (CartItemEntity item : cartItems) {
            // 실제 옵션 데이터 변환
            List<CartResponseDto.OptionDto> optionDtos = new ArrayList<>();
            int optionTotalPrice = 0;
            
            for (var option : item.getOptions()) {
                CartResponseDto.OptionDto optionDto = new CartResponseDto.OptionDto(
                    option.getMenuOption().getMenuOptionId(),
                    option.getMenuOption().getOptionName(),
                    option.getMenuOptionValue().getOptionValue(),
                    option.getMenuOptionValue().getAdditionalPrice(),
                    option.getMenuOptionValue().getMenuOptionValueId() // menuOptionValueId 추가
                );
                optionDtos.add(optionDto);
                optionTotalPrice += option.getMenuOptionValue().getAdditionalPrice();
            }
            
            int basePrice = item.getMenu().getPrice();
            int itemTotalPrice = (basePrice + optionTotalPrice) * item.getQuantity();
            totalMenuAmount += itemTotalPrice;
            
            CartResponseDto.CartItemDto itemDto = new CartResponseDto.CartItemDto(
                item.getCartItemId(),
                item.getMenu().getMenuId(),
                item.getMenu().getMenuName(),
                basePrice,
                item.getQuantity(),
                optionDtos,
                itemTotalPrice
            );
            
            itemDtos.add(itemDto);
        }
        
        // 배달팁 및 최종 금액 계산
        int deliveryFee = 3000;
        int discount = 0;
        int finalAmount = totalMenuAmount + deliveryFee - discount;
        int minOrderAmount = cart.getStore().getMinimumPrice();
        boolean canOrder = totalMenuAmount >= minOrderAmount;
        
        return new CartResponseDto(
            "cart_" + cart.getCartId(),
            cart.getStore().getStoreId(),
            cart.getStore().getStoreName(),
            itemDtos,
            totalMenuAmount,
            deliveryFee,
            discount,
            finalAmount,
            minOrderAmount,
            canOrder
        );
    }
}