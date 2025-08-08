package com.sist.baemin.menu.repository;

import com.sist.baemin.menu.domain.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    
    // 가게별 메뉴 조회
    List<MenuEntity> findByStore_StoreIdOrderByMenuNameAsc(Long storeId);
    
    // 가게별 사용 가능한 메뉴 조회
    List<MenuEntity> findByStore_StoreIdAndIsAvailableTrueOrderByMenuNameAsc(Long storeId);
    
    // 메뉴 상세 조회 (가게 정보 포함)
    @Query("SELECT m FROM MenuEntity m WHERE m.menuId = :menuId")
    Optional<MenuEntity> findMenuWithStore(@Param("menuId") Long menuId);
} 