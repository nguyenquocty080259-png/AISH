package com.aish.mvc.service.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

//    private final String SECRET =
//            "aish-secret-key-aish-secret-key";
//
//    public String generateToken(String email){
//
//        return Jwts.builder()
//                .subject(email)
//                .issuedAt(new Date())
//                .expiration(
//                        new Date(
//                                System.currentTimeMillis()
//                                        + 86400000
//                        )
//                )
//                .signWith(
//                        Keys.hmacShaKeyFor(
//                                SECRET.getBytes()
//                        )
//                )
//                .compact();
//    }
    public String generateToken(String email) {
        return "demo-token";
    }
}
