package com.sist.baemin.review.controller;

import com.sist.baemin.review.dto.ReviewResponseDto;
import com.sist.baemin.review.dto.ReviewStatsDto;
import com.sist.baemin.review.service.ReviewService;
import com.sist.baemin.store.dto.StoreResponseDto;
import com.sist.baemin.store.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;
    private final StoreService storeService;

    public ReviewController(ReviewService reviewService, StoreService storeService) {
        this.reviewService = reviewService;
        this.storeService = storeService;
    }

    // 리뷰 페이지 (뷰 - HTML 반환)
    @GetMapping("/store/{storeId}/reviews")
    public String reviewList(@PathVariable("storeId") Long storeId, Model model) {
        try {
            // 가게 정보 조회
            StoreResponseDto store = storeService.getStoreDetail(storeId);
            model.addAttribute("store", store);
            
            // 리뷰 통계 조회
            ReviewStatsDto reviewStats = reviewService.getReviewStatsByStore(storeId);
            model.addAttribute("reviewStats", reviewStats);
            
            // 리뷰 목록 조회
            List<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId);
            model.addAttribute("reviews", reviews);
            
            // 장바구니 아이템 수
            model.addAttribute("cartItemCount", 0);
            
        } catch (RuntimeException e) {
            // 가게를 찾을 수 없는 경우 기본값 설정
            model.addAttribute("store", null);
            model.addAttribute("reviewStats", ReviewStatsDto.builder()
                    .averageRating(4.5)
                    .totalReviews(0)
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .ownerComments(0)
                    .build());
            model.addAttribute("reviews", new ArrayList<>());
            model.addAttribute("cartItemCount", 0);
        }
        
        return "review/review-list";
    }

    // 가게별 리뷰 목록 API (JSON 반환)
    @GetMapping("/reviews/store/{storeId}")
    @ResponseBody
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByStoreApi(@PathVariable Long storeId) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByStore(storeId);
        return ResponseEntity.ok(reviews);
    }

    // 가게별 리뷰 통계 API (JSON 반환)
    @GetMapping("/reviews/stats/{storeId}")
    @ResponseBody
    public ResponseEntity<ReviewStatsDto> getReviewStatsApi(@PathVariable Long storeId) {
        ReviewStatsDto reviewStats = reviewService.getReviewStatsByStore(storeId);
        return ResponseEntity.ok(reviewStats);
    }
} 