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
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class KaKaoApiController {
    @Autowired
    private KaKaoOauthService kaKaoOauthService;

    @Autowired
    private UserService userService;

    @GetMapping("/oauth")
    public ResponseEntity<ResultDto<UserDTO>> login(@RequestParam("code") String code) {
        String accessToken = kaKaoOauthService.getAccessToken(code);

        System.out.println(accessToken);

        KaKaoUserInfo userInfo = kaKaoOauthService.getUserInfo(accessToken);

        UserDTO user = userService.processKaKaoUserLogin(userInfo);

        ResultDto<UserDTO> result = new ResultDto<>(200, "user 로그인 완료", user);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
