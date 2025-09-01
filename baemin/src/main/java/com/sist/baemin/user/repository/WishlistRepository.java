package com.sist.baemin.user.repository;

import com.sist.baemin.user.domain.WishlistsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistsEntity, Long> {
    
    @Query("SELECT w FROM WishlistsEntity w WHERE w.user.userId = :userId")
    List<WishlistsEntity> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT w FROM WishlistsEntity w WHERE w.user.userId = :userId AND w.store.storeId = :storeId")
    Optional<WishlistsEntity> findByUserIdAndStoreId(@Param("userId") Long userId, @Param("storeId") Long storeId);
}