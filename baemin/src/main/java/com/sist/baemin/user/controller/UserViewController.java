package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.dto.ProfileImageUploadDto;
import com.sist.baemin.user.dto.UserProfileDto;
import com.sist.baemin.user.dto.UserProfileUpdateDto;
import com.sist.baemin.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserViewController {

    @Autowired
    private UserService userService;

    @GetMapping("/api/login")
    public String loginPage() {
        return "html/login";
    }
    
    @GetMapping("/api/mypage")
    public String myPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        try {
            if (userDetails != null) {
                String email = userDetails.getUsername();
                // UserService에서 실제 사용자 정보 가져오기
                UserProfileDto user = userService.getUserProfile(email);
                model.addAttribute("user", user);
            } else {
                // 로그인되지 않은 경우 로그인 페이지로 리다이렉트
                return "redirect:/api/login";
            }
            
            return "html/mypage";
        } catch (Exception e) {
            System.out.println("마이페이지 로드 오류: " + e.getMessage());
            return "redirect:/api/login";
        }
    }
    
    // REST API - 프로필 조회
    @GetMapping("/api/user/profile")
    @ResponseBody
    public ResponseEntity<ResultDto<UserProfileDto>> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            String email = userDetails.getUsername();
            UserProfileDto profile = userService.getUserProfile(email);
            return ResponseEntity.ok(new ResultDto<>(200, "프로필 조회 성공", profile));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "프로필 조회 실패: " + e.getMessage(), null));
        }
    }
    
    // REST API - 프로필 수정
    @PutMapping("/api/user/profile")
    @ResponseBody
    public ResponseEntity<ResultDto<Void>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserProfileUpdateDto updateDto
    ) {
        try {
            String email = userDetails.getUsername();
            userService.updateUserProfile(email, updateDto);
            return ResponseEntity.ok(new ResultDto<>(200, "프로필 수정 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "프로필 수정 실패: " + e.getMessage(), null));
        }
    }
    
    // REST API - 프로필 이미지 업로드
    @PostMapping("/api/user/profile-image")
    @ResponseBody
    public ResponseEntity<ResultDto<ProfileImageUploadDto>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("profileImage") MultipartFile file
    ) {
        try {
            String email = userDetails.getUsername();
            String imageUrl = userService.uploadProfileImage(email, file);
            ProfileImageUploadDto result = new ProfileImageUploadDto(imageUrl);
            return ResponseEntity.ok(new ResultDto<>(200, "이미지 업로드 성공", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "이미지 업로드 실패: " + e.getMessage(), null));
        }
    }
}
