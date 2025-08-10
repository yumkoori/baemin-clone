package com.sist.baemin.store.service;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.repository.SearchWordRepoitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchKeyWordService {

    @Autowired
    SearchWordRepoitory searchWordRepoitory;
    public List<FoodListDTO> getKeyWord(String keyword){

        return searchWordRepoitory.searchKeyWord(keyword);
    }
}
