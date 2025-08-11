package com.sist.baemin.review.repository;

import com.sist.baemin.store.domain.ReviewImagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewImagesRepository extends JpaRepository<ReviewImagesEntity, Long> {
    //리뷰 아이디로 자식 이미지 일괄 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ReviewImagesEntity ri where ri.review.reviewId = :reviewId")
    int deleteAllByReviewId(@Param("reviewId") Long reviewId);
}
