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
class ResultadoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM resultados");
        jdbcTemplate.execute("DELETE FROM submissions");
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        User u = new User("Estudiante Test", "estudiante.test@quimbaya.edu.co",
            passwordEncoder.encode("pwd"), "estudiante");
        userDao.save(u);

        String loginJson = "{\"email\":\"estudiante.test@quimbaya.edu.co\",\"password\":\"pwd\",\"role\":\"estudiante\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        token = node.path("data").path("token").asText();
    }

    @Test
    void getMisResultados_returnsEmptyList_whenNoResults() throws Exception {
        mockMvc.perform(get("/api/resultados/mis-resultados")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getMisResultados_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/resultados/mis-resultados"))
                .andExpect(status().isForbidden()); // Spring Security returns 403 for missing JWT
    }

    @Test
    void getByEvaluacion_returnsEmptyList_whenNoResults() throws Exception {
        mockMvc.perform(get("/api/resultados/evaluacion/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getBySubmission_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/resultados/submission/999")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
