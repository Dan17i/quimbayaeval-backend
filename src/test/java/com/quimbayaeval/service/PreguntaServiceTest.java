package com.quimbayaeval.service;

import com.quimbayaeval.dao.PreguntaDao;
import com.quimbayaeval.model.Pregunta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PreguntaServiceTest {

    @Mock
    private PreguntaDao preguntaDao;

    @InjectMocks
    private PreguntaService preguntaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crear_delegates() {
        Pregunta p = new Pregunta();
        when(preguntaDao.save(p)).thenReturn(p);
        Pregunta res = preguntaService.crear(p);
        assertSame(p, res);
    }

    @Test
    void obtenerPorEvaluacion_callsDao() {
        List<Pregunta> list = Arrays.asList(new Pregunta());
        when(preguntaDao.findByEvaluacion(3)).thenReturn(list);
        List<Pregunta> res = preguntaService.obtenerPorEvaluacion(3);
        assertEquals(1, res.size());
    }
}
