package com.sist.baemin.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeliveryController {

    @GetMapping("/delivery")
    public String delivery(Model model) {
        // 추후 배달 페이지에 필요한 데이터를 model에 추가할 수 있습니다
        return "html/delivery";
    }

    @GetMapping("/takeout")
    public String takeout(Model model) {
        // 추후 포장 페이지에 필요한 데이터를 model에 추가할 수 있습니다
        return "html/takeout";
    }

    @GetMapping("/shopping-live")
    public String shoppingLive(Model model) {
        // 추후 쇼핑라이브 페이지에 필요한 데이터를 model에 추가할 수 있습니다
        return "html/shopping-live";
    }
}
