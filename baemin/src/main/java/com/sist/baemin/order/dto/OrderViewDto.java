package com.sist.baemin.order.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderViewDto {

    // 상단/가게/주문정보
    private String storeName;
    private String storeImage;
    private String orderNo;

    // 금액/정산
    private int menuAmount;
    private int deliveryTip;
    private int discountTotal;
    private int paymentAmount;

    // 주문 아이템
    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private String menuName;
        private int basePrice;
        private int quantity;
        private int lineTotal;
        @Builder.Default
        private List<OptionDto> options = new ArrayList<>();
        private String optionSummary;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OptionDto {
        private String name;
        private int price;
    }
}


