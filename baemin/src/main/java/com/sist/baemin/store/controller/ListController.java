package com.sist.baemin.store.controller;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.service.FoodListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api")
public class ListController {

    @Autowired
    FoodListService foodListService;


    @GetMapping("/list")
    public String main(@RequestParam("category") int category, Model model) {
        List<FoodListDTO> list = foodListService.getFoodList(category);
        model.addAttribute("food", category);
        model.addAttribute("list", list);
        return "html/list";
    }
}
