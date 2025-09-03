package com.sist.baemin.user.service;

import com.sist.baemin.user.domain.SocialUserEntity;
import com.sist.baemin.user.domain.UserEmailEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.SocialUserRepository;
import com.sist.baemin.user.repository.UserEmailRepository;
import com.sist.baemin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class GoogleOauthService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final SocialUserRepository socialUserRepository;
    private final UserEmailRepository userEmailRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        loginOrJoin(user);
        return user;
    }

    //유저가 있으면 로그인 처리, 없으면 회원가입 처리
    @Transactional
    private void loginOrJoin(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");
        
        // 1) 사용자 조회 또는 생성
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity created = UserEntity.builder()
                            .email(email)
                            .nickname("배민이")
                            .name(name)
                            .role("USER")
                            .tier("BRONZE")
                            .createdAt(java.time.LocalDateTime.now())
                            .build();
                    return userRepository.save(created);
                });

        // 2) 소셜 매핑 업서트
        socialUserRepository.findByProviderAndProviderId("google", providerId)
                .orElseGet(() -> socialUserRepository.save(
                        SocialUserEntity.builder()
                                .user(userEntity)
                                .provider("google")
                                .providerId(providerId)
                                .providerEmail(email)
                                .emailVerified(true)
                                .build()
                ));

        // 3) user_emails 업서트
        userEmailRepository.findByEmail(email)
                .orElseGet(() -> userEmailRepository.save(
                        UserEmailEntity.builder()
                                .user(userEntity)
                                .email(email)
                                .isPrimary(true)
                                .isVerified(true)
                                .sourceIdentityId(null)
                                .build()
                ));
    }


}
