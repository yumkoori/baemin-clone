package com.sist.baemin.store.repository;

import com.sist.baemin.store.domain.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    
    // 가게 상세 조회
    Optional<StoreEntity> findByStoreId(Long storeId);
    
    // 모든 가게 목록 조회
    List<StoreEntity> findAll();
    
    // 가게명으로 검색
    List<StoreEntity> findByStoreNameContaining(String storeName);
    
    // 최소 주문 금액으로 필터링
    List<StoreEntity> findByMinimumPriceLessThanEqual(Integer minimumPrice);
    
    // 배달료로 필터링
    List<StoreEntity> findByDeliveryFeeLessThanEqual(Integer deliveryFee);
} 