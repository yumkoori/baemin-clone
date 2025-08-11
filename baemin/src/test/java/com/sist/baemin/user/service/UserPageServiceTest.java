package com.sist.baemin.user.service;

import com.sist.baemin.user.dto.UserAddressDto;
import com.sist.baemin.user.dto.UserReviewDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserPageServiceTest {

    @Autowired
    private UserPageService userPageService;

    @Test
    void getReviewsWithEmail() {
        String email = "yumik00@naver.com";
        List<UserReviewDTO> reviews = userPageService.getReviewsWithEmail(email);
        System.out.println(reviews);
    }

    @Test
    void getUserAddresses() {
        String email = "yumik00@naver.com";
        List<UserAddressDto> userAddresses = userPageService.getUserAddresses(email);
        System.out.println(userAddresses);
    }
}