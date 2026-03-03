package com.quimbayaeval.service;

import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.model.Evaluacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluacionServiceValidationTest {

    @Mock
    private EvaluacionDao evaluacionDao;

    @InjectMocks
    private EvaluacionService evaluacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCrearEvaluacionValida() {
        Evaluacion eval = new Evaluacion();
        eval.setNombre("Test");
        eval.setCursoId(1);
        eval.setProfesorId(1);
        when(evaluacionDao.save(eval)).thenReturn(eval);

        Evaluacion result = evaluacionService.crear(eval);
        assertNotNull(result);
        verify(evaluacionDao).save(eval);
    }

    @Test
    void testObtenerPorIdExistente() {
        Evaluacion eval = new Evaluacion();
        eval.setId(1);
        eval.setNombre("Evaluación 1");
        when(evaluacionDao.findById(1)).thenReturn(Optional.of(eval));

        Optional<Evaluacion> result = evaluacionService.obtenerPorId(1);
        assertTrue(result.isPresent());
        assertEquals("Evaluación 1", result.get().getNombre());
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(evaluacionDao.findById(999)).thenReturn(Optional.empty());

        Optional<Evaluacion> result = evaluacionService.obtenerPorId(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testObtenerTodasSinPaginacion() {
        List<Evaluacion> evals = Arrays.asList(new Evaluacion(), new Evaluacion());
        when(evaluacionDao.findAll()).thenReturn(evals);

        List<Evaluacion> result = evaluacionService.obtenerTodas();
        assertEquals(2, result.size());
    }

    @Test
    void testObtenerTodasConPaginacion() {
        List<Evaluacion> evals = Arrays.asList(new Evaluacion());
        Map<String, Object> filters = new HashMap<>();
        when(evaluacionDao.findAll(filters, 0, 5, "id", "DESC"))
                .thenReturn(evals);

        List<Evaluacion> result = evaluacionService.obtenerTodas(filters, 0, 5, "id", "DESC");
        assertEquals(1, result.size());
    }

    @Test
    void testObtenerActivas() {
        List<Evaluacion> evals = Arrays.asList(new Evaluacion());
        when(evaluacionDao.findByEstado("Activa")).thenReturn(evals);

        List<Evaluacion> result = evaluacionService.obtenerActivas();
        assertEquals(1, result.size());
    }

    @Test
    void testPublicarEvaluacion() {
        Evaluacion eval = new Evaluacion();
        eval.setId(1);
        eval.setPublicada(false);
        eval.setEstado("Borrador");
        when(evaluacionDao.findById(1)).thenReturn(Optional.of(eval));

        evaluacionService.publicar(1);

        assertTrue(eval.getPublicada());
        assertEquals("Activa", eval.getEstado());
        verify(evaluacionDao).update(eval);
    }

    @Test
    void testPublicarEvaluacionNoExistente() {
        when(evaluacionDao.findById(999)).thenReturn(Optional.empty());

        evaluacionService.publicar(999);

        // Debería manejar gracefully sin errores
        verify(evaluacionDao, never()).update(any());
    }

    @Test
    void testEliminarEvaluacion() {
        evaluacionService.eliminar(5);
        verify(evaluacionDao).deleteById(5);
    }

    @Test
    void testObtenerPorCurso() {
        List<Evaluacion> evals = Arrays.asList(new Evaluacion(), new Evaluacion());
        when(evaluacionDao.findByCurso(10)).thenReturn(evals);

        List<Evaluacion> result = evaluacionService.obtenerPorCurso(10);
        assertEquals(2, result.size());
    }

    @Test
    void testObtenerPorCursoSinEvaluaciones() {
        when(evaluacionDao.findByCurso(999)).thenReturn(Arrays.asList());

        List<Evaluacion> result = evaluacionService.obtenerPorCurso(999);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerDelProfesor() {
        List<Evaluacion> evals = Arrays.asList(new Evaluacion());
        when(evaluacionDao.findByProfesor(7)).thenReturn(evals);

        List<Evaluacion> result = evaluacionService.obtenerDelProfesor(7);
        assertEquals(1, result.size());
    }
}
