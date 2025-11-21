package com._cortex.url_management.util;

import com._cortex.url_management.dto.UrlResponse;
import com._cortex.url_management.dto.UserResponse;
import com._cortex.url_management.model.Url;
import com._cortex.url_management.model.User;

/**
 * Utility class for mapping between entities and DTOs
 */
public class DtoMapper {

    private DtoMapper() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert User entity to UserResponse DTO
     */
    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        return response;
    }

    /**
     * Convert Url entity to UrlResponse DTO
     */
    public static UrlResponse toUrlResponse(Url url, String baseUrl) {
        if (url == null) {
            return null;
        }

        UrlResponse response = new UrlResponse();
        response.setId(url.getId());
        response.setShortCode(url.getShortCode());
        response.setShortUrl(baseUrl + "/" + url.getShortCode());
        response.setOriginalUrl(url.getOriginalUrl());
        response.setCreatedAt(url.getCreatedAt());
        response.setLastAccessedAt(url.getLastAccessedAt());
        response.setExpireAt(url.getExpireAt());
        response.setHits(url.getHits());

        if (url.getCreatedBy() != null) {
            response.setCreatedByUserId(url.getCreatedBy().getId());
            response.setCreatedByUsername(url.getCreatedBy().getUsername());
        }

        return response;
    }
}
