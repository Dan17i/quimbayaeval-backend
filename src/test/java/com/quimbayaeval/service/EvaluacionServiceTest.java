package com.quimbayaeval.service;

import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.model.Evaluacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluacionServiceTest {

    @Mock
    private EvaluacionDao evaluacionDao;

    @InjectMocks
    private EvaluacionService evaluacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crear_delegates() {
        Evaluacion e = new Evaluacion();
        when(evaluacionDao.save(e)).thenReturn(e);
        Evaluacion res = evaluacionService.crear(e);
        assertSame(e, res);
    }

    @Test
    void obtenerActivas_callsDao() {
        List<Evaluacion> list = Arrays.asList(new Evaluacion());
        when(evaluacionDao.findByEstado("Activa")).thenReturn(list);
        List<Evaluacion> res = evaluacionService.obtenerActivas();
        assertEquals(1, res.size());
    }

    @Test
    void publicar_setsEstadoAndUpdates() {
        Evaluacion e = new Evaluacion();
        e.setId(10);
        e.setEstado("Borrador");
        when(evaluacionDao.findById(10)).thenReturn(Optional.of(e));

        evaluacionService.publicar(10);
        assertEquals("Activa", e.getEstado());
        verify(evaluacionDao).update(e);
    }
}
