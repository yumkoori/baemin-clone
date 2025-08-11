package com.sist.baemin.review.service;

import com.sist.baemin.common.util.TimeUtil;
import com.sist.baemin.review.dto.ReviewResponseDto;
import com.sist.baemin.review.dto.ReviewStatsDto;
import com.sist.baemin.review.repository.ReviewRepository;
import com.sist.baemin.store.domain.ReviewEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    
    // 가게별 리뷰 목록 조회
    public List<ReviewResponseDto> getReviewsByStore(Long storeId) {
        try {
            List<ReviewEntity> reviews = reviewRepository.findByStore_StoreIdOrderByCreatedAtDesc(storeId);
            return reviews.stream()
                    .map(this::convertToReviewResponseDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("리뷰 조회 중 오류 발생: {}", e.getMessage());
            return List.of();
        }
    }
    
    // 가게별 리뷰 통계 조회
    public ReviewStatsDto getReviewStatsByStore(Long storeId) {
        try {
            long totalReviews = reviewRepository.countByStoreId(storeId);
            Double averageRating = reviewRepository.getAverageRatingByStoreId(storeId);
            
            // 별점별 리뷰 수 동적 계산
            long fiveStarCount = reviewRepository.countByStoreIdAndRating(storeId, 5);
            long fourStarCount = reviewRepository.countByStoreIdAndRating(storeId, 4);
            long threeStarCount = reviewRepository.countByStoreIdAndRating(storeId, 3);
            long twoStarCount = reviewRepository.countByStoreIdAndRating(storeId, 2);
            long oneStarCount = reviewRepository.countByStoreIdAndRating(storeId, 1);
            
            // 사장님 댓글 수는 현재 ReviewEntity에 필드가 없으므로 0으로 설정
            long ownerComments = 0;
            
            return ReviewStatsDto.builder()
                    .averageRating(averageRating != null ? averageRating : 0.0)
                    .totalReviews((int) totalReviews)
                    .fiveStarCount((int) fiveStarCount)
                    .fourStarCount((int) fourStarCount)
                    .threeStarCount((int) threeStarCount)
                    .twoStarCount((int) twoStarCount)
                    .oneStarCount((int) oneStarCount)
                    .ownerComments((int) ownerComments)
                    .build();
        } catch (Exception e) {
            log.error("리뷰 통계 조회 중 오류 발생: {}", e.getMessage());
            return ReviewStatsDto.builder()
                    .averageRating(0.0)
                    .totalReviews(0)
                    .fiveStarCount(0)
                    .fourStarCount(0)
                    .threeStarCount(0)
                    .twoStarCount(0)
                    .oneStarCount(0)
                    .ownerComments(0)
                    .build();
        }
    }
    
    // Entity를 DTO로 변환
    private ReviewResponseDto convertToReviewResponseDto(ReviewEntity review) {
        // 사용자별 리뷰 수와 평균 평점 동적 계산
        int userReviewCount = 0;
        double userAverageRating = 0.0;
        
        try {
            if (review.getUser() != null) {
                // UserEntity에 getter가 없으므로 reflection 사용
                Long userId = null;
                try {
                    java.lang.reflect.Field userIdField = review.getUser().getClass().getDeclaredField("userId");
                    userIdField.setAccessible(true);
                    userId = (Long) userIdField.get(review.getUser());
                } catch (Exception e) {
                    log.warn("userId 필드 접근 실패: {}", e.getMessage());
                }
                
                if (userId != null) {
                    userReviewCount = (int) reviewRepository.countByUserId(userId);
                    Double avgRating = reviewRepository.getAverageRatingByUserId(userId);
                    userAverageRating = avgRating != null ? avgRating : 0.0;
                }
            }
        } catch (Exception e) {
            log.warn("사용자별 리뷰 통계 조회 실패: {}", e.getMessage());
        }
        
        // 사용자명 가져오기
        String userName = "익명";
        try {
            if (review.getUser() != null) {
                // UserEntity에서 실제 사용자명 가져오기
                java.lang.reflect.Field nameField = review.getUser().getClass().getDeclaredField("name");
                nameField.setAccessible(true);
                String actualName = (String) nameField.get(review.getUser());
                if (actualName != null && !actualName.trim().isEmpty()) {
                    userName = actualName;
                }
            }
        } catch (Exception e) {
            log.warn("사용자명 조회 실패: {}", e.getMessage());
        }
        
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .userName(userName)
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .userReviewCount(userReviewCount)
                .userAverageRating(userAverageRating)
                .orderDate(TimeUtil.getRelativeTime(review.getCreatedAt())) // 상대적 시간 표시
                .hasImages(false) // 임시 데이터 (ReviewImagesEntity 연관관계 확인 필요)
                .images(List.of()) // 임시 데이터
                .build();
    }
} 