package com.sist.baemin.user.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.baemin.user.domain.KaKaoUserInfo;
import com.sist.baemin.user.dto.KaKaoTokenResponse;
import com.sist.baemin.user.dto.KakaoUserResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class KaKaoOauthService {
    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");                     //숨김 작업 필요
        params.add("client_id", "9332367d804b05aa4921d0ddd1c788cb");
        params.add("redirect_uri", "http://localhost:8080/api/oauth");
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        String jsonBody = restTemplate.postForEntity(
                "https://kauth.kakao.com/oauth/token", request, String.class
        ).getBody();


        System.out.println(jsonBody);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            KaKaoTokenResponse tokenResponse = objectMapper.readValue(jsonBody, KaKaoTokenResponse.class);

            System.out.println(tokenResponse);

            return tokenResponse.getAccess_token();
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();

        }

        return null;
    }

    public KaKaoUserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                String.class
        );

        System.out.println(response.getBody());

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            KaKaoUserInfo userInfo = objectMapper.readValue(response.getBody(), KaKaoUserInfo.class);

            System.out.println("파싱된 유저 정보: " + userInfo);
            return userInfo;

        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }
        return KaKaoUserInfo.builder().build();
    }

    public void unlinkKakaoAccount(String kakaoAccessToken, String targetId) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("target_id_type", "user_id");
        body.add("target_id", targetId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://kapi.kakao.com/v1/user/unlink",
                request,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("카카오 연결 해제 실패: " + response.getBody());
        }
    }

}
