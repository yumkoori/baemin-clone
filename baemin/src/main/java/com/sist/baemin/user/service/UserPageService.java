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

    public UserProfileDto getUserProfile(Long userId) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);

        System.out.println("프로필 유저 정보 조회............");
        System.out.println(userOpt);

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();

            // 유저 쿠폰 개수 조회
            Long couponCount = userCouponRepository.countByUser_UserIdAndIsUsed(userId, false);

            // 유저 포인트 양 조회
            Long point = userPointRepository.findCurrentPointByUserId(userId);

            return UserProfileDto.builder()
                    .nickname(user.getNickname() != null ? user.getNickname() : "배민이")
                    .realName(user.getName() != null ? user.getName() : user.getNickname())
                    .email(user.getEmail())
                    .profileImageUrl(user.getProfileImage() != null ?
                            user.getProfileImage() :
                            "https://via.placeholder.com/80x80/00d4aa/white?text=👤")
                    .couponCount(couponCount)
                    .point(point)
                    .build();
        }
        throw new RuntimeException("사용자를 찾을 수 없습니다.");
    }

    // 프로필 수정
    public void updateUserProfile(Long userId, UserProfileUpdateDto updateDto) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
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
    public String uploadProfileImage(Long userId, MultipartFile file) {
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
            Optional<UserEntity> userOpt = userRepository.findById(userId);
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

            System.out.println("프로필 이미지 업로드 성공: " + (userOpt.map(UserEntity::getEmail).orElse("unknown")) + " -> " + imageUrl);
            return imageUrl;

        } catch (Exception e) {
            System.out.println("프로필 이미지 업로드 실패: " + e.getMessage());
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 주소 목록 조회
    public List<UserAddressDto> getUserAddresses(Long userId) {
        List<UserAddressEntity> addressEntityList = userAddressRepository.findByUser_UserId(userId);

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
    public UserAddressDto getUserAddress(Long userId, Long addressId) {
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
    public void addUserAddress(Long userId, UserAddressCreateDto addressDto) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        UserEntity user = userOpt.get();

        // 기존 주소 조회
        List<UserAddressEntity> existing = userAddressRepository.findByUser_UserId(userId);
        // 최초 주소이거나(isEmpty) 사용자가 기본으로 체크한 경우, 이번 주소를 기본으로 설정
        boolean shouldBeDefault = addressDto.isDefault() || (existing == null || existing.isEmpty());
        if (shouldBeDefault && existing != null && !existing.isEmpty()) {
            // 기존 기본 주소 해제
            for (UserAddressEntity e : existing) {
                if (e.isDefault()) {
                    e.setDefault(false);
                }
            }
            userAddressRepository.saveAll(existing);
        }

        UserAddressEntity entity = new UserAddressEntity();
        entity.setUser(user);
        entity.setAddressName(addressDto.getAlias());
        entity.setZipCode(addressDto.getZipCode());
        entity.setRoadAddress(addressDto.getRoadAddress());
        entity.setDetailAddress(addressDto.getDetailAddress());
        entity.setDefault(shouldBeDefault);
        // 위경도 저장 (null 허용)
        entity.setLatitude(addressDto.getLatitude());
        entity.setLongitude(addressDto.getLongitude());

        userAddressRepository.save(entity);
    }

    // 주소 수정 (대표 주소 처리 포함)
    @Transactional
    public void updateUserAddress(Long userId, Long addressId, UserAddressCreateDto addressDto) {
        UserAddressEntity entity = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다."));

        if (entity.getUser() == null || !userId.equals(entity.getUser().getUserId())) {
            throw new RuntimeException("해당 주소에 대한 권한이 없습니다.");
        }

        // 필드 업데이트
        if (addressDto.getAlias() != null) entity.setAddressName(addressDto.getAlias());
        if (addressDto.getZipCode() != null) entity.setZipCode(addressDto.getZipCode());
        if (addressDto.getRoadAddress() != null) entity.setRoadAddress(addressDto.getRoadAddress());
        entity.setDetailAddress(addressDto.getDetailAddress());
        entity.setLatitude(addressDto.getLatitude());
        entity.setLongitude(addressDto.getLongitude());

        // 기본 주소 체크 시: 기존 기본 해제 → 현재 주소만 기본으로
        if (addressDto.isDefault()) {
            List<UserAddressEntity> list = userAddressRepository.findByUser_UserId(userId);
            for (UserAddressEntity e : list) {
                if (e.isDefault()) {
                    e.setDefault(false);
                }
            }
            if (!list.isEmpty()) userAddressRepository.saveAll(list);
            entity.setDefault(true);
        }

        userAddressRepository.save(entity);
    }

    // 주소 삭제 (구현)
    @Transactional
    public void deleteUserAddress(String email, Long addressId) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        Long userId = userOpt.get().getUserId();

        // 삭제 대상 주소를 조회하여 기본주소 여부 확인
        Optional<UserAddressEntity> targetOpt = userAddressRepository.findById(addressId);
        if (targetOpt.isEmpty() || !userId.equals(targetOpt.get().getUser().getUserId())) {
            throw new RuntimeException("삭제할 주소가 없거나 권한이 없습니다.");
        }
        boolean wasDefault = targetOpt.get().isDefault();

        // 삭제
        userAddressRepository.deleteByAddressIdAndUser_UserId(addressId, userId);

        // 기본 주소를 삭제한 경우, 남은 주소 중 하나를 기본으로 승격
        if (wasDefault) {
            List<UserAddressEntity> remain = userAddressRepository.findByUser_UserId(userId);
            if (remain != null && !remain.isEmpty()) {
                UserAddressEntity first = remain.get(0);
                if (!first.isDefault()) {
                    first.setDefault(true);
                    userAddressRepository.save(first);
                }
            }
        }
    }

    // 기본 주소 설정 (한 개만 대표)
    @Transactional
    public void setDefaultAddress(String email, Long addressId) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        Long userId = userOpt.get().getUserId();

        UserAddressEntity target = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("주소를 찾을 수 없습니다."));
        if (!userId.equals(target.getUser().getUserId())) {
            throw new RuntimeException("해당 주소에 대한 권한이 없습니다.");
        }

        // 1) 모든 주소의 기본값 해제
        List<UserAddressEntity> list = userAddressRepository.findByUser_UserId(userId);
        for (UserAddressEntity e : list) {
            if (e.isDefault()) {
                e.setDefault(false);
            }
        }
        if (!list.isEmpty()) {
            userAddressRepository.saveAll(list);
        }

        // 2) 대상 주소만 기본으로 설정
        target.setDefault(true);
        userAddressRepository.save(target);
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
    public List<UserReviewDTO> getReviewsWithUserId(Long userId) {
        List<UserReviewDTO> reviews = reviewRepository.findUserReviewsByUserId(userId);
        return reviews;
    }

    //리뷰 삭제
    @Transactional
    public void removeReview(Long reviewId) {
        reviewImagesRepository.deleteAllByReviewId(reviewId);
        reviewRepository.deleteByReviewId(reviewId);
    }

    //닉네임 수정
    public void updateNickName(Long userId, String nickName) {
        Optional<UserEntity> user = userRepository.findById(userId);

        if(user.isPresent()) {
            UserEntity userEntity = user.get();
            userEntity.setNickname(nickName);

            userRepository.save(userEntity);
        } else {
            System.out.println("유저가 존재하지 않습니다.");
        }
    }
}
