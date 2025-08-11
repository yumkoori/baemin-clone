package com.sist.baemin.order.service;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.domain.CartItemOptionsEntity;
import com.sist.baemin.order.dto.CartAddRequestDto;
import com.sist.baemin.order.dto.CartItemOptionsUpdateDto;
import com.sist.baemin.order.repository.CartRepository;
import com.sist.baemin.order.repository.CartItemRepository;
import com.sist.baemin.order.repository.CartItemOptionsRepository;
import com.sist.baemin.user.repository.UserRepository;
import com.sist.baemin.store.repository.StoreRepository;
import com.sist.baemin.menu.repository.MenuRepository;
import com.sist.baemin.menu.repository.MenuOptionRepository;
import com.sist.baemin.menu.repository.MenuOptionValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartDbService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CartItemOptionsRepository cartItemOptionsRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    private final MenuOptionValueRepository menuOptionValueRepository;
    
    /**
     * 팀원이 전달한 데이터를 실제 DB에 저장
     * @param userId 사용자 ID
     * @param request 장바구니 추가 요청 데이터
     * @return 저장된 CartItem ID
     */
    public Long addToCartDb(Long userId, CartAddRequestDto request) {
        log.info("DB 저장 시작 - userId: {}, storeId: {}, menuId: {}", 
                userId, request.getStoreId(), request.getMenuId());
        
        // 1. 해당 사용자의 해당 가게 장바구니 조회 or 생성
        CartEntity cart = cartRepository.findByUserIdAndStoreId(userId, request.getStoreId())
                .orElseGet(() -> createNewCart(userId, request.getStoreId()));
        
        // 2. 동일한 메뉴와 옵션 조합이 이미 있는지 확인
        CartItemEntity cartItem = findOrCreateCartItem(cart, request);
        
        CartItemEntity savedItem = cartItemRepository.save(cartItem);
        
        log.info("DB 저장 완료 - cartItemId: {}", savedItem.getCartItemId());
        return savedItem.getCartItemId();
    }
    
    private CartEntity createNewCart(Long userId, Long storeId) {
        CartEntity cart = new CartEntity();
        cart.setUser(userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("사용자를 찾을 수 없습니다: " + userId)));
        cart.setStore(storeRepository.findById(storeId).orElseThrow(() -> 
            new RuntimeException("가게를 찾을 수 없습니다: " + storeId)));
        cart.setCreatedAt(LocalDateTime.now());
        
        return cartRepository.save(cart);
    }
    
    private CartItemEntity findOrCreateCartItem(CartEntity cart, CartAddRequestDto request) {
        // 같은 메뉴의 기존 장바구니 아이템들 조회
        List<CartItemEntity> existingItems = cartItemRepository
                .findByCartIdAndMenuId(cart.getCartId(), request.getMenuId());
        
        // 옵션 조합이 동일한 아이템 찾기
        for (CartItemEntity item : existingItems) {
            if (hasSameOptions(item, request.getOptions())) {
                // 동일한 옵션 조합 발견 - 수량 증가
                item.setQuantity(item.getQuantity() + request.getQuantity());
                log.info("기존 아이템 수량 업데이트: {} (옵션 동일)", item.getQuantity());
                return item;
            }
        }
        
        // 동일한 옵션 조합이 없으면 새 아이템 생성
        CartItemEntity newItem = createNewCartItem(cart, request);
        
        // 옵션 저장
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            for (var optionDto : request.getOptions()) {
                var menuOption = menuOptionRepository.findById(optionDto.getOptionId())
                    .orElseThrow(() -> new RuntimeException("메뉴 옵션을 찾을 수 없습니다: " + optionDto.getOptionId()));
                
                // menuOptionValueId로 직접 조회 (개선됨)
                var menuOptionValue = menuOptionValueRepository.findById(optionDto.getMenuOptionValueId())
                    .orElseThrow(() -> new RuntimeException("옵션 값을 찾을 수 없습니다: " + optionDto.getMenuOptionValueId()));
                
                CartItemOptionsEntity optionEntity = new CartItemOptionsEntity();
                optionEntity.setCartItem(newItem);
                optionEntity.setMenuOption(menuOption);
                optionEntity.setMenuOptionValue(menuOptionValue);
                
                newItem.getOptions().add(optionEntity);
                log.info("새 아이템 옵션: {} ({}) +{}원", 
                    menuOption.getOptionName(), menuOptionValue.getOptionValue(), 
                    menuOptionValue.getAdditionalPrice());
            }
        }
        
        log.info("새 장바구니 아이템 생성: menuId {}", request.getMenuId());
        return newItem;
    }
    
    private boolean hasSameOptions(CartItemEntity existingItem, List<CartAddRequestDto.OptionRequest> requestOptions) {
        if (requestOptions == null) requestOptions = new ArrayList<>();
        
        List<CartItemOptionsEntity> existingOptions = existingItem.getOptions();
        
        // 옵션 개수가 다르면 다른 조합
        if (existingOptions.size() != requestOptions.size()) {
            return false;
        }
        
        // 모든 옵션이 일치하는지 확인
        for (var requestOption : requestOptions) {
            boolean found = existingOptions.stream().anyMatch(existing -> 
                existing.getMenuOption().getMenuOptionId().equals(requestOption.getOptionId()) &&
                existing.getMenuOptionValue().getMenuOptionValueId().equals(requestOption.getMenuOptionValueId())
            );
            if (!found) {
                return false;
            }
        }
        
        return true;
    }

    private CartItemEntity createNewCartItem(CartEntity cart, CartAddRequestDto request) {
        CartItemEntity cartItem = new CartItemEntity();
        cartItem.setCart(cart);
        cartItem.setMenu(menuRepository.findById(request.getMenuId()).orElseThrow(() -> 
            new RuntimeException("메뉴를 찾을 수 없습니다: " + request.getMenuId())));
        cartItem.setQuantity(request.getQuantity());
        
        return cartItem;
    }
    
    /**
     * 장바구니 아이템 수량 업데이트
     * @param cartItemId 장바구니 아이템 ID
     * @param newQuantity 새로운 수량
     */
    public void updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        log.info("장바구니 아이템 수량 업데이트 - cartItemId: {}, newQuantity: {}", cartItemId, newQuantity);
        
        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        if (newQuantity <= 0) {
            throw new RuntimeException("수량은 1개 이상이어야 합니다.");
        }
        
        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
        
        log.info("장바구니 아이템 수량 업데이트 완료 - cartItemId: {}", cartItemId);
    }
    
    /**
     * 장바구니 아이템 삭제
     * @param cartItemId 장바구니 아이템 ID
     */
    public void deleteCartItem(Long cartItemId) {
        log.info("장바구니 아이템 삭제 - cartItemId: {}", cartItemId);
        
        if (!cartItemRepository.existsById(cartItemId)) {
            throw new RuntimeException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId);
        }
        
        cartItemRepository.deleteById(cartItemId);
        log.info("장바구니 아이템 삭제 완료 - cartItemId: {}", cartItemId);
    }
    
    /**
     * 사용자의 장바구니 전체 삭제
     * @param userId 사용자 ID
     */
    public void clearCart(Long userId) {
        log.info("장바구니 전체 삭제 - userId: {}", userId);
        
        Optional<CartEntity> cart = cartRepository.findByUserId(userId);
        if (cart.isPresent()) {
            cartItemRepository.deleteByCartCartId(cart.get().getCartId());
            log.info("장바구니 전체 삭제 완료 - userId: {}, cartId: {}", userId, cart.get().getCartId());
        } else {
            log.info("삭제할 장바구니가 없습니다 - userId: {}", userId);
        }
    }
    
    /**
     * 장바구니 아이템의 옵션 변경
     * @param cartItemId 장바구니 아이템 ID
     * @param newOptions 새로운 옵션 목록
     */
    public void updateCartItemOptions(Long cartItemId, List<CartItemOptionsUpdateDto.OptionRequest> newOptions) {
        log.info("장바구니 아이템 옵션 변경 - cartItemId: {}", cartItemId);
        
        CartItemEntity cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("장바구니 아이템을 찾을 수 없습니다: " + cartItemId));
        
        // 기존 옵션들 삭제
        cartItemOptionsRepository.deleteByCartItemCartItemId(cartItemId);
        cartItem.getOptions().clear();
        
        // 새로운 옵션들 추가
        if (newOptions != null && !newOptions.isEmpty()) {
            for (var optionDto : newOptions) {
                var menuOption = menuOptionRepository.findById(optionDto.getOptionId())
                    .orElseThrow(() -> new RuntimeException("메뉴 옵션을 찾을 수 없습니다: " + optionDto.getOptionId()));
                
                // menuOptionValueId로 직접 조회 (개선됨)
                var menuOptionValue = menuOptionValueRepository.findById(optionDto.getMenuOptionValueId())
                    .orElseThrow(() -> new RuntimeException("옵션 값을 찾을 수 없습니다: " + optionDto.getMenuOptionValueId()));
                
                CartItemOptionsEntity optionEntity = new CartItemOptionsEntity();
                optionEntity.setCartItem(cartItem);
                optionEntity.setMenuOption(menuOption);
                optionEntity.setMenuOptionValue(menuOptionValue);
                
                cartItemOptionsRepository.save(optionEntity);
                cartItem.getOptions().add(optionEntity);
                
                log.info("새 옵션 저장: {} ({}) +{}원", 
                    menuOption.getOptionName(), menuOptionValue.getOptionValue(), 
                    menuOptionValue.getAdditionalPrice());
            }
        }
        
        // 장바구니 아이템 저장
        cartItemRepository.save(cartItem);
        log.info("장바구니 아이템 옵션 변경 완료 - cartItemId: {}", cartItemId);
    }
    
    /**
     * 사용자의 전체 장바구니 조회
     */
    @Transactional(readOnly = true)
    public Optional<CartEntity> getUserCart(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}