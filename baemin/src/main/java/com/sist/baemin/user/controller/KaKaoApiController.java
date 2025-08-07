package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.dto.KaKaoUnlinkRequestDto;
import com.sist.baemin.user.service.KaKaoOauthService;
import com.sist.baemin.user.service.UserService;
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
    public String kaKaoLogin(@RequestParam("code") String code, Model model, HttpServletResponse response) {
        try {
            String accessToken = kaKaoOauthService.getAccessToken(code);
            KaKaoUserInfo userInfo = kaKaoOauthService.getUserInfo(accessToken);
            String jwtToken = userService.processKaKaoUserLogin(userInfo);


            ResponseCookie jwtCookie = ResponseCookie.from("Authorization", jwtToken)  // ✅ 공백 없음
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(60 * 60)
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());

            // user가 null일 수 있으므로 userInfo에서 직접 이메일 가져오기
            String email = userInfo.getKakao_account().getEmail();
            model.addAttribute("email", email);

            return "redirect:/api/main?jwtToken=" + jwtToken + "&accessToken=" + accessToken;

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode("카카오 로그인에 실패했습니다: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/api/login?error=" + errorMessage;
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ResultDto<Void>> logout(HttpServletResponse response) {
        try {
            // JWT 쿠키 삭제
            ResponseCookie jwtCookie = ResponseCookie.from("Authorization", "")
                    .httpOnly(false)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(0) // 즉시 만료
                    .build();

            response.setHeader("Set-Cookie", jwtCookie.toString());

            return ResponseEntity.ok(new ResultDto<>(200, "로그아웃 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "로그아웃 실패: " + e.getMessage(), null));
        }
    }

    @PostMapping("/oauth/kaKao-unlink")
    public ResponseEntity<ResultDto<Void>> unlinkKakaoAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody KaKaoUnlinkRequestDto requestDto
    ) {
        try {
            // 방법 1: 인증된 사용자 정보에서 이메일 가져오기
            String email = userDetails.getUsername();
            
            // 방법 2: JWT 토큰에서 target_id 추출
            String token = authHeader.replace("Bearer ", "");
            String targetId = jwtUtil.extractClaimAsString(token, "target_id");

            System.out.println("연결 해제 요청 - 사용자: " + email);
            System.out.println("Target ID: " + targetId);

            kaKaoOauthService.unlinkKakaoAccount(requestDto.getKakaoAccessToken(), targetId);

            return ResponseEntity.ok(new ResultDto<>(200, "카카오 연결 해제 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ResultDto<>(500, "카카오 연결 해제 실패: " + e.getMessage(), null));
        }
    }

}
