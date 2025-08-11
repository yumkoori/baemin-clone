package com.sist.baemin.user.service;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;
    public String processKaKaoUserLogin(KaKaoUserInfo userInfo, String kakaoAccessToken) {
        String email = userInfo.getKakao_account().getEmail();
        Long targetId = userInfo.getId();
        String nickname = null;
        
        if (userInfo.getKakao_account() != null &&
            userInfo.getKakao_account().getProfile() != null) {
            nickname = userInfo.getKakao_account().getProfile().getNickname();
        }
        if (nickname == null && userInfo.getProperties() != null) {
            nickname = userInfo.getProperties().getNickname();
        }
        if (nickname == null) {
            nickname = "배민이"; // 기본값
        }
        
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);

        if(userOpt.isPresent()) {
            System.out.println("가입된 회원입니다: " + email);
            // 기존 회원의 정보 업데이트 (닉네임이 변경되었을 수 있음)
            UserEntity existingUser = userOpt.get();
            existingUser.setNickname(nickname);
            userRepository.save(existingUser);
        } else {
            System.out.println("회원가입 처리 시작: " + email);
            UserEntity userEntity = UserEntity.builder()
                    .email(email)
                    .nickname(nickname)
                    .name(nickname) // 실명은 별도 입력받을 때까지 닉네임으로 설정
                    .role("USER")
                    .tier("BRONZE")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            userRepository.save(userEntity);
        }
        return jwtUtil.generateTokenForKaKao(email, targetId, kakaoAccessToken);
    }
    
    // 프로필 조회 (실제 DB 데이터 사용)
    public UserProfileDto getUserProfile(String email) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            return UserProfileDto.builder()
                    .nickname(user.getNickname() != null ? user.getNickname() : "배민이")
                    .realName(user.getName() != null ? user.getName() : user.getNickname()) 
                    .email(user.getEmail())
                    .profileImageUrl(user.getProfileImage() != null ? 
                            user.getProfileImage() : 
                            "https://via.placeholder.com/80x80/00d4aa/white?text=👤")
                    .defaultAddress("서울특별시 강남구 테헤란로 123") // TODO: 실제 주소 엔티티 연동
                    .build();
        }
        throw new RuntimeException("사용자를 찾을 수 없습니다.");
    }
    
    // 프로필 수정 (실제 DB 업데이트)
    public void updateUserProfile(String email, UserProfileUpdateDto updateDto) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            
            // 닉네임 업데이트
            if (updateDto.getNickname() != null && !updateDto.getNickname().trim().isEmpty()) {
                user.setNickname(updateDto.getNickname().trim());
            }
            
            // 실명 업데이트
            if (updateDto.getRealName() != null && !updateDto.getRealName().trim().isEmpty()) {
                user.setName(updateDto.getRealName().trim());
            }
            
            // 이메일 업데이트 (중복 체크 필요)
            if (updateDto.getEmail() != null && !updateDto.getEmail().equals(email)) {
                // 새로운 이메일 중복 체크
                Optional<UserEntity> duplicateUser = userRepository.findByEmail(updateDto.getEmail());
                if (duplicateUser.isPresent()) {
                    throw new RuntimeException("이미 사용 중인 이메일입니다.");
                }
                user.setEmail(updateDto.getEmail());
            }
            
            userRepository.save(user);
            System.out.println("프로필 업데이트 완료: " + user.getEmail());
        } else {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
    }
    
    // 프로필 이미지 업로드 (실제 구현)
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
    
    // 주소 목록 조회 (임시 구현)
    public List<UserAddressDto> getUserAddresses(String email) {
        // TODO: 실제 주소 엔티티에서 조회
        List<UserAddressDto> addresses = new ArrayList<>();
        addresses.add(UserAddressDto.builder()
                .id(1L)
                .alias("집")
                .zipCode("12345")
                .roadAddress("서울특별시 강남구 테헤란로 123")
                .detailAddress("456호")
                .isDefault(true)
                .build());
        return addresses;
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
}
