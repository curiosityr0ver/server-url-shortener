package com._cortex.url_management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Request DTO for creating a shortened URL with custom short code
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomUrlRequest {

    @NotBlank(message = "Original URL is required")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String originalUrl;

    @NotBlank(message = "Custom short code is required")
    @Size(min = 3, max = 20, message = "Short code must be between 3 and 20 characters")
    @Pattern(regexp = "^[0-9A-Za-z]+$", message = "Short code must contain only alphanumeric characters")
    private String customShortCode;

    private Long userId;

    private Instant expireAt;
}
