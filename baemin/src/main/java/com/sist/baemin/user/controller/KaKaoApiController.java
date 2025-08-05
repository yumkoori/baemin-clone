package com.sist.baemin.user.controller;

import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.user.dto.ArchSampleResponseDto;
import com.sist.baemin.user.service.KaKaoOauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class KaKaoApiController {
    @Autowired
    private KaKaoOauthService kaKaoOauthService;

    @GetMapping("/oauth")
    public ResponseEntity<ResultDto<String>> login(@RequestParam("code") String code) {
        String accessToken = kaKaoOauthService.getAccessToken(code);

        System.out.println(accessToken);

        String userInfo = kaKaoOauthService.getUserInfo(accessToken);
        ResultDto<String> result = new ResultDto<>(200, "user 정보조회 완료", userInfo);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

}
