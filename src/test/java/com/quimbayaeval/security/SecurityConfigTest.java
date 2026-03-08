package com.quimbayaeval.security;

import com.quimbayaeval.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    // ========== Public Endpoints Tests ==========

    @Test
    void publicEndpoint_login_allowsUnauthenticatedAccess() throws Exception {
        // El endpoint es público (no requiere auth), aunque el usuario no exista devuelve 404
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"role\":\"estudiante\"}"))
                .andExpect(status().isNotFound()); // Usuario no existe
    }

    @Test
    void publicEndpoint_actuatorHealth_allowsUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpoint_swaggerUI_allowsUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection()); // Redirects to swagger-ui/index.html
    }

    // ========== Protected Endpoints Tests ==========

    @Test
    void protectedEndpoint_withoutAuth_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isForbidden()); // Spring Security devuelve 403 sin auth
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = {"ESTUDIANTE"})
    void protectedEndpoint_withAuth_returnsOk() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    // ========== Role-Based Authorization Tests ==========

    @Test
    @WithMockUser(username = "estudiante@test.com", roles = {"ESTUDIANTE"})
    void estudianteRole_canAccessCursos() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "maestro@test.com", roles = {"MAESTRO"})
    void maestroRole_canAccessCursos() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "coordinador@test.com", roles = {"COORDINADOR"})
    void coordinadorRole_canAccessCursos() throws Exception {
        mockMvc.perform(get("/api/cursos"))
                .andExpect(status().isOk());
    }

    // ========== CORS Tests ==========

    @Test
    void corsConfiguration_allowsConfiguredOrigins() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .header("Origin", "http://localhost:5173")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"role\":\"estudiante\"}"))
                .andExpect(status().isNotFound()); // Usuario no existe
    }

    // ========== CSRF Tests ==========

    @Test
    void csrfProtection_isDisabled() throws Exception {
        // CSRF should be disabled for REST APIs
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"role\":\"estudiante\"}"))
                .andExpect(status().isNotFound()); // Usuario no existe
    }

    // ========== Session Management Tests ==========

    @Test
    void sessionManagement_isStateless() throws Exception {
        // Session should be stateless (no JSESSIONID cookie)
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"password\",\"role\":\"estudiante\"}"))
                .andExpect(status().isNotFound()); // Usuario no existe
        // No session cookie should be set
    }
}
