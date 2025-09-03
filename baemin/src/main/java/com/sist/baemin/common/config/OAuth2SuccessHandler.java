package com.sist.baemin.common.config;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.SocialUserEntity;
import com.sist.baemin.user.domain.UserEmailEntity;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.SocialUserRepository;
import com.sist.baemin.user.repository.UserEmailRepository;
import com.sist.baemin.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofDays(1);
    public static final String REDIRECT_PATH = "/api/main";

    private final JwtUtil jwtUtil;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final UserEmailRepository userEmailRepository;
    private final SocialUserRepository socialUserRepository;
    private final UserRepository userRepository;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 구글 표준 claims
        String provider = "google";
        String providerId = (String) oAuth2User.getAttributes().get("sub");     // 고유 ID
        String email     = (String) oAuth2User.getAttributes().get("email");
        String name      = (String) oAuth2User.getAttributes().get("name");
        String picture   = (String) oAuth2User.getAttributes().get("picture");

        // 1) socialUser(provider, providerId)로 식별 → userId, 없으면 생성
        Long userId = socialUserRepository.findByProviderAndProviderId(provider, providerId)
                .map(SocialUserEntity::getUser)
                .map(UserEntity::getUserId)
                .or(() -> userEmailRepository.findByEmail(email)
                        .map(UserEmailEntity::getUser)
                        .map(UserEntity::getUserId))
                .orElseGet(() -> {
                    // 최초 로그인: 사용자/소셜/이메일 매핑 생성
                    UserEntity user = userRepository.findByEmail(email)
                            .orElseGet(() -> userRepository.save(UserEntity.builder()
                                    .email(email)
                                    .nickname("배민이")
                                    .name(name)
                                    .role("USER")
                                    .tier("BRONZE")
                                    .createdAt(java.time.LocalDateTime.now())
                                    .build()));

                    socialUserRepository.findByProviderAndProviderId(provider, providerId)
                            .orElseGet(() -> socialUserRepository.save(SocialUserEntity.builder()
                                    .user(user)
                                    .provider(provider)
                                    .providerId(providerId)
                                    .providerEmail(email)
                                    .emailVerified(true)
                                    .build()));

                    userEmailRepository.findByEmail(email)
                            .orElseGet(() -> userEmailRepository.save(UserEmailEntity.builder()
                                    .user(user)
                                    .email(email)
                                    .isPrimary(true)
                                    .isVerified(true)
                                    .sourceIdentityId(null)
                                    .build()));

                    return user.getUserId();
                });

        // 3) JWT 발급 (구글/공용 메서드 사용 권장)
        String jwtToken = jwtUtil.generateTokenForGoogle(userId, email);

        // 4) 쿠키 저장
        ResponseCookie jwtCookie = ResponseCookie.from("Authorization", jwtToken)
                .httpOnly(true)
                .secure(false)          // 운영 배포 시 true 권장(HTTPS)
                .path("/")
                .sameSite("Lax")        // 필요시 "None" + secure(true)
                .maxAge(ACCESS_TOKEN_DURATION)
                .build();

        response.setHeader("Set-Cookie", jwtCookie.toString());

        // 5) 인증 요청 쿠키 정리
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 6) 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, REDIRECT_PATH);
    }

}
