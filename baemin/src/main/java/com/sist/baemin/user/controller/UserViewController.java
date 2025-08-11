package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.service.UserPageService;
import com.sist.baemin.user.service.UserService;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api")
public class UserViewController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserPageService userPageService;

    @GetMapping("/login")
    public String loginPage() {
        return "html/login";
    }

    //마이페이지 조회
    @GetMapping("/mypage")
    public String getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
            if(userDetails == null) {
                return "/html/mypage";
            }

            String email = userDetails.getUsername();
            UserProfileDto profile = userPageService.getUserProfile(email);

            model.addAttribute("user", profile);

            System.out.println(profile);

            return "/html/mypage";
    }

    @GetMapping("/mypage/profile")
    public String mypageProfile(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            UserProfileDto profile = userPageService.getUserProfile(email);
            model.addAttribute("user", profile);
        }
        return "/html/mypage-profile";
    }

    @GetMapping("/mypage/profile/nickname")
    public String mypageEditNickname(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            UserProfileDto profile = userPageService.getUserProfile(email);
            model.addAttribute("user", profile);
        }
        return "/html/mypage-nickname";
    }

    @GetMapping("/mypage/reviews")
    public String mypageReviews(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getUsername();
            List<UserReviewDTO> reviews = userPageService.getReviewsWithEmail(email);
            model.addAttribute("reviews", reviews);
        } else {
            model.addAttribute("reviews", java.util.Collections.emptyList());
        }
        return "/html/mypage-reviews";
    }

    @GetMapping("/mypage/address")
    public String mypageAddress(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            List<UserAddressDto> userAddresses = userPageService.getUserAddresses(userDetails.getUser().getEmail());
            if (userAddresses == null) {
                userAddresses = java.util.Collections.emptyList();
            } else {
                System.out.println("유저 주소");
                System.out.println(userAddresses);
                userAddresses = userAddresses.stream()
                        .sorted(java.util.Comparator.comparing(UserAddressDto::isDefault).reversed())
                        .toList();
            }
            model.addAttribute("addresses", userAddresses);
        } else {
            model.addAttribute("addresses", java.util.Collections.emptyList());
        }
        return "/html/mypage-address";
    }


}
