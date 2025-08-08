package com.sist.baemin.store.service;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.repository.FoodListRepoitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FoodListService {
    @Autowired
    FoodListRepoitory foodListRepoitory;

    public List<FoodListDTO> getFoodList(int category){
        return foodListRepoitory.getBasicFoodList(category);
    }

}
