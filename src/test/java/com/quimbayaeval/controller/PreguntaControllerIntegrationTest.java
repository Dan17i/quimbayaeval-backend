package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.Pregunta;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class PreguntaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private String token;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM preguntas");
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM users");

        User prof = new User("Prof", "prof3@example.com", passwordEncoder.encode("pwd"), "maestro");
        userDao.save(prof);
        // create evaluation
        jdbcTemplate.update("INSERT INTO cursos (codigo,nombre,profesor_id) VALUES ('X','X',?)", prof.getId());
        Integer cursoId = jdbcTemplate.queryForObject("SELECT id FROM cursos LIMIT 1", Integer.class);
        jdbcTemplate.update("INSERT INTO evaluaciones (nombre,curso_id,profesor_id,tipo) VALUES ('E',?,?,'Quiz')", cursoId, prof.getId());

        String loginJson = "{\"email\":\"prof3@example.com\",\"password\":\"pwd\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(content).path("data").path("token").asText();
    }

    @Test
    void createAndRetrievePregunta() throws Exception {
        // get evaluacion id
        Integer evalId = jdbcTemplate.queryForObject("SELECT id FROM evaluaciones LIMIT 1", Integer.class);
        Pregunta p = new Pregunta();
        p.setEvaluacionId(evalId);
        p.setEnunciado("¿1+1?");
        p.setTipo("respuesta_corta");
        String json = objectMapper.writeValueAsString(p);
        mockMvc.perform(post("/api/preguntas")
                .header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.evaluacionId").value(evalId));

        mockMvc.perform(get("/api/preguntas/evaluacion/"+evalId)
                .header("Authorization","Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].enunciado").value("¿1+1?"));
    }
}
