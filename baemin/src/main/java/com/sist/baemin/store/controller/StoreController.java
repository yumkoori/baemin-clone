package com.sist.baemin.store.controller;

import com.sist.baemin.menu.dto.MenuResponseDto;
import com.sist.baemin.menu.service.MenuService;
import com.sist.baemin.review.dto.ReviewResponseDto;
import com.sist.baemin.review.service.ReviewService;
import com.sist.baemin.store.dto.StoreResponseDto;
import com.sist.baemin.store.service.StoreService;
import com.sist.baemin.user.domain.UserAddressEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.service.UserAddressService;
import com.sist.baemin.user.service.UserService;
import com.sist.baemin.user.service.FavoriteService;
import com.sist.baemin.user.dto.FavoriteStatusDto;
import com.sist.baemin.user.dto.FavoriteRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/api")
public class StoreController {

    private final StoreService storeService;
    private final MenuService menuService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final UserAddressService userAddressService;
    private final FavoriteService favoriteService;

    public StoreController(StoreService storeService, MenuService menuService, ReviewService reviewService, UserService userService, UserAddressService userAddressService, FavoriteService favoriteService) {
        this.storeService = storeService;
        this.menuService = menuService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.userAddressService = userAddressService;
        this.favoriteService = favoriteService;
    }

    // 현재 인증된 사용자의 기본 주소를 가져오는 유틸리티 메소드
    private UserAddressEntity getCurrentUserDefaultAddress() {
        System.out.println("=== getCurrentUserDefaultAddress() called ===");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("Authentication object: " + authentication);
        
        if (authentication == null) {
            System.out.println("Authentication is null, returning null address");
            return null;
        }
        
        System.out.println("Is authenticated: " + authentication.isAuthenticated());
        System.out.println("Authentication class: " + authentication.getClass().getName());
        
        if (authentication instanceof AnonymousAuthenticationToken) {
            System.out.println("User is anonymous, returning null address");
            return null;
        }
        
        if (!authentication.isAuthenticated()) {
            System.out.println("User is not authenticated, returning null address");
            return null;
        }
        
        String username = authentication.getName();
        System.out.println("Authenticated username: " + username);
        
        UserEntity user = userService.findByUsername(username);
        System.out.println("Found user entity: " + user);
        
        if (user == null) {
            System.out.println("User not found, returning null address");
            return null;
        }
        
        UserAddressEntity address = userAddressService.findDefaultAddressByUserId(user.getUserId());
        System.out.println("Found user address: " + address);
        System.out.println("=== getCurrentUserDefaultAddress() completed ===");
        
        return address;
    }

    // 가게 상세 페이지 (뷰 - HTML 반환)
    @GetMapping("/store/{storeId}")
    public String storeDetail(@PathVariable("storeId") Long storeId, Model model) {
        try {
            // 현재 로그인한 사용자의 기본 주소를 가져옵니다.
            UserAddressEntity userAddress = getCurrentUserDefaultAddress();
            
            // DB에서 가게 정보 조회 (사용자 주소 기반)
            StoreResponseDto store = storeService.getStoreDetail(storeId, userAddress);
            model.addAttribute("store", store);
            
            // 해당 가게의 메뉴 조회
            List<MenuResponseDto> allMenus = menuService.getMenusByStore(storeId);
            
            // 메뉴를 각 탭에 전달 (현재는 모든 메뉴를 각 탭에 표시)
            model.addAttribute("popularMenus", allMenus);
            model.addAttribute("mainMenus", allMenus);
            model.addAttribute("setMenus", allMenus);
            model.addAttribute("dumplingMenus", allMenus);
            
            // 최신 리뷰 1개 조회
            List<ReviewResponseDto> latestReviews = reviewService.getReviewsByStore(storeId);
            ReviewResponseDto latestReview = latestReviews.isEmpty() ? null : latestReviews.get(0);
            model.addAttribute("latestReview", latestReview);
            
            // 장바구니 아이템 수
            model.addAttribute("cartItemCount", 0);
            
        } catch (RuntimeException e) {
            // 가게를 찾을 수 없는 경우 기본값 설정
            model.addAttribute("store", null);
            model.addAttribute("popularMenus", new ArrayList<>());
            model.addAttribute("mainMenus", new ArrayList<>());
            model.addAttribute("setMenus", new ArrayList<>());
            model.addAttribute("dumplingMenus", new ArrayList<>());
            model.addAttribute("latestReview", null);
            model.addAttribute("cartItemCount", 0);
        }

        return "html/store-detail";
    }

