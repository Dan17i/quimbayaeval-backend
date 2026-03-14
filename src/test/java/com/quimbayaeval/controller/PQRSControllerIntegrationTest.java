package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.PQRSDao;
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
class PQRSControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PQRSDao pqrsDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String token;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM pqrs");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        // crear usuario
        User u = new User("Alice", "alice@example.com", passwordEncoder.encode("pwd"), "estudiante");
        userDao.save(u);
        String loginJson = "{\"email\":\"alice@example.com\",\"password\":\"pwd\",\"role\":\"estudiante\"}";
        String content = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(content);
        token = node.path("data").path("token").asText();
    }

    @Test
    void createAndRespondPQRS() throws Exception {
        Map<String,Object> dto = new HashMap<>();
        dto.put("tipo","Pregunta");
        dto.put("asunto","Test");
        dto.put("descripcion","¿hola?");
        dto.put("usuarioId", userDao.findByEmail("alice@example.com").get().getId());
        dto.put("estado","Pendiente");

        String json = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/api/pqrs")
                .header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        Integer id = pqrsDao.findByUsuario(userDao.findByEmail("alice@example.com").get().getId()).get(0).getId();
        Map<String,Object> resp = new HashMap<>();
        resp.put("respuesta","gracias");
        resp.put("respondidoPorId", userDao.findByEmail("alice@example.com").get().getId());
        String respJson = objectMapper.writeValueAsString(resp);
        mockMvc.perform(post("/api/pqrs/"+id+"/respond")
                .header("Authorization","Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(respJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("PQRS respondido exitosamente"));
    }
}
