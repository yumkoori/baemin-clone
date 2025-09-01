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
    void getReviewsWithUserId() {
        Long userId = 7L;
        List<UserReviewDTO> reviews = userPageService.getReviewsWithUserId(userId);
        System.out.println(reviews);
    }

    @Test
    void getUserAddresses() {
        Long userId = 7L;
        List<UserAddressDto> userAddresses = userPageService.getUserAddresses(userId);
        System.out.println(userAddresses);
    }
}