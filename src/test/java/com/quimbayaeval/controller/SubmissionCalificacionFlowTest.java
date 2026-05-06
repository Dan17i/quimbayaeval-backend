package com.quimbayaeval.controller;

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

/**
 * Tests P1 bloqueantes (T01–T10) del reporte ISO 25010.
 * Cubre: validación de submissions, permisos en calificaciones,
 * unicidad por pregunta, cálculo automático de resultado y nota colombiana.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SubmissionCalificacionFlowTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserDao userDao;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Integer estudianteId;
    private Integer maestroId;
    private String token;
    private String tokenMaestro;
    private String tokenCoordinador;

    /*
     * Evaluaciones pre-insertadas:
     *  300 → Activa + publicada, 1 intento      (T01, T05)
     *  301 → Borrador             (T02)
     *  302 → Cerrada              (T03)
     *  303 → Activa + publicada, 1 intento, ya tiene 1 submission (T04)
     *  304 → Activa + publicada, 2 intentos, pregunta 300 con 4 pts (T06–T10)
     *
     * Submissions pre-insertadas:
     *  300 → eval 303, estudiante (agota intentos para T04)
     *  301 → eval 304, estudiante (base para calificaciones T06–T10)
     *
     * Pregunta pre-insertada:
     *  300 → eval 304, puntuacion 4.0
     */
    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM resultados");
        jdbcTemplate.execute("DELETE FROM calificaciones");
        jdbcTemplate.execute("DELETE FROM submissions");
        jdbcTemplate.execute("DELETE FROM inscripciones");
        jdbcTemplate.execute("DELETE FROM preguntas");
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        userDao.save(new User("Estudiante T", "estud.t@quimbaya.edu.co",
                passwordEncoder.encode("pwd"), "estudiante"));
        userDao.save(new User("Maestro T", "maestro.t@quimbaya.edu.co",
                passwordEncoder.encode("pwd"), "maestro"));
        userDao.save(new User("Coord T", "coord.t@quimbaya.edu.co",
                passwordEncoder.encode("pwd"), "coordinador"));

        estudianteId = userDao.findByEmail("estud.t@quimbaya.edu.co").get().getId();
        maestroId    = userDao.findByEmail("maestro.t@quimbaya.edu.co").get().getId();

        jdbcTemplate.update(
            "INSERT INTO cursos (id, codigo, nombre, descripcion, profesor_id, created_at, updated_at) " +
            "VALUES (300, 'F-300', 'Curso Flow', 'Desc', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);

        // evaluacion 300: Activa + publicada, 1 intento
        jdbcTemplate.update(
            "INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, intentos_permitidos, created_at, updated_at) " +
            "VALUES (300, 'Eval Activa', 'Desc', 300, ?, 'Examen', 'Activa', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);
        // evaluacion 301: Borrador
        jdbcTemplate.update(
            "INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, intentos_permitidos, created_at, updated_at) " +
            "VALUES (301, 'Eval Borrador', 'Desc', 300, ?, 'Examen', 'Borrador', false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);
        // evaluacion 302: Cerrada
        jdbcTemplate.update(
            "INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, intentos_permitidos, created_at, updated_at) " +
            "VALUES (302, 'Eval Cerrada', 'Desc', 300, ?, 'Examen', 'Cerrada', false, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);
        // evaluacion 303: Activa + publicada, 1 intento (ya agotado por submission 300)
        jdbcTemplate.update(
            "INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, intentos_permitidos, created_at, updated_at) " +
            "VALUES (303, 'Eval Agotada', 'Desc', 300, ?, 'Examen', 'Activa', true, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);
        // evaluacion 304: Activa + publicada, 2 intentos — base para calificaciones
        jdbcTemplate.update(
            "INSERT INTO evaluaciones (id, nombre, descripcion, curso_id, profesor_id, tipo, estado, publicada, intentos_permitidos, created_at, updated_at) " +
            "VALUES (304, 'Eval Calificacion', 'Desc', 300, ?, 'Examen', 'Activa', true, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            maestroId);

        // pregunta 300 → eval 304, puntuacion 4.0
        jdbcTemplate.execute(
            "INSERT INTO preguntas (id, evaluacion_id, enunciado, tipo, puntuacion) " +
            "VALUES (300, 304, 'Pregunta test', 'seleccion_multiple', 4.0)");

        // submission 300 → agota el único intento de eval 303 para el estudiante
        jdbcTemplate.update(
            "INSERT INTO submissions (id, evaluacion_id, estudiante_id, estado) " +
            "VALUES (300, 303, ?, 'Enviado')", estudianteId);

        // submission 301 → eval 304, base para tests de calificación
        jdbcTemplate.update(
            "INSERT INTO submissions (id, evaluacion_id, estudiante_id, estado) " +
            "VALUES (301, 304, ?, 'Enviado')", estudianteId);

        token            = login("estud.t@quimbaya.edu.co", "estudiante");
        tokenMaestro     = login("maestro.t@quimbaya.edu.co", "maestro");
        tokenCoordinador = login("coord.t@quimbaya.edu.co", "coordinador");
    }

    private String login(String email, String role) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"pwd\",\"role\":\"%s\"}", email, role);
        String resp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("token").asText();
    }

    // ── T01 ──────────────────────────────────────────────────────────────────
    // POST /api/submissions con evaluación Activa + publicada → 200 OK

    @Test
    void T01_submission_evaluacionActiva_returnsOk() throws Exception {
        String body = "{\"evaluacionId\":300}";
        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.evaluacionId").value(300));
    }

    // ── T02 ──────────────────────────────────────────────────────────────────
    // POST /api/submissions con evaluación en Borrador → 400

    @Test
    void T02_submission_evaluacionBorrador_returns400() throws Exception {
        String body = "{\"evaluacionId\":301}";
        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── T03 ──────────────────────────────────────────────────────────────────
    // POST /api/submissions con evaluación Cerrada → 400

    @Test
    void T03_submission_evaluacionCerrada_returns400() throws Exception {
        String body = "{\"evaluacionId\":302}";
        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── T04 ──────────────────────────────────────────────────────────────────
    // POST /api/submissions cuando los intentos ya se agotaron → 400

    @Test
    void T04_submission_intentosAgotados_returns400() throws Exception {
        // eval 303 ya tiene submission 300 para este estudiante (1 intento usado, límite 1)
        String body = "{\"evaluacionId\":303}";
        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── T05 ──────────────────────────────────────────────────────────────────
    // estudianteId se toma del JWT, no del body → el campo en body es ignorado

    @Test
    void T05_submission_estudianteIdFromJwt_ignoresBodyValue() throws Exception {
        // Enviamos estudianteId=9999 en el body; el controlador debe sobreescribirlo con el del JWT
        String body = "{\"evaluacionId\":300,\"estudianteId\":9999}";
        mockMvc.perform(post("/api/submissions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estudianteId").value(estudianteId));
    }

    // ── T06 ──────────────────────────────────────────────────────────────────
    // POST /api/calificaciones por maestro → 200, calificadoPorId viene del JWT

    @Test
    void T06_calificacion_byMaestro_returnsOkAndCalificadoPorIdFromJwt() throws Exception {
        String body = String.format(
            "{\"submissionId\":301,\"preguntaId\":300,\"puntuacionObtenida\":3.0,\"calificadoPorId\":9999}");
        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.calificadoPorId").value(maestroId));
    }

    // ── T07 ──────────────────────────────────────────────────────────────────
    // POST /api/calificaciones por estudiante → 403 Forbidden

    @Test
    void T07_calificacion_byEstudiante_returns403() throws Exception {
        String body = "{\"submissionId\":301,\"preguntaId\":300,\"puntuacionObtenida\":3.0}";
        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    // ── T08 ──────────────────────────────────────────────────────────────────
    // Calificación duplicada (mismo submission + pregunta) → error de restricción

    @Test
    void T08_calificacion_duplicada_returnsError() throws Exception {
        String body = "{\"submissionId\":301,\"preguntaId\":300,\"puntuacionObtenida\":3.0}";

        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        // segundo intento con el mismo (submission, pregunta) → viola UNIQUE
        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is5xxServerError());
    }

    // ── T09 ──────────────────────────────────────────────────────────────────
    // Después de calificar, GET /api/resultados/mis-resultados refleja el resultado

    @Test
    void T09_misResultados_reflejaCalificacion() throws Exception {
        String body = "{\"submissionId\":301,\"preguntaId\":300,\"puntuacionObtenida\":3.0}";
        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resultados/mis-resultados")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].submissionId").value(301));
    }

    // ── T10 ──────────────────────────────────────────────────────────────────
    // Nota colombiana: puntuacion 3.0 / maxima 4.0 → porcentaje 75 → notaEscala 4.0

    @Test
    void T10_notaEscalaColombiana_porcentaje75_esNotaCuatro() throws Exception {
        // pregunta 300 tiene puntuacion maxima 4.0; calificamos con 3.0 → porcentaje = 75
        String body = "{\"submissionId\":301,\"preguntaId\":300,\"puntuacionObtenida\":3.0}";
        mockMvc.perform(post("/api/calificaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/resultados/mis-resultados")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].porcentaje").value(75.0))
                .andExpect(jsonPath("$.data[0].notaEscala").value(4.0));
    }
}
