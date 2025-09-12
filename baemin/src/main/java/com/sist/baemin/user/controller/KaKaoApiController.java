package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.dto.KaKaoUnlinkRequestDto;
import com.sist.baemin.user.service.KaKaoOauthService;
import com.sist.baemin.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Controller
@RequestMapping("/api")
public class KaKaoApiController {
    @Autowired
    private KaKaoOauthService kaKaoOauthService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/oauth")
    public String kaKaoLogin(@RequestParam("code") String code, Model model, HttpServletRequest request, HttpServletResponse response) {
        try {
            String scheme = request.getHeader("X-Forwarded-Proto");
            if (scheme == null || scheme.isBlank()) scheme = request.getScheme();
            String host = request.getHeader("X-Forwarded-Host");
            if (host == null || host.isBlank()) host = request.getHeader("Host");
            String baseUrl = scheme + "://" + host;
            String redirectUri = baseUrl + "/api/oauth";

            String accessToken = kaKaoOauthService.getAccessToken(code, redirectUri);
            KaKaoUserInfo userInfo = kaKaoOauthService.getUserInfo(accessToken);
            String jwtToken = userService.processKaKaoUserLogin(userInfo, accessToken);

            ResponseCookie jwtCookie = ResponseCookie.from("Authorization", jwtToken)
                    .httpOnly(true)     // XSS 공격 방어 - JavaScript에서 접근 불가
                    .secure(false)      // 개발환경용 (운영환경에서는 true)
                    .path("/")
                    .sameSite("Lax")    // CSRF 공격 방어
                    .maxAge(60 * 60)    // 1시간
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());

            boolean hasAddress = userService.userHasAnyAddress(jwtUtil.extractUserId(jwtToken));
            return hasAddress ? "redirect:/api/main" : "redirect:/api/onboarding/address";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode("카카오 로그인에 실패했습니다: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/api/login?error=" + errorMessage;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResultDto<Void>> logout(
            HttpServletResponse response,
            @CookieValue(value = "Authorization", required = false) String jwtToken
    ) {
        return performLogout(response, jwtToken);
    }

    @GetMapping("/logout")
    public String logoutFromKakao(
            HttpServletResponse response,
            @CookieValue(value = "Authorization", required = false) String jwtToken
    ) {
        System.out.println("카카오에서 리다이렉트된 로그아웃 처리");
        performLogout(response, jwtToken);
        return "redirect:/api/login";
    }

    private ResponseEntity<ResultDto<Void>> performLogout(
            HttpServletResponse response,
            String jwtToken
    ) {
        try {
            System.out.println("로그아웃 처리 시작");
            
            // 카카오 로그아웃 처리 (토큰이 있는 경우)
            if (jwtToken != null && !jwtToken.isEmpty()) {
                try {
                    String kakaoAccessToken = jwtUtil.extractClaimAsString(jwtToken, "kakao_access_token");
                    if (kakaoAccessToken != null && !kakaoAccessToken.isEmpty()) {
                        // 카카오 로그아웃 API 호출은 선택사항 (실패해도 로그아웃 진행)
                        kaKaoOauthService.logoutKakaoUser(kakaoAccessToken);
                        System.out.println("카카오 토큰 로그아웃 완료");
                    }
                } catch (Exception e) {
                    System.out.println("⚠카카오 로그아웃 실패 (계속 진행): " + e.getMessage());
                }
            }

            // JWT 쿠키 삭제
            System.out.println("JWT 쿠키 삭제");
            ResponseCookie jwtCookie = ResponseCookie.from("Authorization", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0) // 즉시 만료
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());
            System.out.println("로그아웃 처리 완료");

            return ResponseEntity.ok(new ResultDto<>(200, "로그아웃 성공", null));
        } catch (Exception e) {
            System.out.println("로그아웃 실패: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "로그아웃 실패: " + e.getMessage(), null));
        }
    }

    @PostMapping("/oauth/kaKao-unlink")
    public ResponseEntity<ResultDto<Void>> unlinkKakaoAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @CookieValue(value = "Authorization", required = false) String jwtToken
    ) {
        try {
            if (jwtToken == null || jwtToken.isEmpty()) {
                return ResponseEntity.status(401)
                        .body(new ResultDto<>(401, "인증 토큰이 없습니다", null));
            }

            // 인증된 사용자 정보에서 이메일 가져오기
            Long userId = userDetails.getUserId();
            
            // JWT 토큰에서 필요한 정보 추출
            String targetId = jwtUtil.extractClaimAsString(jwtToken, "target_id");
            String kakaoAccessToken = jwtUtil.extractClaimAsString(jwtToken, "kakao_access_token");

            System.out.println("연결 해제 요청 - 사용자: " + userId);
            System.out.println("Target ID: " + targetId);

            kaKaoOauthService.unlinkKakaoAccount(kakaoAccessToken, targetId);

            return ResponseEntity.ok(new ResultDto<>(200, "카카오 연결 해제 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "카카오 연결 해제 실패: " + e.getMessage(), null));
        }
    }



}
