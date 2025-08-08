package com.sist.baemin.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private String userName;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private int userReviewCount;
    private double userAverageRating;
    private String orderDate;
    private boolean hasImages;
    private List<String> images;
} 