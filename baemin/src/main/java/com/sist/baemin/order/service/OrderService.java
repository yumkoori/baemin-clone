package com.sist.baemin.order.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.baemin.menu.domain.MenuEntity;
import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.domain.CartItemEntity;
import com.sist.baemin.order.domain.CartItemOptionsEntity;
import com.sist.baemin.order.dto.OrderViewDto;
import com.sist.baemin.order.dto.OrderViewDto.OptionDto;
import com.sist.baemin.order.dto.OrderViewDto.OrderItemDto;
import com.sist.baemin.order.repository.CartItemOptionsRepository;
import com.sist.baemin.store.domain.StoreEntity;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartItemOptionsRepository cartItemOptionsRepository;

    @Transactional(readOnly = true)
    public OrderViewDto buildOrderViewByCartItemOptionId(Long cartItemOptionId) {
        Optional<CartItemOptionsEntity> optional = cartItemOptionsRepository.findByIdWithAllJoins(cartItemOptionId);
        CartItemOptionsEntity cio = optional.orElseThrow(() -> new IllegalArgumentException("장바구니 옵션을 찾을 수 없습니다."));

        CartItemEntity cartItem = cio.getCartItem();
        MenuEntity menu = cartItem.getMenu();
        CartEntity cart = cartItem.getCart();
        StoreEntity store = cart.getStore();

        // 기본 금액 계산 (옵션 1개 기준, 필요 시 확장)
        int basePrice = menu.getPrice();
        int optionPrice = cio.getMenuOptionValue().getAdditionalPrice() == null ? 0 : cio.getMenuOptionValue().getAdditionalPrice();
        int quantity = cartItem.getQuantity() == null ? 1 : cartItem.getQuantity();
        int lineTotal = (basePrice + optionPrice) * quantity;

        // 배달비/할인은 임시 값 (스토어/쿠폰 로직 연동 시 수정)
        int deliveryTip = store.getDeliveryFee();
        int discountTotal = 0;
        int paymentAmount = lineTotal + deliveryTip - discountTotal;

        OrderItemDto itemDto = OrderItemDto.builder()
                .menuName(menu.getMenuName())
                .basePrice(basePrice)
                .quantity(quantity)
                .lineTotal(lineTotal)
                .optionSummary(cio.getMenuOption().getOptionName() + ": " + cio.getMenuOptionValue().getOptionValue())
                .build();

        itemDto.getOptions().add(OptionDto.builder()
                .name(cio.getMenuOptionValue().getOptionValue())
                .price(optionPrice)
                .build());

        OrderViewDto dto = OrderViewDto.builder()
                .storeName(store.getStoreName())
                .storeImage(store.getMainImage())
                .orderNo("T" + System.currentTimeMillis())
                .menuAmount(lineTotal)
                .deliveryTip(deliveryTip)
                .discountTotal(discountTotal)
                .paymentAmount(paymentAmount)
                .build();
        dto.getItems().add(itemDto);
        return dto;
    }
}
