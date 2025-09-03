package com.sist.baemin;

import com.sist.baemin.user.domain.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class MainController {
    @GetMapping("/main")
    public String mainPage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("email", userDetails.getUsername());
        } else {
            System.out.println("사용자 이메일 시큐리티에 안담김");
            model.addAttribute("email", null);
        }
        return "html/main";
    }

    @GetMapping("/cart/page")
    public String cartPage(Model model) {
        model.addAttribute("title", "장바구니");
        return "html/cart";
    }

    /**
     * 최근 주문한 가게 목록을 반환하는 API
     * TODO: 실제 DB에서 사용자의 최근 주문 데이터를 조회하도록 구현
     */
    @GetMapping("/recent-orders")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentOrders(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        if (userDetails != null) {
            // TODO: 실제 DB 조회 로직으로 교체 필요
            // Long userId = userDetails.getUserId();
            // List<RecentOrderDto> recentOrders = orderService.getRecentOrdersByUserId(userId);

            // 임시 데이터 (실제 구현시 DB에서 조회한 데이터로 교체)
            List<Map<String, Object>> mockData = Arrays.asList(
                    createMockRestaurant("마라이", "⭐ 4.5", "배달팁 1,500원~2,500원", "0.8km", "/images/restaurant1.jpg", true),
                    createMockRestaurant("고트홀리", "⭐ 4.5", "배달팁 1,500원~2,500원", "0.8km", "/images/restaurant2.jpg", true),
                    createMockRestaurant("치킨카", "⭐ 4.5", "배달팁 1,500원~2,500원", "0.8km", "/images/restaurant3.jpg", true),
                    createMockRestaurant("마약족발", "⭐ 4.8", "배달팁 1,500원", "1.2km", "/images/restaurant4.jpg", true),
                    createMockRestaurant("김치찌개집", "⭐ 4.3", "배달팁 2,000원", "1.5km", "/images/restaurant1.jpg", false),
                    createMockRestaurant("피자헛", "⭐ 4.6", "배달팁 3,000원", "2.1km", "/images/restaurant2.jpg", true)
            );

            response.put("success", true);
            response.put("data", mockData);
        } else {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 임시 레스토랑 데이터 생성 헬퍼 메서드
     * TODO: 실제 구현시 제거하고 DTO 클래스 사용
     */
    private Map<String, Object> createMockRestaurant(String name, String rating, String deliveryFee, String distance, String imageUrl, boolean hasCoupon) {
        Map<String, Object> restaurant = new HashMap<>();
        restaurant.put("name", name);
        restaurant.put("rating", rating);
        restaurant.put("deliveryFee", deliveryFee);
        restaurant.put("distance", distance);
        restaurant.put("imageUrl", imageUrl);
        restaurant.put("hasCoupon", hasCoupon);
        return restaurant;
    }
}