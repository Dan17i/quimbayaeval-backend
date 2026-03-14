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
    private String tokenMaestro;
    private String tokenCoordinador;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM resultados");
        jdbcTemplate.execute("DELETE FROM submissions");
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        userDao.save(new User("Estudiante Test", "estudiante.test@quimbaya.edu.co",
            passwordEncoder.encode("pwd"), "estudiante"));
        userDao.save(new User("Profe Test", "profe.test@quimbaya.edu.co",
            passwordEncoder.encode("pwd"), "maestro"));
        userDao.save(new User("Admin Test", "admin.test@quimbaya.edu.co",
            passwordEncoder.encode("pwd"), "coordinador"));

        token          = login("estudiante.test@quimbaya.edu.co", "estudiante");
        tokenMaestro   = login("profe.test@quimbaya.edu.co", "maestro");
        tokenCoordinador = login("admin.test@quimbaya.edu.co", "coordinador");
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
                .andExpect(status().isForbidden());
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

    // ── nuevos endpoints de curso ──────────────────────────────────────────────

    @Test
    void getByCurso_returnsEmptyList_whenNoCurso() throws Exception {
        mockMvc.perform(get("/api/resultados/curso/999")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getByCurso_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/resultados/curso/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getByCurso_withData_returnsDetalles() throws Exception {
        // insertar datos mínimos: curso, evaluacion, submission, resultado
        Integer profesorId = userDao.findByEmail("profe.test@quimbaya.edu.co").get().getId();
        Integer estudianteId = userDao.findByEmail("estudiante.test@quimbaya.edu.co").get().getId();

        jdbcTemplate.execute("INSERT INTO cursos (id, codigo, nombre, descripcion, profesor_id, created_at, updated_at) " +
            "VALUES (100, 'TST-100', 'Curso Test', 'Desc', " + profesorId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, created_at, updated_at) " +
            "VALUES (100, 'Eval Test', 'Desc', 100, " + profesorId + ", 'Examen', 'Publicada', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO submissions (id, evaluacion_id, estudiante_id, estado) " +
            "VALUES (100, 100, " + estudianteId + ", 'Enviado')");
        jdbcTemplate.execute("INSERT INTO resultados (id, submission_id, puntuacion_total, puntuacion_maxima, porcentaje, estado_aprobacion) " +
            "VALUES (100, 100, 40.0, 50.0, 80.0, 'Aprobado')");

        mockMvc.perform(get("/api/resultados/curso/100")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].estudianteNombre").value("Estudiante Test"))
                .andExpect(jsonPath("$.data[0].evaluacionNombre").value("Eval Test"))
                .andExpect(jsonPath("$.data[0].profesorNombre").value("Profe Test"))
                .andExpect(jsonPath("$.data[0].notaEscala").isNumber());
    }

    @Test
    void getResumenByCurso_returnsEmptyList_whenNoCurso() throws Exception {
        mockMvc.perform(get("/api/resultados/curso/999/resumen")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void getResumenByCurso_requiresAuth() throws Exception {
        mockMvc.perform(get("/api/resultados/curso/1/resumen"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getResumenByCurso_withData_returnsResumen() throws Exception {
        Integer profesorId = userDao.findByEmail("profe.test@quimbaya.edu.co").get().getId();
        Integer estudianteId = userDao.findByEmail("estudiante.test@quimbaya.edu.co").get().getId();

        jdbcTemplate.execute("INSERT INTO cursos (id, codigo, nombre, descripcion, profesor_id, created_at, updated_at) " +
            "VALUES (200, 'TST-200', 'Curso Resumen', 'Desc', " + profesorId + ", CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, created_at, updated_at) " +
            "VALUES (200, 'Eval Resumen', 'Desc', 200, " + profesorId + ", 'Examen', 'Publicada', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)");
        jdbcTemplate.execute("INSERT INTO submissions (id, evaluacion_id, estudiante_id, estado) " +
            "VALUES (200, 200, " + estudianteId + ", 'Enviado')");
        jdbcTemplate.execute("INSERT INTO resultados (id, submission_id, puntuacion_total, puntuacion_maxima, porcentaje, estado_aprobacion) " +
            "VALUES (200, 200, 35.0, 50.0, 70.0, 'Aprobado')");

        mockMvc.perform(get("/api/resultados/curso/200/resumen")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].evaluacionNombre").value("Eval Resumen"))
                .andExpect(jsonPath("$.data[0].promedioGrupo").value(70.0))
                .andExpect(jsonPath("$.data[0].promedioEscala").isNumber())
                .andExpect(jsonPath("$.data[0].totalEstudiantes").value(1))
                .andExpect(jsonPath("$.data[0].aprobados").value(1))
                .andExpect(jsonPath("$.data[0].reprobados").value(0));
    }
}
