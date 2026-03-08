package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.Curso;
import com.quimbayaeval.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CursoControllerPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CursoDao cursoDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String professorToken;

    @BeforeEach
    void setUp() throws Exception {
        // limpiar tablas
        cursoDao.findAll().forEach(c -> cursoDao.deleteById(c.getId()));
        jdbcTemplate.execute("DELETE FROM users");

        // crear profesor
        User prof = new User("Prof", "prof@example.com", passwordEncoder.encode("pwd"), "maestro");
        userDao.save(prof);

        // obtener token
        String loginJson = "{\"email\":\"prof@example.com\",\"password\":\"pwd\",\"role\":\"maestro\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        professorToken = node.path("data").path("token").asText();

        // crear 15 cursos para probar paginación
        Integer profId = userDao.findByEmail("prof@example.com").get().getId();
        for (int i = 1; i <= 15; i++) {
            Curso c = new Curso();
            c.setCodigo("C" + i);
            c.setNombre("Curso " + i);
            c.setDescripcion("Descripción " + i);
            c.setProfesorId(profId);
            cursoDao.save(c);
        }
    }

    @Test
    void testGetAllWithoutPagination() throws Exception {
        mockMvc.perform(get("/api/cursos")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(15));
    }

    @Test
    void testGetAllWithPagination() throws Exception {
        mockMvc.perform(get("/api/cursos?page=0&size=5")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(5));
    }

    @Test
    void testGetAllWithPaginationPage2() throws Exception {
        mockMvc.perform(get("/api/cursos?page=2&size=5")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));
    }

    @Test
    void testGetAllWithPaginationBeyondBounds() throws Exception {
        mockMvc.perform(get("/api/cursos?page=100&size=5")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void testGetAllWithSorting() throws Exception {
        mockMvc.perform(get("/api/cursos?page=0&size=5&sort=codigo&direction=ASC")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigo").value("C1"));
    }

    @Test
    void testGetAllWithSortingDescending() throws Exception {
        mockMvc.perform(get("/api/cursos?page=0&size=5&sort=codigo&direction=DESC")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].codigo").value("C9"));
    }

    @Test
    void testGetAllWithZeroSize() throws Exception {
        // cuando size=0, debería devolver error o lista vacía
        mockMvc.perform(get("/api/cursos?page=0&size=0")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAllWithNegativePage() throws Exception {
        // página negativa debería manejar gracefully
        mockMvc.perform(get("/api/cursos?page=-1&size=5")
                .header("Authorization", "Bearer " + professorToken))
                .andExpect(status().is5xxServerError());
    }
}
