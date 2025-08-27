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


}
