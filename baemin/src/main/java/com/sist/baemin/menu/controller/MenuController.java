package com.sist.baemin.menu.controller;

import com.sist.baemin.menu.dto.MenuDetailResponseDto;
import com.sist.baemin.menu.dto.MenuResponseDto;
import com.sist.baemin.menu.service.MenuService;
import com.sist.baemin.store.dto.StoreResponseDto;
import com.sist.baemin.store.service.StoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.ArrayList;

@Controller
@RequestMapping("/api")
public class MenuController {
    
    private final MenuService menuService;
    private final StoreService storeService;

    public MenuController(MenuService menuService, StoreService storeService) {
        this.menuService = menuService;
        this.storeService = storeService;
    }
    
    // 메뉴 상세 페이지 (뷰 - HTML 반환)
    @GetMapping("/store/{storeId}/menu/{menuId}")
    public String menuDetail(@PathVariable("storeId") Long storeId, @PathVariable("menuId") Long menuId, Model model) {
        try {
            // DB에서 메뉴 상세 정보 조회 (옵션 포함)
            MenuDetailResponseDto menu = menuService.getMenuDetail(menuId);
            model.addAttribute("menu", menu);
            
            // 가게 정보 조회
            StoreResponseDto store = storeService.getStoreDetail(storeId);
            model.addAttribute("store", store);
            
            // 장바구니 아이템 수
            model.addAttribute("cartItemCount", 0);
            
        } catch (RuntimeException e) {
            // 메뉴를 찾을 수 없는 경우 기본값 설정
            model.addAttribute("menu", null);
            model.addAttribute("store", null);
            model.addAttribute("cartItemCount", 0);
        }
        
        return "menu/menu-detail";
    }

    // 메뉴 상세 정보 API (JSON 반환)
    @GetMapping("/menus/{menuId}")
    @ResponseBody
    public ResponseEntity<MenuDetailResponseDto> getMenuApi(@PathVariable Long menuId) {
        MenuDetailResponseDto menu = menuService.getMenuDetail(menuId);
        return ResponseEntity.ok(menu);
    }

    // 가게별 메뉴 목록 API (JSON 반환)
    @GetMapping("/stores/{storeId}/menus")
    @ResponseBody
    public ResponseEntity<List<MenuResponseDto>> getMenusByStoreApi(@PathVariable Long storeId) {
        List<MenuResponseDto> menus = menuService.getMenusByStore(storeId);
        return ResponseEntity.ok(menus);
    }

    // 메뉴 검색 API (JSON 반환)
    @GetMapping("/menus/search")
    @ResponseBody
    public ResponseEntity<List<MenuResponseDto>> searchMenusApi(@RequestParam Long storeId, @RequestParam String keyword) {
        // 현재는 가게별 메뉴 목록을 반환 (검색 기능은 추후 구현)
        List<MenuResponseDto> menus = menuService.getMenusByStore(storeId);
        return ResponseEntity.ok(menus);
    }
} 