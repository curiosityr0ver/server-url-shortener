package com._cortex.url_management.controller;

import com._cortex.url_management.dto.CreateCustomUrlRequest;
import com._cortex.url_management.dto.CreateUrlRequest;
import com._cortex.url_management.dto.UrlResponse;
import com._cortex.url_management.model.Url;
import com._cortex.url_management.model.User;
import com._cortex.url_management.service.UrlService;
import com._cortex.url_management.service.UserService;
import com._cortex.url_management.util.DtoMapper;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for URL shortening operations
 */
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UrlController {

    private final UrlService urlService;
    private final UserService userService;

    /**
     * Create a shortened URL with auto-generated short code
     * POST /api/urls
     */
    @PostMapping("/api/urls")
    public ResponseEntity<UrlResponse> createShortUrl(@Valid @RequestBody CreateUrlRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));
        }

        Url url = urlService.createShortUrl(
                request.getOriginalUrl(),
                user,
                request.getExpireAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toUrlResponse(url));
    }

    /**
     * Create a shortened URL with custom short code
     * POST /api/urls/custom
     */
    @PostMapping("/api/urls/custom")
    public ResponseEntity<UrlResponse> createCustomShortUrl(@Valid @RequestBody CreateCustomUrlRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userService.findById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));
        }

        Url url = urlService.createCustomShortUrl(
                request.getOriginalUrl(),
                request.getCustomShortCode(),
                user,
                request.getExpireAt());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(DtoMapper.toUrlResponse(url));
    }

    /**
     * Get URL details by short code (without tracking)
     * GET /api/urls/{shortCode}
     */
    @GetMapping("/api/urls/{shortCode}")
    public ResponseEntity<UrlResponse> getUrlDetails(@PathVariable String shortCode) {
        Url url = urlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("URL not found with short code: " + shortCode));

        return ResponseEntity.ok(DtoMapper.toUrlResponse(url));
    }

    /**
     * Redirect to original URL (with hit tracking)
     * GET /{shortCode}
     */
    @GetMapping("/{shortCode}")
    public void redirectToOriginalUrl(@PathVariable String shortCode, HttpServletResponse response) throws IOException {
        Url url = urlService.findByShortCodeAndTrack(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("URL not found or expired: " + shortCode));

        response.sendRedirect(url.getOriginalUrl());
    }

    /**
     * Delete a URL by ID
     * DELETE /api/urls/{id}
     */
    @DeleteMapping("/api/urls/{id}")
    public ResponseEntity<Void> deleteUrl(@PathVariable Long id) {
        urlService.deleteUrl(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all URLs created by a specific user
     * GET /api/users/{userId}/urls
     */
    @GetMapping("/api/users/{userId}/urls")
    public ResponseEntity<List<UrlResponse>> getUserUrls(@PathVariable Long userId) {
        List<Url> urls = urlService.findByUserId(userId);
        List<UrlResponse> responses = urls.stream()
                .map(DtoMapper::toUrlResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get most popular URLs
     * GET /api/urls/stats/popular
     */
    @GetMapping("/api/urls/stats/popular")
    public ResponseEntity<List<UrlResponse>> getPopularUrls() {
        List<Url> urls = urlService.getMostPopularUrls();
        List<UrlResponse> responses = urls.stream()
                .map(DtoMapper::toUrlResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Delete all expired URLs
     * DELETE /api/urls/expired
     */
    @DeleteMapping("/api/urls/expired")
    public ResponseEntity<Integer> deleteExpiredUrls() {
        int deletedCount = urlService.deleteExpiredUrls();
        return ResponseEntity.ok(deletedCount);
    }
}
