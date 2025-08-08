package com.sist.baemin.store.controller;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.service.FilterFoodListService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@Log4j2
public class FilterFoodListController {

    @Autowired
    FilterFoodListService filterFoodListService;
    @GetMapping("/api/restaurants")
    public List<FoodListDTO> filterList (@RequestParam("filter") String filterType, @RequestParam("category") int category
                                         , Model model){
        log.info("😒😒😒😒😒😒😒😒😒😒" + filterType);
        return filterFoodListService.filterFoodList(filterType, category);
    }
}
