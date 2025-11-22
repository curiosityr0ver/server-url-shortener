package com._cortex.url_management.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;
        String errorMessage = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7).trim();
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("Successfully extracted username from JWT: {}", username);
            } catch (ExpiredJwtException e) {
                errorMessage = "JWT token has expired";
                logger.warn("JWT token has expired: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                errorMessage = "JWT token is malformed";
                logger.warn("JWT token is malformed: {}", e.getMessage());
            } catch (UnsupportedJwtException e) {
                errorMessage = "JWT token is unsupported";
                logger.warn("JWT token is unsupported: {}", e.getMessage());
            } catch (SignatureException e) {
                errorMessage = "JWT signature validation failed";
                logger.warn("JWT signature validation failed: {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                errorMessage = "JWT claims string is empty";
                logger.warn("JWT claims string is empty: {}", e.getMessage());
            } catch (Exception e) {
                errorMessage = "Error processing JWT token";
                logger.error("Error extracting username from JWT: {}", e.getMessage(), e);
            }
        }

        // If there's an error with the token, return 401 Unauthorized
        if (errorMessage != null && jwt != null) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                logger.debug("Loading user details for username: {}", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                boolean isValid = jwtUtil.validateToken(jwt, userDetails);
                logger.debug("Token validation result for user {}: {}", username, isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    logger.debug("Authentication set successfully for user: {}", username);
                } else {
                    logger.warn("Token validation failed for user: {}", username);
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }
            } catch (org.springframework.security.core.userdetails.UsernameNotFoundException e) {
                logger.warn("User not found: {}", username);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                return;
            } catch (Exception e) {
                logger.error("Error loading user details for {}: {}", username, e.getMessage(), e);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("status", String.valueOf(status));
        
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
