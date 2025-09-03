package com.sist.baemin.common.config;

import com.sist.baemin.common.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogoutChainHandler implements LogoutHandler {
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        CookieUtil.deleteCookie(request, response, "Authorization");

        // 3) OAuth2AuthorizedClient 제거(세션/리포지토리 정리)
        if (authentication instanceof OAuth2AuthenticationToken oauth2) {
            String clientId = oauth2.getAuthorizedClientRegistrationId(); // "google"
            authorizedClientService.removeAuthorizedClient(clientId, oauth2.getName());
        }
    }


}
