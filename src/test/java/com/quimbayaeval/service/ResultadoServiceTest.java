package com.quimbayaeval.service;

import com.quimbayaeval.dao.ResultadoDao;
import com.quimbayaeval.model.Resultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultadoServiceTest {

    @Mock
    private ResultadoDao resultadoDao;

    @InjectMocks
    private ResultadoService resultadoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerPorEstudiante_delegatesDao() {
        Resultado r = new Resultado();
        r.setEstudianteId(4);
        r.setPorcentaje(new BigDecimal("85.0"));
        when(resultadoDao.findByEstudiante(4)).thenReturn(Arrays.asList(r));

        List<Resultado> result = resultadoService.obtenerPorEstudiante(4);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getEstudianteId());
        verify(resultadoDao).findByEstudiante(4);
    }

    @Test
    void obtenerPorEvaluacion_delegatesDao() {
        Resultado r = new Resultado();
        r.setEvaluacionId(2);
        when(resultadoDao.findByEvaluacion(2)).thenReturn(Arrays.asList(r));

        List<Resultado> result = resultadoService.obtenerPorEvaluacion(2);

        assertEquals(1, result.size());
        verify(resultadoDao).findByEvaluacion(2);
    }

    @Test
    void obtenerPorSubmission_returnsEmpty_whenNotFound() {
        when(resultadoDao.findBySubmission(99)).thenReturn(Optional.empty());

        Optional<Resultado> result = resultadoService.obtenerPorSubmission(99);

        assertFalse(result.isPresent());
        verify(resultadoDao).findBySubmission(99);
    }

    @Test
    void obtenerPorSubmission_returnsResultado_whenFound() {
        Resultado r = new Resultado();
        r.setSubmissionId(2);
        r.setPuntuacionTotal(new BigDecimal("4.0"));
        when(resultadoDao.findBySubmission(2)).thenReturn(Optional.of(r));

        Optional<Resultado> result = resultadoService.obtenerPorSubmission(2);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("4.0"), result.get().getPuntuacionTotal());
    }
}
