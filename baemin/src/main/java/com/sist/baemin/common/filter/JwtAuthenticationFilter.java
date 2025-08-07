package com.sist.baemin.common.filter;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtUtil jwtUtil;
        private final UserDetailsService userDetailsService;

        public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
            this.jwtUtil = jwtUtil;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            String token = null;
            String header = request.getHeader("Authorization");

            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
                System.out.println("[JwtFilter] 헤더에서 토큰 추출됨");
            }

            if (token == null && request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    System.out.println("[JwtFilter] 감지된 쿠키: " + cookie.getName() + " = " + cookie.getValue());
                    if ("Authorization".equals(cookie.getName())) {
                        token = cookie.getValue();  // ✅ 수정 포인트
                        System.out.println("[JwtFilter] 쿠키에서 토큰 추출됨");
                        break;
                    }
                }
            }

            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7); // ✅ Bearer 제거
                }

                try {
                    if (jwtUtil.validateToken(token)) {
                        String email = jwtUtil.extractEmail(token);
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        System.out.println("[JwtFilter] 인증된 사용자: " + email);
                    } else {
                        System.out.println("[JwtFilter] 토큰 유효성 검증 실패");
                    }
                } catch (Exception e) {
                    System.out.println("[JwtFilter] 토큰 처리 중 오류: " + e.getMessage());
                    // 인증 실패 시 SecurityContext를 명시적으로 클리어
                    SecurityContextHolder.clearContext();
                }
            }


            filterChain.doFilter(request, response);
        }

    }


