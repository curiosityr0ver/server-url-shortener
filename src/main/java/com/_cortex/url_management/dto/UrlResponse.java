package com._cortex.url_management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for URL information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlResponse {

    private Long id;

    private String shortCode;

    private String shortUrl;

    private String originalUrl;

    private Long createdByUserId;

    private String createdByUsername;

    private Instant createdAt;

    private Instant lastAccessedAt;

    private Instant expireAt;

    private Long hits;
}
