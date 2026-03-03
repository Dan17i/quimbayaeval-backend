package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.Curso;
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
@TestPropertySource(locations = "classpath:application-test.yml")
class EvaluacionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CursoDao cursoDao;

    @Autowired
    private EvaluacionDao evaluacionDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;
    private Integer cursoId;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        // crear profesor y curso
        User prof = new User("Prof", "prof2@example.com", passwordEncoder.encode("pwd"), "maestro");
        userDao.save(prof);
        Curso c = new Curso();
        c.setCodigo("C200");
        c.setNombre("Course");
        c.setDescripcion("desc");
        c.setProfesorId(prof.getId());
        cursoDao.save(c);
        cursoId = c.getId();

        // login professor
        String loginJson = "{\"email\":\"prof2@example.com\",\"password\":\"pwd\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        token = node.path("data").path("token").asText();
    }

    @Test
    void createAndPublishEvaluacion() throws Exception {
        Map<String,Object> dto = new HashMap<>();
        dto.put("nombre","Eval1");
        dto.put("descripcion","desc");
        dto.put("cursoId", cursoId);
        dto.put("profesorId", userDao.findByEmail("prof2@example.com").get().getId());
        dto.put("tipo","Quiz");

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/evaluaciones")
                .header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // publish
        Integer evalId = evaluacionDao.findByCurso(cursoId).get(0).getId();
        mockMvc.perform(post("/api/evaluaciones/"+evalId+"/publicar")
                .header("Authorization","Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("Activa"));
    }
}
