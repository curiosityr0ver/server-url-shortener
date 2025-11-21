package com._cortex.url_management.service;

import com._cortex.url_management.model.Url;
import com._cortex.url_management.model.User;
import com._cortex.url_management.repository.UrlRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @InjectMocks
    private UrlService urlService;

    private User testUser;
    private Url testUrl;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testUrl = new Url();
        testUrl.setId(1L);
        testUrl.setShortCode("abc123");
        testUrl.setOriginalUrl("https://example.com");
        testUrl.setCreatedBy(testUser);
        testUrl.setHits(0L);
    }

    @Test
    void testCreateShortUrl_Success() {
        // Arrange
        String originalUrl = "https://example.com";
        when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        // Act
        Url result = urlService.createShortUrl(originalUrl, testUser, null);

        // Assert
        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void testCreateShortUrl_WithExpiration() {
        // Arrange
        String originalUrl = "https://example.com";
        Instant expireAt = Instant.now().plusSeconds(3600);
        when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        // Act
        Url result = urlService.createShortUrl(originalUrl, testUser, expireAt);

        // Assert
        assertNotNull(result);
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void testCreateCustomShortUrl_Success() {
        // Arrange
        String originalUrl = "https://example.com";
        String customCode = "mylink";
        when(urlRepository.findByShortCode(customCode)).thenReturn(Optional.empty());
        when(urlRepository.save(any(Url.class))).thenReturn(testUrl);

        // Act
        Url result = urlService.createCustomShortUrl(originalUrl, customCode, testUser, null);

        // Assert
        assertNotNull(result);
        verify(urlRepository).findByShortCode(customCode);
        verify(urlRepository).save(any(Url.class));
    }

    @Test
    void testCreateCustomShortUrl_ThrowsExceptionWhenCodeExists() {
        // Arrange
        String originalUrl = "https://example.com";
        String customCode = "existing";
        when(urlRepository.findByShortCode(customCode)).thenReturn(Optional.of(testUrl));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            urlService.createCustomShortUrl(originalUrl, customCode, testUser, null);
        });

        assertTrue(exception.getMessage().contains("Short code already exists"));
        verify(urlRepository).findByShortCode(customCode);
        verify(urlRepository, never()).save(any(Url.class));
    }

    @Test
    void testFindByShortCodeAndTrack_Success() {
        // Arrange
        String shortCode = "abc123";
        testUrl.setExpireAt(null); // Not expired
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(testUrl));

        // Act
        Optional<Url> result = urlService.findByShortCodeAndTrack(shortCode);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(shortCode, result.get().getShortCode());
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository).incrementHits(eq(shortCode), any(Instant.class));
    }

    @Test
    void testFindByShortCodeAndTrack_ExpiredUrl() {
        // Arrange
        String shortCode = "expired";
        testUrl.setExpireAt(Instant.now().minusSeconds(3600)); // Expired 1 hour ago
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(testUrl));

        // Act
        Optional<Url> result = urlService.findByShortCodeAndTrack(shortCode);

        // Assert
        assertFalse(result.isPresent(), "Expired URL should not be returned");
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository, never()).incrementHits(anyString(), any(Instant.class));
    }

    @Test
    void testFindByShortCodeAndTrack_NotFound() {
        // Arrange
        String shortCode = "notfound";
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        // Act
        Optional<Url> result = urlService.findByShortCodeAndTrack(shortCode);

        // Assert
        assertFalse(result.isPresent());
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository, never()).incrementHits(anyString(), any(Instant.class));
    }

    @Test
    void testFindByShortCode_WithoutTracking() {
        // Arrange
        String shortCode = "abc123";
        when(urlRepository.findByShortCode(shortCode)).thenReturn(Optional.of(testUrl));

        // Act
        Optional<Url> result = urlService.findByShortCode(shortCode);

        // Assert
        assertTrue(result.isPresent());
        verify(urlRepository).findByShortCode(shortCode);
        verify(urlRepository, never()).incrementHits(anyString(), any(Instant.class));
    }

    @Test
    void testFindByUser() {
        // Arrange
        List<Url> expectedUrls = Arrays.asList(testUrl);
        when(urlRepository.findByCreatedBy(testUser)).thenReturn(expectedUrls);

        // Act
        List<Url> result = urlService.findByUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(urlRepository).findByCreatedBy(testUser);
    }

    @Test
    void testFindByUserId() {
        // Arrange
        Long userId = 1L;
        List<Url> expectedUrls = Arrays.asList(testUrl);
        when(urlRepository.findByCreatedById(userId)).thenReturn(expectedUrls);

        // Act
        List<Url> result = urlService.findByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(urlRepository).findByCreatedById(userId);
    }

    @Test
    void testGetMostPopularUrls() {
        // Arrange
        Url popularUrl = new Url();
        popularUrl.setHits(100L);
        List<Url> expectedUrls = Arrays.asList(popularUrl, testUrl);
        when(urlRepository.findTopByOrderByHitsDesc()).thenReturn(expectedUrls);

        // Act
        List<Url> result = urlService.getMostPopularUrls();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(urlRepository).findTopByOrderByHitsDesc();
    }

    @Test
    void testDeleteUrl() {
        // Arrange
        Long urlId = 1L;
        doNothing().when(urlRepository).deleteById(urlId);

        // Act
        urlService.deleteUrl(urlId);

        // Assert
        verify(urlRepository).deleteById(urlId);
    }

    @Test
    void testDeleteExpiredUrls() {
        // Arrange
        when(urlRepository.deleteExpiredUrls(any(Instant.class))).thenReturn(5);

        // Act
        int deletedCount = urlService.deleteExpiredUrls();

        // Assert
        assertEquals(5, deletedCount);
        verify(urlRepository).deleteExpiredUrls(any(Instant.class));
    }
}
