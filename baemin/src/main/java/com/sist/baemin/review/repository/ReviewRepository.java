package com.sist.baemin.review.repository;

import com.sist.baemin.store.domain.ReviewEntity;
import com.sist.baemin.user.dto.UserReviewDTO;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
    // 가게별 리뷰 조회
    List<ReviewEntity> findByStore_StoreIdOrderByCreatedAtDesc(Long storeId);
    
    // 가게별 리뷰 수 조회
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.store.storeId = :storeId")
    long countByStoreId(@Param("storeId") Long storeId);
    
    // 가게별 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.store.storeId = :storeId")
    Double getAverageRatingByStoreId(@Param("storeId") Long storeId);
    
    // 가게별 별점별 리뷰 수 조회
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.store.storeId = :storeId AND r.rating = :rating")
    long countByStoreIdAndRating(@Param("storeId") Long storeId, @Param("rating") int rating);
    
    // 사용자별 리뷰 수 조회
    @Query("SELECT COUNT(r) FROM ReviewEntity r WHERE r.user.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
    
    // 사용자별 평균 평점 조회
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.user.userId = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);

    //리뷰 내역 조회
    @Query("""
    select new com.sist.baemin.user.dto.UserReviewDTO(
        r.reviewId,
        s.storeName,
        r.rating,
        r.content,
        (select min(ri.imageUrl) from ReviewImagesEntity ri where ri.review = r)
    )
    from ReviewEntity r
      join r.user u
      join r.store s
    where u.email = :email
    order by r.createdAt desc
""")
    List<UserReviewDTO> findUserReviewsByEmail(@Param("email") String email);


    //리뷰 삭제 (이미지 먼저 삭제되어야함)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ReviewEntity r where r.reviewId = :reviewId")
    int deleteByReviewId(@Param("reviewId") Long reviewId);
} 