package com.sist.baemin.menu.repository;

import com.sist.baemin.menu.domain.MenuOptionValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuOptionValueRepository extends JpaRepository<MenuOptionValueEntity, Long> {
    
    // 메뉴 옵션별 값 조회
    List<MenuOptionValueEntity> findByMenuOption_MenuOptionId(Long menuOptionId);
    
    // 메뉴 옵션별 사용 가능한 값 조회
    List<MenuOptionValueEntity> findByMenuOption_MenuOptionIdAndIsAvailableTrue(Long menuOptionId);
    
    // 메뉴 옵션별 값 조회 (표시 순서대로 정렬)
    List<MenuOptionValueEntity> findByMenuOption_MenuOptionIdOrderByDisplayOrderAsc(Long menuOptionId);
    
    // 메뉴 옵션별 사용 가능한 값 조회 (표시 순서대로 정렬)
    List<MenuOptionValueEntity> findByMenuOption_MenuOptionIdAndIsAvailableTrueOrderByDisplayOrderAsc(Long menuOptionId);
}
