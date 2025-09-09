package com.sist.baemin.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.baemin.common.filter.JwtAuthenticationFilter;
import com.sist.baemin.common.response.ResultDto;
import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.repository.SocialUserRepository;
import com.sist.baemin.user.repository.UserEmailRepository;
import com.sist.baemin.user.service.CustomUserDetailsService;
import com.sist.baemin.user.repository.UserRepository;
import com.sist.baemin.user.service.GoogleOauthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserEmailRepository userEmailRepository;
    @Autowired
    private SocialUserRepository socialUserRepository;
    @Autowired
    private GoogleOauthService googleOauthService;

    @Autowired
    private LogoutChainHandler logoutChainHandler;
    @Autowired
    private LogoutFinalizeSuccessHandler logoutSuccess;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtUtil, userRepository);

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 허용
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // 기본 페이지 허용
                        .requestMatchers("/", "/login", "/main", "/html/**").permitAll()
                        
                        // 장바구니 페이지 허용 (인증은 컨트롤러에서 처리)
                        .requestMatchers("/api/cart/page").permitAll()
                        
                        // 메뉴 관련 조회 API는 허용 (순서 중요!)
                        .requestMatchers("/api/menu/**").permitAll()
                        .requestMatchers("/api/menus/**").permitAll()
                        .requestMatchers("/api/store/**").permitAll()
                        .requestMatchers("/api/stores/**").permitAll()
                        
                        // 장바구니 관련 API는 인증 필요
                        .requestMatchers("/api/cart/**").authenticated()
                        
                        // 나머지 API는 허용 (조회용)
                        .requestMatchers("/api/**").permitAll()

                        // 나머지는 인증 필요 (예: /admin 등)
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2.loginPage("/api/login")
                        .authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint
                                .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository())
                        )
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(googleOauthService))
                        .successHandler(oAuth2SuccessHandler())
                )

                .logout(l -> l
                        .logoutUrl("/logout")
                        .addLogoutHandler(logoutChainHandler)
                        .logoutSuccessHandler(logoutSuccess)
                        .deleteCookies("ACCESS_TOKEN", "REFRESH_TOKEN") // 1) 클라이언트 쿠키 삭제
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 인증 실패 시 커스텀 응답
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            
                            ResultDto<Object> errorResponse = new ResultDto<>(401, "로그인이 필요한 서비스입니다.", null);
                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // 권한 없음 시 커스텀 응답  
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            
                            ResultDto<Object> errorResponse = new ResultDto<>(403, "접근 권한이 없습니다.", null);
                            ObjectMapper mapper = new ObjectMapper();
                            response.getWriter().write(mapper.writeValueAsString(errorResponse));
                        })
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

   @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
   }

   @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userRepository);
   }

   @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtUtil, oAuth2AuthorizationRequestBasedOnCookieRepository(), userEmailRepository, socialUserRepository, userRepository);
   }
}
