package com._cortex.url_management.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com._cortex.url_management.model.User;
import com._cortex.url_management.security.JwtAuthenticationFilter;
import com._cortex.url_management.security.JwtUtil;
import com._cortex.url_management.security.SecurityConfig;
import com._cortex.url_management.service.CustomUserDetailsService;
import com._cortex.url_management.service.UserService;

/**
 * Unit tests for UserController
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    public void testCreateUser_Success() throws Exception {
        // Arrange
        User user = new User(1L, "testuser", "test@example.com", "hashedpassword");

        when(userService.createUser(any(User.class))).thenReturn(user);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    public void testCreateUser_ValidationError_InvalidEmail() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"invalid-email\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testCreateUser_ValidationError_ShortPassword() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testCreateUser_DuplicateUsername() throws Exception {
        // Arrange
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existing\",\"email\":\"new@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testGetUserById_Success() throws Exception {
        // Arrange
        User user = new User(1L, "testuser", "test@example.com", "hashedpassword");

        when(userService.findById(1L)).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    public void testGetUserById_NotFound() throws Exception {
        // Arrange
        when(userService.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    public void testGetUserByUsername_Success() throws Exception {
        // Arrange
        User user = new User(1L, "testuser", "test@example.com", "hashedpassword");

        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    public void testDeleteUser_Success() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testCreateUser_Unauthorized() throws Exception {
        // Act & Assert - No @WithMockUser, should be forbidden
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"test@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isForbidden());
    }
}
