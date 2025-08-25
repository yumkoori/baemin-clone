package com.sist.baemin.common.filter;

import com.sist.baemin.common.util.JwtUtil;
import com.sist.baemin.user.domain.CustomUserDetails;
import com.sist.baemin.user.domain.UserEntity;
import com.sist.baemin.user.repository.UserRepository;
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
        private final UserRepository userRepository;

        public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
            this.jwtUtil = jwtUtil;
            this.userRepository = userRepository;
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
            }

            if (token == null && request.getCookies() != null) {
                for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                    if ("Authorization".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }

            if (token != null) {
                if (token.startsWith("Bearer ")) {
                    token = token.substring(7);
                }

                try {
                    if (jwtUtil.validateToken(token)) {
                        Long userId = jwtUtil.extractUserId(token);
                        if (userId != null) {
                            UserEntity user = userRepository.findById(userId).orElse(null);
                            if (user != null) {
                                UserDetails userDetails = new CustomUserDetails(user);
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        }

                    } else {
                    }
                } catch (Exception e) {
                    System.out.println("[JwtFilter] 토큰 처리 중 오류: " + e.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }


            filterChain.doFilter(request, response);
        }

    }


