package com.sist.baemin.menu.service;

import com.sist.baemin.menu.domain.MenuOptionEntity;
import com.sist.baemin.menu.dto.MenuOptionResponseDto;
import com.sist.baemin.menu.repository.MenuOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuOptionService {
    
    private final MenuOptionRepository menuOptionRepository;
    
    // 메뉴별 모든 옵션 조회
    public List<MenuOptionResponseDto> getMenuOptions(Long menuId) {
        List<MenuOptionEntity> options = menuOptionRepository.findByMenu_MenuIdOrderByIsRequiredDesc(menuId);
        return options.stream()
                .map(this::convertToMenuOptionResponseDto)
                .collect(Collectors.toList());
    }
    
    // 메뉴별 사용 가능한 옵션 조회 (MenuOptionEntity에 isAvailable 필드가 없으므로 모든 옵션 반환)
    public List<MenuOptionResponseDto> getAvailableMenuOptions(Long menuId) {
        List<MenuOptionEntity> options = menuOptionRepository.findByMenu_MenuIdOrderByIsRequiredDesc(menuId);
        return options.stream()
                .map(this::convertToMenuOptionResponseDto)
                .collect(Collectors.toList());
    }
    
    // 메뉴별 필수 옵션 조회
    public List<MenuOptionResponseDto> getRequiredMenuOptions(Long menuId) {
        List<MenuOptionEntity> options = menuOptionRepository.findByMenu_MenuIdAndIsRequiredTrue(menuId);
        return options.stream()
                .map(this::convertToMenuOptionResponseDto)
                .collect(Collectors.toList());
    }
    
    // 메뉴별 선택 옵션 조회
    public List<MenuOptionResponseDto> getOptionalMenuOptions(Long menuId) {
        List<MenuOptionEntity> options = menuOptionRepository.findByMenu_MenuIdAndIsRequiredFalse(menuId);
        return options.stream()
                .map(this::convertToMenuOptionResponseDto)
                .collect(Collectors.toList());
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