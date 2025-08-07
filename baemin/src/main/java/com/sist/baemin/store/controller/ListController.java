package com.sist.baemin.store.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ListController {

    @GetMapping("/list")
    public String main(@Param("category") int category, Model model) {
        model.addAttribute("food", category);

        return "views/list";
    }
}
