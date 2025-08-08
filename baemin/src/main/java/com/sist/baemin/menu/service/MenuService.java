package com.sist.baemin.menu.service;

import com.sist.baemin.menu.domain.MenuEntity;
import com.sist.baemin.menu.domain.MenuOptionEntity;
import com.sist.baemin.menu.dto.MenuDetailResponseDto;
import com.sist.baemin.menu.dto.MenuOptionResponseDto;
import com.sist.baemin.menu.dto.MenuResponseDto;
import com.sist.baemin.menu.repository.MenuOptionRepository;
import com.sist.baemin.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MenuService {
    
    private final MenuRepository menuRepository;
    private final MenuOptionRepository menuOptionRepository;
    
    // 가게별 메뉴 목록 조회
    public List<MenuResponseDto> getMenusByStore(Long storeId) {
        List<MenuEntity> menus = menuRepository.findByStore_StoreIdAndIsAvailableTrueOrderByMenuNameAsc(storeId);
        return menus.stream()
                .map(this::convertToMenuResponseDto)
                .collect(Collectors.toList());
    }
    
    // 카테고리별 메뉴 목록 조회 (현재 MenuEntity에 categoryId가 없으므로 임시로 가게별 조회 사용)
    public List<MenuResponseDto> getMenusByCategory(Long storeId, Long categoryId) {
        List<MenuEntity> menus = menuRepository.findByStore_StoreIdAndIsAvailableTrueOrderByMenuNameAsc(storeId);
        return menus.stream()
                .map(this::convertToMenuResponseDto)
                .collect(Collectors.toList());
    }
    
    // 메뉴 상세 조회 (옵션 포함)
    public MenuDetailResponseDto getMenuDetail(Long menuId) {
        MenuEntity menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("메뉴를 찾을 수 없습니다."));
        
        // 옵션 조회 (예외 처리 강화)
        List<MenuOptionEntity> options = new ArrayList<>();
        try {
            options = menuOptionRepository.findByMenu_MenuId(menuId);
            log.info("메뉴 ID {}의 옵션 {}개 조회 성공", menuId, options.size());
        } catch (Exception e) {
            log.warn("메뉴 ID {}의 옵션 조회 실패: {}", menuId, e.getMessage());
            options = new ArrayList<>();
        }
        
        return convertToMenuDetailResponseDto(menu, options);
    }
    
    // 메뉴 Entity 조회
    public MenuEntity getMenuEntityById(Long menuId) {
        return menuRepository.findById(menuId).orElse(null);
    }
    
    // Entity를 DTO로 변환
    private MenuResponseDto convertToMenuResponseDto(MenuEntity menu) {
        MenuResponseDto dto = new MenuResponseDto();
        dto.setMenuId(menu.getMenuId());
        dto.setMenuName(menu.getMenuName());
        dto.setDescription(menu.getDescription());
        dto.setPrice(menu.getPrice());
        dto.setImageUrl(menu.getImageUrl());
        dto.setIsAvailable(menu.getIsAvailable());
        dto.setStoreId(null); // StoreEntity에 storeId 필드가 없으므로 임시로 null
        dto.setCategoryId(null); // MenuEntity에는 categoryId 필드가 없음
        return dto;
    }
    
    // Entity를 상세 DTO로 변환
    private MenuDetailResponseDto convertToMenuDetailResponseDto(MenuEntity menu, List<MenuOptionEntity> options) {
        MenuDetailResponseDto dto = new MenuDetailResponseDto();
        dto.setMenuId(menu.getMenuId());
        dto.setMenuName(menu.getMenuName());
        dto.setDescription(menu.getDescription());
        dto.setPrice(menu.getPrice());
        dto.setImageUrl(menu.getImageUrl());
        dto.setIsAvailable(menu.getIsAvailable());
        dto.setStoreId(null); // StoreEntity에 storeId 필드가 없으므로 임시로 null
        dto.setCategoryId(null); // MenuEntity에는 categoryId 필드가 없음
        
        // HTML 템플릿에서 사용되는 추가 필드들 설정
        dto.setIsPopular(false); // 기본값 false
        dto.setIsRecommended(false); // 기본값 false
        dto.setReviewCount(0); // 기본값 0
        
        // 옵션 변환 (안전성 강화)
        List<MenuOptionResponseDto> optionDtos = new ArrayList<>();
        if (options != null) {
            optionDtos = options.stream()
                    .map(this::convertToMenuOptionResponseDto)
                    .collect(Collectors.toList());
        }
        dto.setOptions(optionDtos);
        
        return dto;
    }
    
    // 옵션 Entity를 DTO로 변환
    private MenuOptionResponseDto convertToMenuOptionResponseDto(MenuOptionEntity option) {
        MenuOptionResponseDto dto = new MenuOptionResponseDto();
        dto.setOptionId(option.getMenuOptionId());
        dto.setOptionName(option.getOptionName());
        dto.setDescription(""); // MenuOptionEntity에는 description 필드가 없음
        dto.setAdditionalPrice(option.getAdditionalPrice());
        dto.setIsRequired(option.getIsRequired());
        dto.setIsAvailable(true); // MenuOptionEntity에는 isAvailable 필드가 없으므로 기본값 true
        return dto;
    }
} 