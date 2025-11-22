package com._cortex.url_management;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    public static void main(String[] args) {
        String secret = "ThisIsAVerySecretKeyForJWTTokenThatShouldBeAtLeast32BytesLong!!";
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        System.out.println("Secret length: " + keyBytes.length + " bytes");

        // Create token
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("testuser")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 10)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("Generated token: " + token);

        // Verify token
        try {
            Claims parsedClaims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("Token verified successfully!");
            System.out.println("Subject: " + parsedClaims.getSubject());
        } catch (Exception e) {
            System.err.println("Token verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
