package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.service.UserPageService;
import com.sist.baemin.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserPageController {
    @Autowired
    private UserPageService userPageService;
    @Autowired
    private UserService userService;

    // REST API - 프로필 수정
    @PutMapping("/user/profile")
    public ResponseEntity<ResultDto<Void>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserProfileUpdateDto updateDto
    ) {
        try {
            String email = userDetails.getUsername();
            userPageService.updateUserProfile(email, updateDto);
            return ResponseEntity.ok(new ResultDto<>(200, "프로필 수정 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "프로필 수정 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 프로필 이미지 업로드
    @PostMapping("/user/profile-image")
    public ResponseEntity<ResultDto<ProfileImageUploadDto>> uploadProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("profileImage") MultipartFile file
    ) {
        try {
            String email = userDetails.getUsername();
            String imageUrl = userPageService.uploadProfileImage(email, file);
            ProfileImageUploadDto result = new ProfileImageUploadDto(imageUrl);
            return ResponseEntity.ok(new ResultDto<>(200, "이미지 업로드 성공", result));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "이미지 업로드 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 주소 목록 조회
    @GetMapping("/user/addresses")
    public ResponseEntity<ResultDto<List<UserAddressDto>>> getUserAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            String email = userDetails.getUsername();
            List<UserAddressDto> addresses = userPageService.getUserAddresses(email);
            return ResponseEntity.ok(new ResultDto<>(200, "주소 목록 조회 성공", addresses));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "주소 목록 조회 실패: " + e.getMessage(), null));
        }
    }


    // REST API - 개별 주소 조회
    @GetMapping("/user/addresses/{addressId}")
    public ResponseEntity<ResultDto<UserAddressDto>> getUserAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long addressId
    ) {
        try {
            String email = userDetails.getUsername();
            UserAddressDto address = userPageService.getUserAddress(email, addressId);
            return ResponseEntity.ok(new ResultDto<>(200, "주소 조회 성공", address));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "주소 조회 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 주소 추가
    @PostMapping("/user/addresses")
    public ResponseEntity<ResultDto<Void>> addUserAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserAddressCreateDto addressDto
    ) {
        try {
            String email = userDetails.getUsername();
            userPageService.addUserAddress(email, addressDto);
            return ResponseEntity.ok(new ResultDto<>(200, "주소 추가 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "주소 추가 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 주소 수정
    @PutMapping("/user/addresses/{addressId}")
    public ResponseEntity<ResultDto<Void>> updateUserAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long addressId,
            @RequestBody UserAddressCreateDto addressDto
    ) {
        try {
            String email = userDetails.getUsername();
            userPageService.updateUserAddress(email, addressId, addressDto);
            return ResponseEntity.ok(new ResultDto<>(200, "주소 수정 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "주소 수정 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 주소 삭제
    @DeleteMapping("/user/addresses/{addressId}")
    public ResponseEntity<ResultDto<Void>> deleteUserAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long addressId
    ) {
        try {
            String email = userDetails.getUsername();
            userPageService.deleteUserAddress(email, addressId);
            return ResponseEntity.ok(new ResultDto<>(200, "주소 삭제 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "주소 삭제 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 기본 주소 설정
    @PostMapping("/user/addresses/{addressId}/default")
    public ResponseEntity<ResultDto<Void>> setDefaultAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long addressId
    ) {
        try {
            String email = userDetails.getUsername();
            userPageService.setDefaultAddress(email, addressId);
            return ResponseEntity.ok(new ResultDto<>(200, "기본 주소 설정 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "기본 주소 설정 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 리뷰 삭제
    @DeleteMapping("/user/reviews/{reviewId}")
    public ResponseEntity<ResultDto<Void>> deleteUserReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reviewId
    ) {
        try {
            userPageService.removeReview(reviewId);
            return ResponseEntity.ok(new ResultDto<>(200, "리뷰 삭제 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "리뷰 삭제 실패: " + e.getMessage(), null));
        }
    }

    // REST API - 닉네임 수정
    @PutMapping("/user/nickname")
    public ResponseEntity<ResultDto<Void>> updateNickname(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserProfileUpdateDto updateDto
    ) {
        try {
            String email = userDetails.getUsername();
            if (updateDto.getNickname() == null || updateDto.getNickname().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ResultDto<>(400, "닉네임을 입력해주세요.", null));
            }
            userPageService.updateNickName(email, updateDto.getNickname().trim());
            return ResponseEntity.ok(new ResultDto<>(200, "닉네임 수정 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "닉네임 수정 실패: " + e.getMessage(), null));
        }
    }
}
