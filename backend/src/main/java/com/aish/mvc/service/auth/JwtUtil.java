package com.aish.mvc.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET = "aish-secret-key-aish-secret-key-256bit!!";

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Tạo token từ email
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .signWith(getKey())
                .compact();
    }

    // Tạo token từ email
    public String creteToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .signWith(getKey())
                .compact();
    }

    // Lấy toàn bộ Claims
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Lấy email từ token
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // Lấy role
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    // Kiểm tra token còn hạn không
    public boolean isTokenValid(String token) {
        try {
            extractUsername(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}