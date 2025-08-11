package com.sist.baemin.menu.service;

import com.sist.baemin.menu.domain.MenuOptionEntity;
import com.sist.baemin.menu.domain.MenuOptionValueEntity;
import com.sist.baemin.menu.dto.MenuOptionResponseDto;
import com.sist.baemin.menu.dto.MenuOptionValueResponseDto;
import com.sist.baemin.menu.repository.MenuOptionRepository;
import com.sist.baemin.menu.repository.MenuOptionValueRepository;
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
    private final MenuOptionValueRepository menuOptionValueRepository;
    
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
        dto.setAdditionalPrice(0); // MenuOptionEntity에는 additionalPrice 필드가 없음 (MenuOptionValueEntity에 있음)
        dto.setIsRequired(option.getIsRequired());
        dto.setIsAvailable(true); // MenuOptionEntity에는 isAvailable 필드가 없으므로 기본값 true
        dto.setIsMultiple(option.getIsMultiple());
        
        // 옵션 값들 변환
        List<MenuOptionValueEntity> optionValues = menuOptionValueRepository
                .findByMenuOption_MenuOptionIdAndIsAvailableTrueOrderByDisplayOrderAsc(option.getMenuOptionId());
        List<MenuOptionValueResponseDto> optionValueDtos = optionValues.stream()
                .map(this::convertToMenuOptionValueResponseDto)
                .collect(Collectors.toList());
        dto.setOptionValues(optionValueDtos);
        
        return dto;
    }
    
    // 옵션 값 Entity를 DTO로 변환
    private MenuOptionValueResponseDto convertToMenuOptionValueResponseDto(MenuOptionValueEntity optionValue) {
        MenuOptionValueResponseDto dto = new MenuOptionValueResponseDto();
        dto.setOptionValueId(optionValue.getMenuOptionValueId());
        dto.setOptionValue(optionValue.getOptionValue());
        dto.setAdditionalPrice(optionValue.getAdditionalPrice());
        dto.setDisplayOrder(optionValue.getDisplayOrder());
        dto.setIsAvailable(optionValue.getIsAvailable());
        return dto;
    }
} 