package com.sist.baemin.store.controller;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.service.SearchKeyWordService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Log4j2
public class SearchKeywordController {

    @Autowired
    SearchKeyWordService searchKeyWordService;
    @GetMapping("api/search")
    public List<FoodListDTO> searchKeyWord(@RequestParam("keyword") String keyword, Model model){

        log.info("searchKeyWordsearchKeyWordsearchKeyWordsearchKeyWord");
        return searchKeyWordService.getKeyWord(keyword);

    }
}
