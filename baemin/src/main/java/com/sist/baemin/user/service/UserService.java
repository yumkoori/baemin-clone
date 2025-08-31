package com.sist.baemin.user.service;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.*;
import com.sist.baemin.user.dto.*;
import com.sist.baemin.user.repository.SocialUserRepository;
import com.sist.baemin.user.repository.UserAddressRepository;
import com.sist.baemin.user.repository.UserEmailRepository;
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
    private SocialUserRepository socialUserRepository;
    @Autowired
    private UserEmailRepository userEmailRepository;
    @Autowired
    private UserAddressRepository userAddressRepository;
    @Autowired
    private JwtUtil jwtUtil;
    
    public String processKaKaoUserLogin(KaKaoUserInfo userInfo, String kakaoAccessToken) {
        String email = userInfo.getKakao_account().getEmail();
        Long targetId = userInfo.getId();

        Optional<UserEntity> userOpt = socialUserRepository.findKakaoUserByProviderEmail(email);

        Long userId;

        if(userOpt.isPresent()) {
            System.out.println("가입된 회원입니다: " + email);
            UserEntity existingUser = userOpt.get();
            userId = existingUser.getUserId();
        } else {
            System.out.println("회원가입 처리 시작: " + email);
            UserEntity userEntity = UserEntity.builder()
                    .email(email)
                    .nickname("배민이")
                    .name(userInfo.getProperties().getNickname())
                    .role("USER")
                    .tier("BRONZE")
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            UserEntity savedUserEntity = userRepository.save(userEntity);
            userId = savedUserEntity.getUserId();

            SocialUserEntity socialUserEntity = SocialUserEntity.builder()
                    .user(userEntity)
                    .provider("kakao")
                    .providerId(targetId.toString())
                    .providerEmail(email)
                    .emailVerified(true)
                    .build();

            SocialUserEntity savedSocialUser = socialUserRepository.save(socialUserEntity);

            UserEmailEntity userEmailEntity = UserEmailEntity.builder()
                    .user(userEntity)
                    .email(email)
                    .isPrimary(true)
                    .isVerified(true)
                    .sourceIdentityId(savedSocialUser.getSocialUserId())
                    .build();

            userEmailRepository.save(userEmailEntity);

        }
        return jwtUtil.generateTokenForKaKao(userId, targetId, kakaoAccessToken);
    }
    
    // 사용자 이름을 기반으로 UserEntity를 조회하는 메소드
    public UserEntity findByUsername(String username) {
        System.out.println("=== UserService.findByUsername() called with username: " + username + " ===");
        // 여기서는 email을 username으로 사용합니다.
        // 실제 애플리케이션에서는 사용자 이름을 기반으로 조회하는 로직이 필요합니다.
        // 예를 들어, UserEntity에 username 필드가 있다면, userRepository.findByUsername(username)을 사용할 수 있습니다.
        // 현재는 email을 기반으로 조회합니다.
        UserEntity user = userRepository.findByEmail(username).orElse(null);
        System.out.println("User found by email: " + user);
        System.out.println("=== UserService.findByUsername() completed ===");
        return user;
    }
    
    // 사용자 ID를 기반으로 UserEntity를 조회하는 메소드
    public UserEntity findByUserId(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}
