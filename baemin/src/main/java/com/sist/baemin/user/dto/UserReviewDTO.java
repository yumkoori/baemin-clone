package com.sist.baemin.user.dto;

import lombok.ToString;
import lombok.Getter;

@ToString
@Getter
public class UserReviewDTO {
    private Long reviewId;
    private String storeName;
    private int rating;
    private String content;
    private String reviewImage;


    public UserReviewDTO(Long reviewId, String storeName, int rating, String content, String reviewImage) {
        this.reviewId = reviewId;
        this.storeName = storeName;
        this.rating = rating;
        this.content = content;
        this.reviewImage = reviewImage;
    }
}
