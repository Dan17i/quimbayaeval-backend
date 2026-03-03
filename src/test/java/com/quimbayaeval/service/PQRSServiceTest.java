package com.quimbayaeval.service;

import com.quimbayaeval.dao.PQRSDao;
import com.quimbayaeval.model.PQRS;
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

class PQRSServiceTest {

    @Mock
    private PQRSDao pqrsDao;

    @InjectMocks
    private PQRSService pqrsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crear_delegates() {
        PQRS p = new PQRS();
        when(pqrsDao.save(p)).thenReturn(p);
        PQRS res = pqrsService.crear(p);
        assertSame(p, res);
    }

    @Test
    void obtenerPorUsuario_callsDao() {
        List<PQRS> list = Arrays.asList(new PQRS());
        when(pqrsDao.findByUsuario(42)).thenReturn(list);
        List<PQRS> res = pqrsService.obtenerPorUsuario(42);
        assertEquals(1, res.size());
    }

    @Test
    void responder_changesEstado() {
        PQRS p = new PQRS();
        p.setId(5);
        when(pqrsDao.findById(5)).thenReturn(Optional.of(p));

        pqrsService.responder(5, "ok", 2);
        assertEquals("Resuelto", p.getEstado());
        assertEquals("ok", p.getRespuesta());
        verify(pqrsDao).update(p);
    }
}
