package com.sist.baemin.menu.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.menu.dto.MenuOptionResponseDto;
import com.sist.baemin.menu.service.MenuOptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MenuOptionController {
    
    private final MenuOptionService menuOptionService;
    
    // 메뉴별 모든 옵션 조회
    @GetMapping("/{menuId}/options")
    public ResponseEntity<ResultDto<List<MenuOptionResponseDto>>> getMenuOptions(@PathVariable Long menuId) {
        log.info("메뉴 옵션 조회 요청 - menuId: {}", menuId);
        
        try {
            List<MenuOptionResponseDto> options = menuOptionService.getMenuOptions(menuId);
            log.info("메뉴 옵션 조회 완료 - menuId: {}, 옵션 수: {}", menuId, options.size());
            
            return ResponseEntity.ok(new ResultDto<>(200, "메뉴 옵션 조회 성공", options));
        } catch (Exception e) {
            log.error("메뉴 옵션 조회 실패 - menuId: {}, error: {}", menuId, e.getMessage(), e);
            return ResponseEntity.status(500).body(new ResultDto<>(500, "메뉴 옵션 조회 실패: " + e.getMessage(), null));
        }
    }
    
    // 메뉴별 사용 가능한 옵션 조회
    @GetMapping("/{menuId}/options/available")
    public ResponseEntity<List<MenuOptionResponseDto>> getAvailableMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getAvailableMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
    
    // 메뉴별 필수 옵션 조회
    @GetMapping("/{menuId}/options/required")
    public ResponseEntity<List<MenuOptionResponseDto>> getRequiredMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getRequiredMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
    
    // 메뉴별 선택 옵션 조회
    @GetMapping("/{menuId}/options/optional")
    public ResponseEntity<List<MenuOptionResponseDto>> getOptionalMenuOptions(@PathVariable Long menuId) {
        List<MenuOptionResponseDto> options = menuOptionService.getOptionalMenuOptions(menuId);
        return ResponseEntity.ok(options);
    }
} 