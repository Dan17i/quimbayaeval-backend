package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserDao userDao;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String tokenCoordinador;
    private String tokenMaestro;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        userDao.save(new User("Admin", "admin.test@q.edu.co", passwordEncoder.encode("pwd"), "coordinador"));
        userDao.save(new User("Profe", "profe.test@q.edu.co", passwordEncoder.encode("pwd"), "maestro"));
        userDao.save(new User("Estudiante", "est.test@q.edu.co", passwordEncoder.encode("pwd"), "estudiante"));

        tokenCoordinador = login("admin.test@q.edu.co", "coordinador");
        tokenMaestro     = login("profe.test@q.edu.co", "maestro");
    }

    private String login(String email, String role) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"pwd\",\"role\":\"%s\"}", email, role);
        String resp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("token").asText();
    }

    @Test
    void getUsers_noFilter_returnsAllActiveUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getUsers_filterByMaestro_returnsOnlyMaestros() throws Exception {
        mockMvc.perform(get("/api/users?role=maestro")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].role").value("maestro"));
    }

    @Test
    void getUsers_responseHasNoPassword() throws Exception {
        String resp = mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // el campo password no debe aparecer en la respuesta
        assert !resp.contains("password");
    }

    @Test
    void getUsers_maestroCanAccess() throws Exception {
        mockMvc.perform(get("/api/users?role=estudiante")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk());
    }

    @Test
    void getUsers_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMe_returnsAuthenticatedUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("profe.test@q.edu.co"))
                .andExpect(jsonPath("$.data.role").value("maestro"));
    }

    @Test
    void updateMe_updatesNameAndFotoAndReturnsProfile() throws Exception {
        String body = "{\"name\":\"Profe Actualizado\",\"fotoUrl\":\"https://example.com/foto.jpg\"}";
        mockMvc.perform(put("/api/users/me")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Profe Actualizado"))
                .andExpect(jsonPath("$.data.fotoUrl").value("https://example.com/foto.jpg"));
    }

    @Test
    void changePassword_validPassword_updatesSuccessfully() throws Exception {
        String body = "{\"passwordActual\":\"pwd\",\"passwordNueva\":\"nuevaClave123\"}";
        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void changePassword_wrongCurrentPassword_returnsBadRequest() throws Exception {
        String body = "{\"passwordActual\":\"wrongPwd\",\"passwordNueva\":\"nuevaClave123\"}";
        mockMvc.perform(put("/api/users/me/password")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
