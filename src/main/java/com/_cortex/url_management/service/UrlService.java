package com._cortex.url_management.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com._cortex.url_management.model.*;
import com._cortex.url_management.repository.UrlRepository;
import com._cortex.url_management.util.ShortCodeGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlService {

    private final UrlRepository urlRepository;

    /**
     * Create a shortened URL with auto-generated short code
     * 
     * @param originalUrl the original URL to shorten
     * @param createdBy   the user creating the URL (optional)
     * @param expireAt    the expiration time (optional)
     * @return the created URL
     */
    @Transactional
    public Url createShortUrl(String originalUrl, User createdBy, Instant expireAt) {
        String shortCode = generateUniqueShortCode();

        Url url = new Url();
        url.setShortCode(shortCode);
        url.setOriginalUrl(originalUrl);
        url.setCreatedBy(createdBy);
        url.setExpireAt(expireAt);

        return urlRepository.save(url);
    }

    /**
     * Create a shortened URL with custom short code
     * 
     * @param originalUrl     the original URL to shorten
     * @param customShortCode the custom short code
     * @param createdBy       the user creating the URL (optional)
     * @param expireAt        the expiration time (optional)
     * @return the created URL
     * @throws IllegalArgumentException if short code already exists
     */
    @Transactional
    public Url createCustomShortUrl(String originalUrl, String customShortCode, User createdBy, Instant expireAt) {
        // Check if short code already exists
        if (urlRepository.findByShortCode(customShortCode).isPresent()) {
            throw new IllegalArgumentException("Short code already exists: " + customShortCode);
        }

        Url url = new Url();
        url.setShortCode(customShortCode);
        url.setOriginalUrl(originalUrl);
        url.setCreatedBy(createdBy);
        url.setExpireAt(expireAt);

        return urlRepository.save(url);
    }

    /**
     * Find a URL by short code and increment hit counter
     * 
     * @param shortCode the short code
     * @return Optional containing the URL if found and not expired
     */
    @Transactional
    public Optional<Url> findByShortCodeAndTrack(String shortCode) {
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);

        if (urlOpt.isPresent()) {
            Url url = urlOpt.get();

            // Check if URL has expired
            if (url.getExpireAt() != null && url.getExpireAt().isBefore(Instant.now())) {
                return Optional.empty();
            }

            // Increment hit counter
            urlRepository.incrementHits(shortCode, Instant.now());

            return urlOpt;
        }

        return Optional.empty();
    }

    /**
     * Find a URL by short code without tracking
     * 
     * @param shortCode the short code
     * @return Optional containing the URL if found
     */
    public Optional<Url> findByShortCode(String shortCode) {
        return urlRepository.findByShortCode(shortCode);
    }

    /**
     * Find all URLs created by a user
     * 
     * @param user the user
     * @return list of URLs
     */
    public List<Url> findByUser(User user) {
        return urlRepository.findByCreatedBy(user);
    }

    /**
     * Find all URLs created by a user ID
     * 
     * @param userId the user ID
     * @return list of URLs
     */
    public List<Url> findByUserId(Long userId) {
        return urlRepository.findByCreatedById(userId);
    }

    /**
     * Get most popular URLs
     * 
     * @return list of URLs ordered by hits
     */
    public List<Url> getMostPopularUrls() {
        return urlRepository.findTopByOrderByHitsDesc();
    }

    /**
     * Delete a URL by ID
     * 
     * @param id the URL ID
     */
    @Transactional
    public void deleteUrl(Long id) {
        urlRepository.deleteById(id);
    }

    /**
     * Delete all expired URLs
     * 
     * @return number of deleted URLs
     */
    @Transactional
    public int deleteExpiredUrls() {
        return urlRepository.deleteExpiredUrls(Instant.now());
    }

    /**
     * Generate a unique short code using Base62 encoding
     * 
     * @return a unique short code
     */
    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            shortCode = ShortCodeGenerator.generate();
            attempts++;

            if (attempts >= maxAttempts) {
                throw new RuntimeException("Failed to generate unique short code after " + maxAttempts + " attempts");
            }
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        return shortCode;
    }
}
