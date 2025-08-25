package com.sist.baemin.user.service;

import com.sist.baemin.review.repository.ReviewImagesRepository;
import com.sist.baemin.review.repository.ReviewRepository;
import com.sist.baemin.store.domain.ReviewEntity;
import com.sist.baemin.user.domain.UserAddressEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.repository.UserAddressRepository;
import com.sist.baemin.user.repository.UserCouponRepository;
import com.sist.baemin.user.repository.UserPointRepository;
import com.sist.baemin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserPageService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @Autowired
    private UserCouponRepository userCouponRepository;
    @Autowired
    private UserPointRepository userPointRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReviewImagesRepository reviewImagesRepository;
    @Autowired
    private S3Service s3;

    // 프로필 조회
    public UserProfileDto getUserProfile(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Long couponCount = userCouponRepository.countByUser_EmailAndIsUsed(email, false);
        Long point = userPointRepository.findCurrentPointByUserEmail(email);

        // DB에는 "users/이메일정제/profile/uuid.png" 같은 '키'가 들어있음
        String key = user.getProfileImage();

        // 키를 화면 표시용 URL로 변환 (S3 or 레거시 정적 경로)
        String url = s3.toUrl(key);

        return UserProfileDto.builder()
                .nickname(user.getNickname() != null ? user.getNickname() : "배민이")
                .realName(user.getName() != null ? user.getName() : user.getNickname())
                .email(user.getEmail())
                // placeholder는 서비스에서 넣지 말고, 템플릿의 th:src 엘비스 연산자로 처리
                .profileImageUrl(url)
                .couponCount(couponCount)
                .point(point)
                .build();
    }

    // 프로필 수정
    public void updateUserProfile(String email, UserProfileUpdateDto updateDto) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();

            // 닉네임 업데이트
            if (updateDto.getNickname() != null && !updateDto.getNickname().trim().isEmpty()) {
                user.setNickname(updateDto.getNickname().trim());
            }

            userRepository.save(user);
            System.out.println("프로필 업데이트 완료: " + user.getEmail());
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }

    // 프로필 이미지 업로드
    public String uploadProfileImage(String email, MultipartFile file) {
        try {
            // 1. 파일 유효성 검사
            if (file.isEmpty()) {
                throw new RuntimeException("업로드할 파일이 없습니다.");
            }

            // 2. 파일 확장자 검사
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                throw new RuntimeException("파일명이 올바르지 않습니다.");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            if (!extension.matches("\\.(jpg|jpeg|png)$")) {
                throw new RuntimeException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png만 가능)");
            }

            // 3. 파일 크기 검사 (5MB 제한)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("파일 크기는 5MB를 초과할 수 없습니다.");
            }

            // 4. 업로드 디렉토리 생성 (절대 경로 사용)
            String projectRoot = System.getProperty("user.dir");
            String uploadDir = projectRoot + "/uploads/profile/";
            java.io.File directory = new java.io.File(uploadDir);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                System.out.println("디렉토리 생성: " + uploadDir + " -> " + created);
            }

            // 5. 고유한 파일명 생성 (timestamp + UUID)
            String timestamp = String.valueOf(System.currentTimeMillis());
            String uuid = java.util.UUID.randomUUID().toString().substring(0, 8);
            String newFilename = "profile_" + timestamp + "_" + uuid + extension;

            // 6. 파일 저장
            String filePath = uploadDir + newFilename;
            java.io.File destinationFile = new java.io.File(filePath);
            System.out.println("파일 저장 경로: " + filePath);

            // 파일 저장 전 디렉토리 재확인
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            file.transferTo(destinationFile);

            // 파일 저장 확인
            if (!destinationFile.exists()) {
                throw new RuntimeException("파일 저장에 실패했습니다.");
            }

            System.out.println("파일 저장 성공: " + destinationFile.getAbsolutePath());

            // 7. 웹에서 접근 가능한 URL 생성
            String imageUrl = "/uploads/profile/" + newFilename;

            // 8. DB에 이미지 URL 저장
            Optional<UserEntity> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();

                // 기존 프로필 이미지 파일 삭제 (기본 이미지가 아닌 경우)
                String oldImageUrl = user.getProfileImage();
                if (oldImageUrl != null && oldImageUrl.startsWith("/uploads/")) {
                    try {
                        String oldFilePath = projectRoot + oldImageUrl;
                        java.io.File oldFile = new java.io.File(oldFilePath);
                        if (oldFile.exists()) {
                            boolean deleted = oldFile.delete();
                            System.out.println("기존 프로필 이미지 삭제: " + oldFilePath + " -> " + deleted);
                        }
                    } catch (Exception e) {
                        System.out.println("기존 파일 삭제 실패: " + e.getMessage());
                    }
                }

                user.setProfileImage(imageUrl);
                userRepository.save(user);
            }

            System.out.println("프로필 이미지 업로드 성공: " + email + " -> " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.out.println("프로필 이미지 업로드 실패: " + e.getMessage());
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 주소 목록 조회
    public List<UserAddressDto> getUserAddresses(String email) {
        List<UserAddressEntity> addressEntityList = userAddressRepository.findByUser_Email(email);

        if(!addressEntityList.isEmpty()) {
             return addressEntityList.stream()
                    .map(entity -> UserAddressDto.builder()
                            .id(entity.getAddressId())
                            .alias(entity.getAddressName())
                            .zipCode(entity.getZipCode())
                            .roadAddress(entity.getRoadAddress())
                            .detailAddress(entity.getDetailAddress())
                            .isDefault(entity.isDefault())
                            .build())
                    .toList();
        } else {
            return null;
        }
    }

    // 개별 주소 조회 (임시 구현)
    public UserAddressDto getUserAddress(String email, Long addressId) {
        // TODO: 실제 주소 엔티티에서 조회
        return UserAddressDto.builder()
                .id(addressId)
                .alias("집")
                .zipCode("12345")
                .roadAddress("서울특별시 강남구 테헤란로 123")
                .detailAddress("456호")
                .isDefault(true)
                .build();
    }

    // 주소 추가 (임시 구현)
    public void addUserAddress(String email, UserAddressCreateDto addressDto) {
        // TODO: 실제 주소 엔티티 저장
        System.out.println("주소 추가: " + addressDto.toString());
    }

    // 주소 수정 (임시 구현)
    public void updateUserAddress(String email, Long addressId, UserAddressCreateDto addressDto) {
        // TODO: 실제 주소 엔티티 수정
        System.out.println("주소 수정: " + addressId + " -> " + addressDto.toString());
    }

    // 주소 삭제 (임시 구현)
    public void deleteUserAddress(String email, Long addressId) {
        // TODO: 실제 주소 엔티티 삭제
        System.out.println("주소 삭제: " + addressId);
    }

    // 기본 주소 설정 (임시 구현)
    public void setDefaultAddress(String email, Long addressId) {
        // TODO: 실제 기본 주소 설정
        System.out.println("기본 주소 설정: " + addressId);
    }

    // 비밀번호 변경 (임시 구현)
    public void changePassword(String email, PasswordChangeDto passwordDto) {
        // TODO: 실제 비밀번호 변경 로직 (BCrypt 등)
        System.out.println("비밀번호 변경: " + email);
    }

    // 소셜 계정 연동 해제 (임시 구현)
    public void unlinkSocialAccount(String email, String provider) {
        // TODO: 실제 소셜 연동 해제 로직
        System.out.println("소셜 연동 해제: " + provider + " for " + email);
    }

    // 회원 탈퇴 (임시 구현)
    public void deleteAccount(String email) {
        // TODO: 실제 회원 탈퇴 로직 (soft delete)
        System.out.println("회원 탈퇴: " + email);
    }

    //리뷰 내역 조회
    public List<UserReviewDTO> getReviewsWithEmail(String email) {
        List<UserReviewDTO> reviews = reviewRepository.findUserReviewsByEmail(email);
        return reviews;
    }

    //리뷰 삭제
    @Transactional
    public void removeReview(Long reviewId) {
        reviewImagesRepository.deleteAllByReviewId(reviewId);
        reviewRepository.deleteByReviewId(reviewId);
    }

    //닉네임 수정
    public void updateNickName(String email, String nickName) {
        Optional<UserEntity> user = userRepository.findByEmail(email);

        if(user.isPresent()) {
            UserEntity userEntity = user.get();
            userEntity.setNickname(nickName);

            userRepository.save(userEntity);
        } else {
            System.out.println("유저가 존재하지 않습니다.");
        }
    }
}
