package com.sist.baemin.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String main(Model model) {
        // 추후 메인 페이지에 필요한 데이터를 model에 추가할 수 있습니다
        return "html/main";
    }
} 