package com._cortex.url_management.controller;

import com._cortex.url_management.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class JwtTestController {

    private final JwtUtil jwtUtil;

    @GetMapping("/jwt")
    public ResponseEntity<Map<String, String>> testJwt(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();

        try {
            String username = jwtUtil.extractUsername(token);
            response.put("status", "success");
            response.put("username", username);
            response.put("message", "Token validated successfully");

            // Also check full validation
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("dummy")
                    .authorities(new ArrayList<>())
                    .build();

            boolean isValid = jwtUtil.validateToken(token, userDetails);
            response.put("isValid", String.valueOf(isValid));

        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getClass().getName());
            response.put("message", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/generate-and-validate")
    public ResponseEntity<Map<String, Object>> generateAndValidate() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Generate a token
            UserDetails userDetails = User.builder()
                    .username("testuser")
                    .password("dummy")
                    .authorities(new ArrayList<>())
                    .build();

            String token = jwtUtil.generateToken(userDetails);
            response.put("token", token);
            System.out.println("Generated token: " + token);

            // Immediately validate it
            String extractedUsername = jwtUtil.extractUsername(token);
            response.put("extractedUsername", extractedUsername);
            System.out.println("Extracted username: " + extractedUsername);

            boolean isValid = jwtUtil.validateToken(token, userDetails);
            response.put("isValid", isValid);
            System.out.println("Is valid: " + isValid);

            response.put("status", "success");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("error", e.getClass().getName());
            response.put("message", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(response);
    }
}
