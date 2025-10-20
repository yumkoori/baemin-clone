package com.sist.baemin.order.controller;

import com.sist.baemin.order.domain.CartEntity;
import com.sist.baemin.order.dto.CartDataDto;
import com.sist.baemin.order.service.CartDbService;
import com.sist.baemin.order.service.OrderService;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.domain.UserCouponEntity;
import com.sist.baemin.user.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import com.sist.baemin.user.domain.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Log4j2
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final CartDbService cartDbService;
    private final OrderService orderService;
    private final CouponService couponService;

    @GetMapping("/form")
    public String OrderPageFromCart(
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = null;
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
        }

        // 이제 SecurityConfig에서 이 경로가 보호되므로, user 객체는 null이 될 수 없습니다.
        // 따라서 user == null 체크는 이론적으로는 필요 없지만, 안전을 위해 유지할 수 있습니다.
        if (user == null) {
            // 이 코드가 실행된다면, 여전히 SecurityConfig에 문제가 있는 것입니다.
            log.warn("인증 정보가 없습니다. SecurityContextHolder에 Authentication 객체가 없거나 Principal이 예상 타입이 아닙니다.");
            log.warn("Current Authentication object: " + authentication);
            return "redirect:/login";
        }

        log.info("주문서 요청 - 사용자 ID: {}", user.getUserId());

        CartEntity cart = cartDbService.getUserCart(user.getUserId()).orElse(null);

        if (cart == null || cart.getCartItems().isEmpty()) {
            log.warn("주문서 접근 실패: 사용자 ID {}의 장바구니가 비어있습니다.", user.getUserId());
            return "redirect:/cart?error=empty";
        }

        try {
            CartDataDto cartData = convertToCartDataDto(cart);
            model.addAttribute("cartData", cartData);
            
            // 사용자의 사용 가능한 쿠폰 목록 조회
            List<UserCouponEntity> availableCoupons = couponService.getAvailableUserCoupons(user.getUserId());
            model.addAttribute("coupons", availableCoupons);
            model.addAttribute("couponCount", availableCoupons.size());
            
        } catch (Exception e) {
            log.error("장바구니 정보 변환 실패 - 사용자 ID: {}", user.getUserId(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "주문 정보를 불러오는 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "redirect:/cart";
        }
        return "html/form";
    }

    /**
     * 결제 완료 페이지
     * 포트원 결제 완료 후 리다이렉트되는 페이지
     */
    @GetMapping("/complete")
    public String orderComplete(
            @RequestParam(required = false) String paymentId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            Model model
    ) {
        log.info("결제 완료 페이지 접근 - paymentId: {}, code: {}", paymentId, code);
        
        model.addAttribute("paymentId", paymentId);
        model.addAttribute("code", code);
        model.addAttribute("message", message);
        
        // 결제 성공 여부 확인
        boolean isSuccess = code == null || code.isEmpty();
        model.addAttribute("isSuccess", isSuccess);
        
        return "html/complete";
    }

    private CartDataDto convertToCartDataDto(CartEntity cart) {
        List<CartDataDto.CartItemDto> itemDtos = cart.getCartItems().stream().map(item -> {
            List<CartDataDto.CartOptionDto> optionDtos = item.getOptions().stream().map(opt ->
                    new CartDataDto.CartOptionDto(
                            opt.getMenuOption().getMenuOptionId(),
                            opt.getMenuOptionValue().getMenuOptionValueId(),
                            opt.getMenuOption().getOptionName(),
                            opt.getMenuOptionValue().getOptionValue(),
                            opt.getMenuOptionValue().getAdditionalPrice()
                    )
            ).collect(Collectors.toList());

            int totalOptionsPrice = optionDtos.stream()
                    .mapToInt(CartDataDto.CartOptionDto::getAdditionalPrice)
                    .sum();

            int itemTotalPrice = (item.getMenu().getPrice() + totalOptionsPrice) * item.getQuantity();

            return new CartDataDto.CartItemDto(
                    item.getCartItemId(),
                    item.getMenu().getMenuId(),
                    item.getMenu().getMenuName(),
                    item.getMenu().getPrice(),
                    item.getQuantity(),
                    itemTotalPrice,
                    optionDtos
            );
        }).collect(Collectors.toList());

        long totalAmount = itemDtos.stream().mapToLong(CartDataDto.CartItemDto::getTotalPrice).sum();
        long deliveryFee = cart.getStore().getDeliveryFee();
        long minOrderAmount = cart.getStore().getMinimumPrice();
        long discountAmount = 0;
        long finalAmount = totalAmount + deliveryFee - discountAmount;
        boolean isOrderable = totalAmount >= minOrderAmount;

        return new CartDataDto(
                String.valueOf(cart.getCartId()),
                cart.getStore().getStoreId(),
                cart.getStore().getStoreName(),
                totalAmount,
                deliveryFee,
                discountAmount,
                finalAmount,
                minOrderAmount,
                isOrderable,
                itemDtos
        );
    }
}
