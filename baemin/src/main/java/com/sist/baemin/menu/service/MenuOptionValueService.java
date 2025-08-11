package com.sist.baemin.menu.service;

import com.sist.baemin.menu.domain.MenuOptionValueEntity;
import com.sist.baemin.menu.dto.MenuOptionValueResponseDto;
import com.sist.baemin.menu.repository.MenuOptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuOptionValueService {
    
    private final MenuOptionValueRepository menuOptionValueRepository;
    
    // 메뉴 옵션별 모든 값 조회
    public List<MenuOptionValueResponseDto> getMenuOptionValues(Long menuOptionId) {
        List<MenuOptionValueEntity> optionValues = menuOptionValueRepository
                .findByMenuOption_MenuOptionIdOrderByDisplayOrderAsc(menuOptionId);
        return optionValues.stream()
                .map(this::convertToMenuOptionValueResponseDto)
                .collect(Collectors.toList());
    }
    
    // 메뉴 옵션별 사용 가능한 값 조회
    public List<MenuOptionValueResponseDto> getAvailableMenuOptionValues(Long menuOptionId) {
        List<MenuOptionValueEntity> optionValues = menuOptionValueRepository
                .findByMenuOption_MenuOptionIdAndIsAvailableTrueOrderByDisplayOrderAsc(menuOptionId);
        return optionValues.stream()
                .map(this::convertToMenuOptionValueResponseDto)
                .collect(Collectors.toList());
    }
    
    // 옵션 값 Entity 조회
    public MenuOptionValueEntity getMenuOptionValueEntityById(Long optionValueId) {
        return menuOptionValueRepository.findById(optionValueId).orElse(null);
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
