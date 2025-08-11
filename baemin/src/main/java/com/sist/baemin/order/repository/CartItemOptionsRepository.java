package com.sist.baemin.order.repository;

import com.sist.baemin.order.domain.CartItemOptionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemOptionsRepository extends JpaRepository<CartItemOptionsEntity, Long> {
    
    List<CartItemOptionsEntity> findByCartItemCartItemId(Long cartItemId);
    
    void deleteByCartItemCartItemId(Long cartItemId);
    
    /**
     * 특정 메뉴 옵션에 대해 기존에 사용된 모든 옵션 값들을 중복 제거하여 조회
     */
    @Query("SELECT DISTINCT mov.optionValue FROM CartItemOptionsEntity cio JOIN cio.menuOptionValue mov WHERE cio.menuOption.menuOptionId = :menuOptionId ORDER BY mov.displayOrder")
    List<String> findDistinctOptionValuesByMenuOptionId(@Param("menuOptionId") Long menuOptionId);
    
    /**
     * 동일한 옵션명을 가진 모든 메뉴 옵션들에서 사용된 옵션 값들을 중복 제거하여 조회
     */
    @Query("SELECT DISTINCT mov.optionValue FROM CartItemOptionsEntity cio JOIN cio.menuOptionValue mov WHERE cio.menuOption.optionName = :optionName ORDER BY mov.displayOrder")
    List<String> findDistinctOptionValuesByOptionName(@Param("optionName") String optionName);
}