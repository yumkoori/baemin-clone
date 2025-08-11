package com.sist.baemin.menu.controller;

import com.sist.baemin.menu.dto.MenuOptionResponseDto;
import com.sist.baemin.menu.service.MenuOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu-options")
@RequiredArgsConstructor
public class MenuOptionController {
    
    private final MenuOptionService menuOptionService;
    
    // 메뉴별 모든 옵션 조회
    @GetMapping("/menu/{menuId}")
    public ResponseEntity<List<MenuOptionResponseDto>> getMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
    
    // 메뉴별 사용 가능한 옵션 조회
    @GetMapping("/menu/{menuId}/available")
    public ResponseEntity<List<MenuOptionResponseDto>> getAvailableMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getAvailableMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
    
    // 메뉴별 필수 옵션 조회
    @GetMapping("/menu/{menuId}/required")
    public ResponseEntity<List<MenuOptionResponseDto>> getRequiredMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getRequiredMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
    
    // 메뉴별 선택 옵션 조회
    @GetMapping("/menu/{menuId}/optional")
    public ResponseEntity<List<MenuOptionResponseDto>> getOptionalMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getOptionalMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
} 