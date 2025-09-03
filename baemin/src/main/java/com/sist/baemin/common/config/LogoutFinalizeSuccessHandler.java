package com.sist.baemin.common.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@RequiredArgsConstructor
public class LogoutFinalizeSuccessHandler implements LogoutSuccessHandler {

    private final OAuth2AuthorizedClientService clientService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1) 구글 액세스 토큰 조회
        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            String regId = oauth2.getAuthorizedClientRegistrationId(); // 보통 "google"
            OAuth2AuthorizedClient client =
                    clientService.loadAuthorizedClient(regId, oauth2.getName());

            if (client != null) {
                OAuth2AccessToken accessToken = client.getAccessToken();
                if (accessToken != null && accessToken.getTokenValue() != null) {
                    String tokenToRevoke = accessToken.getTokenValue();

                    // 2) 구글 토큰 철회 (리프레시 토큰이 없어도 액세스 토큰으로 가능)
                    try {
                        var uri = URI.create("https://oauth2.googleapis.com/revoke?token=" + tokenToRevoke);
                        HttpClient.newHttpClient().send(
                                HttpRequest.newBuilder(uri)
                                        .POST(HttpRequest.BodyPublishers.noBody())
                                        .build(),
                                HttpResponse.BodyHandlers.discarding()
                        );
                    } catch (Exception e) {
                        // 철회 실패해도 로컬 로그아웃은 이미 처리됨. 필요 시 로그만 남기세요.
                    }
                }
            }
        }
        response.sendRedirect("/");
    }
}
