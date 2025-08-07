package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.dto.ArchSampleResponseDto;
import com.sist.baemin.user.dto.UserDTO;
import com.sist.baemin.user.service.KaKaoOauthService;
import com.sist.baemin.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/oauth")
    public String kaKaoLogin(@RequestParam("code") String code, Model model) {
        try {
            String accessToken = kaKaoOauthService.getAccessToken(code);
            System.out.println(accessToken);

            KaKaoUserInfo userInfo = kaKaoOauthService.getUserInfo(accessToken);
            String jwtToken = userService.processKaKaoUserLogin(userInfo);

            return "redirect:/api/main?jwtToken=" + jwtToken + "&accessToken=" + accessToken;

        } catch (Exception e) {
            // 에러 발생시 login.html로 에러 메시지와 함께 리다이렉트
            String errorMessage = URLEncoder.encode("카카오 로그인에 실패했습니다: " + e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/api/login?error=" + errorMessage;
        }
    }

}
