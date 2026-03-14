package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CursoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CursoDao cursoDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String professorToken;

    @BeforeEach
    void setUp() throws Exception {
        // limpiar en orden correcto (hijos antes que padres)
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM pqrs");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        // crear profesor
        User prof = new User("Prof", "prof@example.com", passwordEncoder.encode("pwd"), "maestro");
        userDao.save(prof);
        // obtener token mediante login
        String loginJson = "{\"email\":\"prof@example.com\",\"password\":\"pwd\",\"role\":\"maestro\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        professorToken = node.path("data").path("token").asText();
    }

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void createAndListCourse() throws Exception {
        Map<String,Object> dto = new HashMap<>();
        dto.put("codigo","C101");
        dto.put("nombre","Test Course");
        dto.put("descripcion","Desc");
        dto.put("profesorId", userDao.findByEmail("prof@example.com").get().getId());

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/cursos")
                .header("Authorization","Bearer " + professorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.codigo").value("C101"));

        // listar
        mockMvc.perform(get("/api/cursos")
                .header("Authorization","Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigo").value("C101"));
    }
}
