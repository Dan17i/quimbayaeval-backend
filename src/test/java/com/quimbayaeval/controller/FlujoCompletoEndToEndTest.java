package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.User;
import com.quimbayaeval.model.dto.request.CalificacionBatchRequestDTO;
import com.quimbayaeval.model.dto.request.CalificacionItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🎯 PRUEBA DE HUMO INTEGRAL DE FLUJO COMPLETO (END-TO-END)
 *
 * Flujo evaluativo y trazabilidad académica en QuimbayaEVAL:
 * 1. Docente crea evaluación con 3 preguntas (opción múltiple, V/F, abierta) y la publica.
 * 2. Aprendiz rinde la prueba y recibe autocalificación instantánea de preguntas objetivas.
 * 3. Docente realiza calificación en lote (Batch) y consolida informe de notas.
 * 4. Aprendiz radica PQRS, Coordinador valida el SLA de 15 días y atiende la solicitud.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FlujoCompletoEndToEndTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserDao userDao;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Integer maestroId;
    private Integer estudianteId;
    private Integer coordinadorId;

    private String tokenMaestro;
    private String tokenEstudiante;
    private String tokenCoordinador;

    private Integer cursoId = 770;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("DELETE FROM resultados");
        jdbcTemplate.execute("DELETE FROM calificaciones");
        jdbcTemplate.execute("DELETE FROM submissions");
        jdbcTemplate.execute("DELETE FROM pqrs");
        jdbcTemplate.execute("DELETE FROM inscripciones");
        jdbcTemplate.execute("DELETE FROM preguntas");
        jdbcTemplate.execute("DELETE FROM evaluaciones");
        jdbcTemplate.execute("DELETE FROM cursos");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        userDao.save(new User("Instructor SENA", "maestro.e2e@quimbaya.edu.co", passwordEncoder.encode("pwd123"), "maestro"));
        userDao.save(new User("Aprendiz SENA", "estudiante.e2e@quimbaya.edu.co", passwordEncoder.encode("pwd123"), "estudiante"));
        userDao.save(new User("Coordinador SENA", "coordinador.e2e@quimbaya.edu.co", passwordEncoder.encode("pwd123"), "coordinador"));

        maestroId = userDao.findByEmail("maestro.e2e@quimbaya.edu.co").get().getId();
        estudianteId = userDao.findByEmail("estudiante.e2e@quimbaya.edu.co").get().getId();
        coordinadorId = userDao.findByEmail("coordinador.e2e@quimbaya.edu.co").get().getId();

        tokenMaestro = login("maestro.e2e@quimbaya.edu.co", "maestro");
        tokenEstudiante = login("estudiante.e2e@quimbaya.edu.co", "estudiante");
        tokenCoordinador = login("coordinador.e2e@quimbaya.edu.co", "coordinador");

        // Crear curso e inscribir aprendiz
        jdbcTemplate.update(
            "INSERT INTO cursos (id, codigo, nombre, descripcion, profesor_id, created_at, updated_at) " +
            "VALUES (?, 'ADSO-77', 'Análisis y Desarrollo de Software', 'Ficha formativa SENA', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
            cursoId, maestroId);

        jdbcTemplate.update(
            "INSERT INTO inscripciones (estudiante_id, curso_id) VALUES (?, ?)",
            estudianteId, cursoId);
    }

    private String login(String email, String role) throws Exception {
        String body = String.format("{\"email\":\"%s\",\"password\":\"pwd123\",\"role\":\"%s\"}", email, role);
        String resp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("token").asText();
    }

    @Test
    @DisplayName("Flujo Completo E2E: Creación -> Rendición con Autocalificación -> Batch Grading -> PQRS con SLA 15 Días")
    void testFlujoCompletoEndToEnd() throws Exception {

        // =========================================================================
        // PASO 1: MAESTRO CREA EVALUACIÓN CON 3 PREGUNTAS Y LA PUBLICA
        // =========================================================================

        // 1.1 Crear Evaluación
        String evalJson = String.format("""
            {
                "nombre": "Evaluación Integral E2E",
                "descripcion": "Prueba de conocimientos técnicos y metodológicos",
                "cursoId": %d,
                "profesorId": %d,
                "tipo": "Examen",
                "estado": "Borrador",
                "publicada": false,
                "intentosPermitidos": 1,
                "duracionMinutos": 60
            }
            """, cursoId, maestroId);

        String evalResp = mockMvc.perform(post("/api/evaluaciones")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(evalJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        Integer evalId = objectMapper.readTree(evalResp).path("data").path("id").asInt();
        assertNotNull(evalId);

        // 1.2 Crear Pregunta 1: Opción Múltiple (2.0 pts)
        String p1Json = String.format("""
            {
                "evaluacionId": %d,
                "tipo": "seleccion_multiple",
                "enunciado": "¿Cuál es el lenguaje principal de Spring Boot?",
                "opcionesJson": "[{\\"id\\":1,\\"texto\\":\\"Java\\",\\"correcta\\":true},{\\"id\\":2,\\"texto\\":\\"PHP\\",\\"correcta\\":false}]",
                "respuestaCorrectaJson": "{\\"respuesta\\":\\"Java\\"}",
                "puntuacion": 2.0,
                "orden": 1
            }
            """, evalId);

        String p1Resp = mockMvc.perform(post("/api/preguntas")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(p1Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer p1Id = objectMapper.readTree(p1Resp).path("data").path("id").asInt();

        // 1.3 Crear Pregunta 2: Verdadero o Falso (1.5 pts)
        String p2Json = String.format("""
            {
                "evaluacionId": %d,
                "tipo": "verdadero_falso",
                "enunciado": "El SENA imparte formación profesional integral y gratuita.",
                "opcionesJson": "[{\\"id\\":1,\\"texto\\":\\"true\\",\\"correcta\\":true},{\\"id\\":2,\\"texto\\":\\"false\\",\\"correcta\\":false}]",
                "respuestaCorrectaJson": "{\\"respuesta\\":\\"true\\"}",
                "puntuacion": 1.5,
                "orden": 2
            }
            """, evalId);

        String p2Resp = mockMvc.perform(post("/api/preguntas")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(p2Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer p2Id = objectMapper.readTree(p2Resp).path("data").path("id").asInt();

        // 1.4 Crear Pregunta 3: Abierta / Ensayo (1.5 pts)
        String p3Json = String.format("""
            {
                "evaluacionId": %d,
                "tipo": "abierta",
                "enunciado": "Describa los principios clave de una arquitectura limpia.",
                "puntuacion": 1.5,
                "orden": 3
            }
            """, evalId);

        String p3Resp = mockMvc.perform(post("/api/preguntas")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(p3Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer p3Id = objectMapper.readTree(p3Resp).path("data").path("id").asInt();

        // 1.5 Publicar Evaluación
        mockMvc.perform(post("/api/evaluaciones/" + evalId + "/publicar")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // =========================================================================
        // PASO 2: APRENDIZ RINDE LA EVALUACIÓN Y RECIBE AUTOCALIFICACIÓN INMEDIATA
        // =========================================================================

        Map<String, String> respuestasMap = new HashMap<>();
        respuestasMap.put(String.valueOf(p1Id), "Java"); // Correcta -> 2.0 pts
        respuestasMap.put(String.valueOf(p2Id), "true"); // Correcta -> 1.5 pts
        respuestasMap.put(String.valueOf(p3Id), "Separación de responsabilidades, inversión de dependencias y desacoplamiento."); // Abierta

        String submitJson = objectMapper.writeValueAsString(respuestasMap);

        String submitResp = mockMvc.perform(post("/api/evaluaciones/" + evalId + "/submit")
                .header("Authorization", "Bearer " + tokenEstudiante)
                .contentType(MediaType.APPLICATION_JSON)
                .content(submitJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        Integer submissionId = objectMapper.readTree(submitResp).path("data").path("id").asInt();
        assertNotNull(submissionId);

        // Verificar Autocalificación inmediata (Puntuación automática: 2.0 + 1.5 = 3.5 pts sobre 5.0 -> notaEscala 3.8)
        mockMvc.perform(get("/api/resultados/mis-resultados")
                .header("Authorization", "Bearer " + tokenEstudiante))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].puntuacionTotal").value(3.5))
                .andExpect(jsonPath("$.data[0].notaEscala").value(3.8));

        // =========================================================================
        // PASO 3: MAESTRO CALIFICA EN BATCH Y OBSERVA LA NOTA CONSOLIDADA
        // =========================================================================

        // 3.1 Consultar DTO enriquecido sin client-side joins
        mockMvc.perform(get("/api/submissions/evaluacion/" + evalId + "/detalles")
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].estudianteNombre").value("Aprendiz SENA"))
                .andExpect(jsonPath("$.data[0].estudianteEmail").value("estudiante.e2e@quimbaya.edu.co"));

        // 3.2 Calificar pregunta abierta en batch (1.5 pts) + Observación docente
        CalificacionBatchRequestDTO batchRequest = new CalificacionBatchRequestDTO();
        batchRequest.setSubmissionId(submissionId);
        batchRequest.setObservacionesGenerales("Excelente sustentación teórica y aplicación práctica.");

        CalificacionItemDTO item1 = new CalificacionItemDTO(p1Id, 2.0, 2.0, "Correcto");
        CalificacionItemDTO item2 = new CalificacionItemDTO(p2Id, 1.5, 1.5, "Correcto");
        CalificacionItemDTO item3 = new CalificacionItemDTO(p3Id, 1.5, 1.5, "Argumentación técnica completa");

        batchRequest.setCalificaciones(List.of(item1, item2, item3));

        mockMvc.perform(post("/api/calificaciones/batch")
                .header("Authorization", "Bearer " + tokenMaestro)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3.3 Verificar reporte del curso actualizado al 100% (5.0 / 5.0 - Aprobado)
        mockMvc.perform(get("/api/resultados/curso/" + cursoId)
                .header("Authorization", "Bearer " + tokenMaestro))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].notaEscala").value(5.0))
                .andExpect(jsonPath("$.data[0].estadoAprobacion").value("Aprobado"));

        // =========================================================================
        // PASO 4: APRENDIZ RADICA PQRS, COORDINADOR VERIFICA SLA DE 15 DÍAS Y RESPONDE
        // =========================================================================

        // 4.1 Aprendiz radica PQRS
        String pqrsJson = String.format("""
            {
                "tipo": "Petición",
                "asunto": "Constancia de aprobación técnica E2E",
                "descripcion": "Solicito copia oficial del resultado de la prueba.",
                "cursoId": %d,
                "destinatario": "coordinador"
            }
            """, cursoId);

        String pqrsResp = mockMvc.perform(post("/api/pqrs")
                .header("Authorization", "Bearer " + tokenEstudiante)
                .contentType(MediaType.APPLICATION_JSON)
                .content(pqrsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        Integer pqrsId = objectMapper.readTree(pqrsResp).path("data").path("id").asInt();
        assertNotNull(pqrsId);

        // 4.2 Coordinador consulta listado enriquecido con SLA Legal
        mockMvc.perform(get("/api/pqrs/detalles")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(pqrsId))
                .andExpect(jsonPath("$.data[0].enPlazo").value(true))
                .andExpect(jsonPath("$.data[0].estadoSLA").value("A_TIEMPO"))
                .andExpect(jsonPath("$.data[0].usuarioNombre").value("Aprendiz SENA"));

        // 4.3 Coordinador responde y cierra el caso satisfactoriamente
        String responderJson = """
            {
                "respuesta": "Estimado aprendiz, su constancia ha sido emitida satisfactoriamente.",
                "estado": "Resuelta"
            }
            """;

        mockMvc.perform(put("/api/pqrs/" + pqrsId + "/responder")
                .header("Authorization", "Bearer " + tokenCoordinador)
                .contentType(MediaType.APPLICATION_JSON)
                .content(responderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4.4 Verificar estado final del SLA institucional
        mockMvc.perform(get("/api/pqrs/" + pqrsId + "/detalle")
                .header("Authorization", "Bearer " + tokenCoordinador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.estado").value("Resuelta"))
                .andExpect(jsonPath("$.data.estadoSLA").value("ATENDIDO_A_TIEMPO"))
                .andExpect(jsonPath("$.data.respondidoPorNombre").value("Coordinador SENA"));
    }
}
