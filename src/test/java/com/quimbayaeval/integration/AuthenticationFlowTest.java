package com.quimbayaeval.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.model.dto.request.LoginRequestDTO;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        // Clean database
        userRepository.deleteAll();

        // Create test user
        testUser = new UserEntity();
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole("estudiante");
        testUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(testUser);
    }

    @Test
    void completeAuthenticationFlow_loginAndAccessProtectedEndpoint_success() throws Exception {
        // Step 1: Login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
        loginRequest.setRole("estudiante");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("test@test.com"))
                .andExpect(jsonPath("$.data.role").value("estudiante"))
                .andReturn();

        // Extract token from response
        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody)
                .get("data")
                .get("token")
                .asText();

        assertThat(token).isNotEmpty();

        // Step 2: Access protected endpoint with token
        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void authenticationFlow_invalidCredentials_returnsUnauthorized() throws Exception {
        // Attempt login with invalid credentials
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("wrongpassword");
        loginRequest.setRole("estudiante");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void authenticationFlow_accessProtectedEndpointWithoutToken_returnsUnauthorized() throws Exception {
        // Attempt to access protected endpoint without token
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isForbidden()); // Spring Security devuelve 403
    }

    @Test
    void authenticationFlow_accessProtectedEndpointWithInvalidToken_returnsUnauthorized() throws Exception {
        // Attempt to access protected endpoint with invalid token
        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isForbidden()); // Spring Security devuelve 403
    }

    @Test
    void authenticationFlow_loginWithNonExistentUser_returnsUnauthorized() throws Exception {
        // Attempt login with non-existent user
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("nonexistent@test.com");
        loginRequest.setPassword("password123");
        loginRequest.setRole("estudiante");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound()) // Usuario no encontrado devuelve 404
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void authenticationFlow_loginWithInvalidEmail_returnsBadRequest() throws Exception {
        // Attempt login with invalid email format
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("invalid-email");
        loginRequest.setPassword("password123");
        loginRequest.setRole("estudiante");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void authenticationFlow_loginWithBlankPassword_returnsBadRequest() throws Exception {
        // Attempt login with blank password
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("");
        loginRequest.setRole("estudiante");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void authenticationFlow_multipleSuccessfulLogins_generatesDifferentTokens() throws Exception {
        // First login
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");
        loginRequest.setRole("estudiante");

        MvcResult result1 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token1 = objectMapper.readTree(result1.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .asText();

        // Esperar 1 segundo para que el timestamp cambie
        Thread.sleep(1000);

        // Second login
        MvcResult result2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(result2.getResponse().getContentAsString())
                .get("data")
                .get("token")
                .asText();

        // Tokens should be different (different timestamps)
        assertThat(token1).isNotEqualTo(token2);

        // Both tokens should work
        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk());
    }
}
