package com._cortex.url_management.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com._cortex.url_management.model.Url;
import com._cortex.url_management.model.User;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    /**
     * Find a URL by its short code
     * 
     * @param shortCode the short code to search for
     * @return Optional containing the URL if found
     */
    Optional<Url> findByShortCode(String shortCode);

    /**
     * Find all URLs created by a specific user
     * 
     * @param user the user who created the URLs
     * @return list of URLs created by the user
     */
    List<Url> findByCreatedBy(User user);

    /**
     * Find all URLs created by a specific user ID
     * 
     * @param userId the ID of the user
     * @return list of URLs created by the user
     */
    List<Url> findByCreatedById(Long userId);

    /**
     * Find all URLs that have expired
     * 
     * @param now the current timestamp
     * @return list of expired URLs
     */
    @Query("SELECT u FROM Url u WHERE u.expireAt IS NOT NULL AND u.expireAt < :now")
    List<Url> findExpiredUrls(@Param("now") Instant now);

    /**
     * Increment the hit counter for a URL
     * 
     * @param shortCode  the short code of the URL
     * @param accessTime the time of access
     */
    @Modifying
    @Query("UPDATE Url u SET u.hits = u.hits + 1, u.lastAccessedAt = :accessTime WHERE u.shortCode = :shortCode")
    void incrementHits(@Param("shortCode") String shortCode, @Param("accessTime") Instant accessTime);

    /**
     * Find top N most visited URLs
     * 
     * @return list of most visited URLs ordered by hits
     */
    @Query("SELECT u FROM Url u ORDER BY u.hits DESC")
    List<Url> findTopByOrderByHitsDesc();

    /**
     * Delete all expired URLs
     * 
     * @param now the current timestamp
     * @return number of deleted URLs
     */
    @Modifying
    @Query("DELETE FROM Url u WHERE u.expireAt IS NOT NULL AND u.expireAt < :now")
    int deleteExpiredUrls(@Param("now") Instant now);
}