    // 가게 상세정보 페이지 (가게정보·원산지) (뷰 - HTML 반환)
    @GetMapping("/store/{storeId}/info")
    public String storeInfo(@PathVariable("storeId") Long storeId, Model model) {
        try {
            // 현재 로그인한 사용자의 기본 주소를 가져옵니다.
            UserAddressEntity userAddress = getCurrentUserDefaultAddress();
            
            // DB에서 가게 정보 조회 (사용자 주소 기반)
            StoreResponseDto store = storeService.getStoreDetail(storeId, userAddress);
            model.addAttribute("store", store);
            
            // 장바구니 아이템 수
            model.addAttribute("cartItemCount", 0);
            
        } catch (RuntimeException e) {
            // 가게를 찾을 수 없는 경우 기본값 설정
            model.addAttribute("store", null);
            model.addAttribute("cartItemCount", 0);
        }
        
        return "html/store-info";
    }

    // 가게 상세 정보 API (JSON 반환)
    @GetMapping("/stores/{storeId}")
    @ResponseBody
    public ResponseEntity<StoreResponseDto> getStoreApi(@PathVariable Long storeId) {
        // API 호출 시에도 예상 시간을 계산하여 반환
        // 현재 로그인한 사용자의 기본 주소를 가져옵니다.
        UserAddressEntity userAddress = getCurrentUserDefaultAddress();
        
        StoreResponseDto store = storeService.getStoreDetail(storeId, userAddress);
        return ResponseEntity.ok(store);
    }

    // 찜 상태 확인 API
    @GetMapping("/stores/{storeId}/favorite-status")
    @ResponseBody
    public ResponseEntity<FavoriteStatusDto> getStoreFavoriteStatus(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자인 경우
            FavoriteStatusDto status = favoriteService.checkFavoriteStatusForAnonymous("store", storeId);
            return ResponseEntity.ok(status);
        }
        
        // 로그인한 사용자인 경우
        Long userId = userDetails.getUserId();
        FavoriteStatusDto status = favoriteService.checkFavoriteStatus(userId, "store", storeId);
        return ResponseEntity.ok(status);
    }

    // 찜 추가/삭제 API
    @PostMapping("/stores/{storeId}/toggle-favorite")
    @ResponseBody
    public ResponseEntity<FavoriteStatusDto> toggleStoreFavorite(
            @PathVariable Long storeId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        if (userDetails == null) {
            // 로그인하지 않은 사용자인 경우 401 Unauthorized 반환
            return ResponseEntity.status(401).build();
        }
        
        // 로그인한 사용자인 경우
        Long userId = userDetails.getUserId();
        FavoriteRequestDto request = new FavoriteRequestDto("store", storeId);
        
        // 현재 찜 상태 확인
        FavoriteStatusDto currentStatus = favoriteService.checkFavoriteStatus(userId, "store", storeId);
        
        if (currentStatus.getIsFavorite()) {
            // 이미 찜한 상태이면 삭제
            favoriteService.deleteFavorite(userId, currentStatus.getFavoriteId());
            return ResponseEntity.ok(new FavoriteStatusDto(false, null));
        } else {
            // 찜하지 않은 상태이면 추가
            FavoriteStatusDto status = favoriteService.addFavorite(userId, request);
            return ResponseEntity.ok(status);
        }
    }

    // 전체 가게 목록 API (JSON 반환)
    @GetMapping("/stores")
    @ResponseBody
    public ResponseEntity<List<StoreResponseDto>> getAllStoresApi() {
        List<StoreResponseDto> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    // 가게 검색 API (JSON 반환)
    @GetMapping("/stores/search")
    @ResponseBody
    public ResponseEntity<List<StoreResponseDto>> searchStoresApi(@RequestParam String keyword) {
        List<StoreResponseDto> stores = storeService.searchStoresByName(keyword);
        return ResponseEntity.ok(stores);
    }

    // 운영 중인 가게 API (JSON 반환)
    @GetMapping("/stores/open")
    @ResponseBody
    public ResponseEntity<List<StoreResponseDto>> getOpenStoresApi() {
        // 현재는 모든 가게를 반환 (운영 중인 가게 필터링 로직은 추후 구현)
        List<StoreResponseDto> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }
} 