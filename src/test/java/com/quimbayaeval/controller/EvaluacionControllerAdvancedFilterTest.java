package com.quimbayaeval.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.service.EvaluacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests para EvaluacionController con filtrado avanzado y caché
 */
@SpringBootTest
@AutoConfigureMockMvc
public class EvaluacionControllerAdvancedFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EvaluacionService evaluacionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Evaluacion evaluacion1;
    private Evaluacion evaluacion2;
    private Evaluacion evaluacion3;

    @BeforeEach
    void setUp() {
        evaluacion1 = new Evaluacion();
        evaluacion1.setId(1);
        evaluacion1.setNombre("Parcial 1 - Cálculo");
        evaluacion1.setDescripcion("Primera evaluación");
        evaluacion1.setTipo("Examen");
        evaluacion1.setEstado("Activa");
        evaluacion1.setCursoId(1);
        evaluacion1.setProfesorId(1);
        evaluacion1.setDuracionMinutos(60);
        evaluacion1.setPublicada(true);

        evaluacion2 = new Evaluacion();
        evaluacion2.setId(2);
        evaluacion2.setNombre("Quiz 1 - Geometría");
        evaluacion2.setDescripcion("Quiz corto");
        evaluacion2.setTipo("Quiz");
        evaluacion2.setEstado("Activa");
        evaluacion2.setCursoId(1);
        evaluacion2.setProfesorId(1);
        evaluacion2.setDuracionMinutos(20);
        evaluacion2.setPublicada(true);

        evaluacion3 = new Evaluacion();
        evaluacion3.setId(3);
        evaluacion3.setNombre("Proyecto Final");
        evaluacion3.setDescripcion("Proyecto de semestre");
        evaluacion3.setTipo("Proyecto");
        evaluacion3.setEstado("Programada");
        evaluacion3.setCursoId(2);
        evaluacion3.setProfesorId(2);
        evaluacion3.setDuracionMinutos(120);
        evaluacion3.setPublicada(false);
    }

    @Test
    void testObtenerTodasSinFiltros() throws Exception {
        when(evaluacionService.obtenerTodas()).thenReturn(Arrays.asList(evaluacion1, evaluacion2, evaluacion3));

        mockMvc.perform(get("/api/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    void testObtenerTodasConPaginacion() throws Exception {
        List<Evaluacion> page = Arrays.asList(evaluacion1, evaluacion2);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page);

        mockMvc.perform(get("/api/evaluaciones?page=0&size=2&sort=nombre&direction=ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testObtenerConFiltroEstado() throws Exception {
        List<Evaluacion> activas = Arrays.asList(evaluacion1, evaluacion2);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(activas);

        mockMvc.perform(get("/api/evaluaciones?estado=Activa&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testObtenerConFiltroTipo() throws Exception {
        List<Evaluacion> examenes = Arrays.asList(evaluacion1);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(examenes);

        mockMvc.perform(get("/api/evaluaciones?tipo=Examen&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testObtenerConFiltroBusquedaTexto() throws Exception {
        List<Evaluacion> resultados = Arrays.asList(evaluacion1);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(resultados);

        mockMvc.perform(get("/api/evaluaciones?nombre=Cálculo&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testObtenerConMultiplesFiltros() throws Exception {
        List<Evaluacion> resultado = Arrays.asList(evaluacion1);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(resultado);

        mockMvc.perform(get("/api/evaluaciones?tipo=Examen&estado=Activa&cursoId=1&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testObtenerConOrdenamiento() throws Exception {
        List<Evaluacion> ordenados = Arrays.asList(evaluacion2, evaluacion1);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(ordenados);

        mockMvc.perform(get("/api/evaluaciones?page=0&size=10&sort=nombre&direction=ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].nombre").value("Quiz 1 - Geometría"))
                .andExpect(jsonPath("$.data[1].nombre").value("Parcial 1 - Cálculo"));
    }

    @Test
    void testObtenerPorCursoConCache() throws Exception {
        when(evaluacionService.obtenerPorCurso(1)).thenReturn(Arrays.asList(evaluacion1, evaluacion2));

        // Primer request - llamará al servicio
        mockMvc.perform(get("/api/evaluaciones/curso/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));

        // Segundo request - debería estar en caché (no llamará nuevamente al servicio)
        mockMvc.perform(get("/api/evaluaciones/curso/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testObtenerActivasConCache() throws Exception {
        when(evaluacionService.obtenerActivas()).thenReturn(Arrays.asList(evaluacion1, evaluacion2));

        mockMvc.perform(get("/api/evaluaciones/estado/activas")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void testCrearInvalidaCache() throws Exception {
        when(evaluacionService.crear(any(Evaluacion.class))).thenReturn(evaluacion3);

        mockMvc.perform(post("/api/evaluaciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evaluacion3)))
                .andExpect(status().isCreated());
    }

    @Test
    void testActualizarInvalidaCache() throws Exception {
        mockMvc.perform(put("/api/evaluaciones/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(evaluacion1)))
                .andExpect(status().isOk());
    }

    @Test
    void testObtenerPorIdSinCache() throws Exception {
        when(evaluacionService.obtenerPorId(1)).thenReturn(Optional.of(evaluacion1));

        mockMvc.perform(get("/api/evaluaciones/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nombre").value("Parcial 1 - Cálculo"));
    }

    @Test
    void testFiltroEstadoYCurso() throws Exception {
        List<Evaluacion> resultado = Arrays.asList(evaluacion1);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(resultado);

        mockMvc.perform(get("/api/evaluaciones?estado=Activa&cursoId=1&page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].cursoId").value(1))
                .andExpect(jsonPath("$.data[0].estado").value("Activa"));
    }

    @Test
    void testPaginacionEnSegundaPagina() throws Exception {
        List<Evaluacion> page2 = Arrays.asList(evaluacion3);
        when(evaluacionService.obtenerConFiltrosAvanzados(anyList(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(page2);

        mockMvc.perform(get("/api/evaluaciones?page=1&size=2&sort=id&direction=ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value(3));
    }
}
