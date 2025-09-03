package com.sist.baemin.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("your-256-bit-secret-your-256-bit-secret".getBytes());

    private final long EXPIRATION_TIME = 1000 * 60 * 60;

    public String generateTokenForKaKao(Long userId, Long targetId, String kakaoAccessToken) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))                         // userId 기반
                .setIssuedAt(new Date())                     // 생성 시간
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 만료 시간
                .claim("target_id", String.valueOf(targetId))           //카카오 타겟 아이디
                .claim("kakao_access_token", kakaoAccessToken)          //카카오 액세스 토큰
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 서명
                .compact();                                  // 문자열 반환
    }

    public String generateTokenForGoogle(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("email", email)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Long extractUserId(String token) {
        String subject = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody().getSubject();
        return subject != null ? Long.parseLong(subject) : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractClaimAsString(String token, String key) {
        Object claim = getAllClaims(token).get(key);
        return claim != null ? claim.toString() : null;
    }

}
