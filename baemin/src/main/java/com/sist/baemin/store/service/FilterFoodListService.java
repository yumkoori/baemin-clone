package com.sist.baemin.store.service;

import com.sist.baemin.store.dto.FoodListDTO;
import com.sist.baemin.store.repository.FilterFoodListRepoitory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilterFoodListService {

    @Autowired
    FilterFoodListRepoitory filterFoodListRepoitory;

    public List<FoodListDTO> filterFoodList(String filterType, int category){
        switch (filterType) {
            case "rating":
            return filterFoodListRepoitory.filterRatingFoodList(category);
        }
        return null;
    }
}
