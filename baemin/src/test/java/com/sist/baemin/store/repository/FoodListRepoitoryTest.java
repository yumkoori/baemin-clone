//package com.sist.baemin.store.repository;
//
//import com.sist.baemin.store.dto.FoodListDTO;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
//
//@SpringBootTest
//@AutoConfigureTestDatabase(replace = NONE)
//class FoodListRepoitoryTest {
//
//    @Autowired
//    private FoodListRepoitory foodListRepoitory;
//
//    @Test
//    void FoodListTest() {
//        // given
//        int category = 1;
//
//        // when
//        List<FoodListDTO> result = foodListRepoitory.getBasicFoodList(category);
//
//        // then
//        assertNotNull(result, "결과가 null입니다.");
//        assertFalse(result.isEmpty(), "검색 결과가 비어 있습니다.");
//
//        // 디버깅 출력
//        result.forEach(dto -> {
//            System.out.println("가게명: " + dto.getStoreName());
//            System.out.println("별점: " + dto.getRating());
//            System.out.println("리뷰 수: " + dto.getReviewCount());
//            System.out.println("최소 주문 금액: " + dto.getMinimumPrice());
//            System.out.println("배달비: " + dto.getDeliveryFee());
//            System.out.println("----");
//        });
//    }
//}