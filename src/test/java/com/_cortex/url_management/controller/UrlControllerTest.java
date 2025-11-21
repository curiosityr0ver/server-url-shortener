package com._cortex.url_management.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com._cortex.url_management.model.Url;
import com._cortex.url_management.model.User;
import com._cortex.url_management.security.JwtAuthenticationFilter;
import com._cortex.url_management.security.JwtUtil;
import com._cortex.url_management.security.SecurityConfig;
import com._cortex.url_management.service.CustomUserDetailsService;
import com._cortex.url_management.service.UrlService;
import com._cortex.url_management.service.UserService;

/**
 * Unit tests for UrlController
 */
@WebMvcTest(UrlController.class)
@Import(SecurityConfig.class)
public class UrlControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UrlService urlService;

        @MockBean
        private UserService userService;

        @MockBean
        private JwtUtil jwtUtil;

        @MockBean
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @Test
        @WithMockUser
        public void testCreateShortUrl_Success() throws Exception {
                // Arrange
                User user = new User(1L, "testuser", "test@example.com", "hashedpass");
                Url url = new Url(
                                1L, "abc123", "https://example.com", user,
                                Instant.now(), null, null, 0L);

                when(urlService.createShortUrl(anyString(), isNull(), isNull())).thenReturn(url);

                // Act & Assert
                mockMvc.perform(post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"originalUrl\":\"https://example.com\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.shortCode").value("abc123"))
                                .andExpect(jsonPath("$.originalUrl").value("https://example.com"));
        }

        @Test
        @WithMockUser
        public void testCreateShortUrl_ValidationError() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"originalUrl\":\"invalid-url\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        public void testCreateCustomShortUrl_Success() throws Exception {
                // Arrange
                Url url = new Url(
                                1L, "mylink", "https://example.com", null,
                                Instant.now(), null, null, 0L);

                when(urlService.createCustomShortUrl(anyString(), anyString(), isNull(), isNull())).thenReturn(url);

                // Act & Assert
                mockMvc.perform(post("/api/urls/custom")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"originalUrl\":\"https://example.com\",\"customShortCode\":\"mylink\"}"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.shortCode").value("mylink"));
        }

        @Test
        @WithMockUser
        public void testCreateCustomShortUrl_DuplicateShortCode() throws Exception {
                // Arrange
                when(urlService.createCustomShortUrl(anyString(), anyString(), isNull(), isNull()))
                                .thenThrow(new IllegalArgumentException("Short code already exists"));

                // Act & Assert
                mockMvc.perform(post("/api/urls/custom")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"originalUrl\":\"https://example.com\",\"customShortCode\":\"exists\"}"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        public void testGetUrlDetails_Success() throws Exception {
                // Arrange
                Url url = new Url(
                                1L, "abc123", "https://example.com", null,
                                Instant.now(), null, null, 42L);

                when(urlService.findByShortCode("abc123")).thenReturn(Optional.of(url));

                // Act & Assert
                mockMvc.perform(get("/api/urls/abc123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.shortCode").value("abc123"))
                                .andExpect(jsonPath("$.hits").value(42));
        }

        @Test
        @WithMockUser
        public void testGetUrlDetails_NotFound() throws Exception {
                // Arrange
                when(urlService.findByShortCode("notfound")).thenReturn(Optional.empty());

                // Act & Assert
                mockMvc.perform(get("/api/urls/notfound"))
                                .andExpect(status().isBadRequest());
        }

        @Test
        public void testRedirectToOriginalUrl_Success() throws Exception {
                // Arrange
                Url url = new Url(
                                1L, "abc123", "https://example.com", null,
                                Instant.now(), null, null, 42L);

                when(urlService.findByShortCodeAndTrack("abc123")).thenReturn(Optional.of(url));

                // Act & Assert
                // This endpoint is public, no @WithMockUser needed
                mockMvc.perform(get("/abc123"))
                                .andExpect(status().isFound())
                                .andExpect(header().string("Location", "https://example.com"));
        }

        @Test
        @WithMockUser
        public void testDeleteUrl_Success() throws Exception {
                // Arrange
                doNothing().when(urlService).deleteUrl(1L);

                // Act & Assert
                mockMvc.perform(delete("/api/urls/1"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        public void testGetUserUrls_Success() throws Exception {
                // Arrange
                List<Url> urls = Arrays.asList(
                                new Url(1L, "abc123", "https://example1.com", null, Instant.now(), null, null, 10L),
                                new Url(2L, "xyz789", "https://example2.com", null, Instant.now(), null, null, 20L));

                when(urlService.findByUserId(1L)).thenReturn(urls);

                // Act & Assert
                mockMvc.perform(get("/api/users/1/urls"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()").value(2))
                                .andExpect(jsonPath("$[0].shortCode").value("abc123"))
                                .andExpect(jsonPath("$[1].shortCode").value("xyz789"));
        }

        @Test
        @WithMockUser
        public void testGetPopularUrls_Success() throws Exception {
                // Arrange
                List<Url> urls = Arrays.asList(
                                new Url(1L, "viral", "https://popular.com", null, Instant.now(), null, null, 1000L),
                                new Url(2L, "trend", "https://trending.com", null, Instant.now(), null, null, 500L));

                when(urlService.getMostPopularUrls()).thenReturn(urls);

                // Act & Assert
                mockMvc.perform(get("/api/urls/stats/popular"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].hits").value(1000))
                                .andExpect(jsonPath("$[1].hits").value(500));
        }

        @Test
        public void testCreateShortUrl_Unauthorized() throws Exception {
                // Act & Assert - No @WithMockUser, should be forbidden
                mockMvc.perform(post("/api/urls")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"originalUrl\":\"https://example.com\"}"))
                                .andExpect(status().isForbidden());
        }
}
