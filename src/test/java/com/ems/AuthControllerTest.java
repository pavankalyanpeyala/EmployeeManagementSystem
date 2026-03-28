package com.ems;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ems.controller.AuthController;
import com.ems.dto.LoginRequest;
import com.ems.dto.LoginResponse;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(com.ems.config.SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private com.ems.security.JwtAuthFilter jwtAuthFilter;
    @MockBean private com.ems.security.JwtUtils jwtUtils;
    @MockBean private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void login_shouldReturn200_whenValidCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        LoginResponse response = LoginResponse.builder()
                .token("mocked.jwt.token")
                .type("Bearer")
                .username("admin")
                .role("ROLE_ADMIN")
                .expiresIn(86400000L)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mocked.jwt.token"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    void login_shouldReturn400_whenMissingFields() throws Exception {
        LoginRequest request = new LoginRequest();
        // missing username and password

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void register_shouldReturn201_whenAdminRegistersUser() throws Exception {
        com.ems.dto.RegisterRequest request = new com.ems.dto.RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@ems.com");
        request.setRole(Role.ROLE_EMPLOYEE);

        User user = User.builder()
                .id(2L).username("newuser")
                .email("new@ems.com").role(Role.ROLE_EMPLOYEE).build();
        when(authService.register(any())).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void register_shouldReturn403_whenNotAdmin() throws Exception {
        com.ems.dto.RegisterRequest request = new com.ems.dto.RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");
        request.setEmail("new@ems.com");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
