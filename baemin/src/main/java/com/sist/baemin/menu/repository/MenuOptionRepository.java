package com.sist.baemin.menu.repository;

import com.sist.baemin.menu.domain.MenuOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuOptionRepository extends JpaRepository<MenuOptionEntity, Long> {
    
    // 메뉴별 옵션 조회
    List<MenuOptionEntity> findByMenu_MenuId(Long menuId);
    
    // 메뉴별 옵션 조회 (필수 옵션 우선 정렬)
    List<MenuOptionEntity> findByMenu_MenuIdOrderByIsRequiredDesc(Long menuId);
    
    // 메뉴별 필수 옵션 조회
    List<MenuOptionEntity> findByMenu_MenuIdAndIsRequiredTrue(Long menuId);
    
    // 메뉴별 선택 옵션 조회
    List<MenuOptionEntity> findByMenu_MenuIdAndIsRequiredFalse(Long menuId);
} 