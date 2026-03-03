package com.quimbayaeval.service;

import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.model.Curso;
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

class CursoServiceTest {

    @Mock
    private CursoDao cursoDao;

    @InjectMocks
    private CursoService cursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void crear_callsDaoAndReturns() {
        Curso c = new Curso();
        c.setCodigo("ABC");
        when(cursoDao.save(c)).thenReturn(c);

        Curso result = cursoService.crear(c);
        assertSame(c, result);
        verify(cursoDao).save(c);
    }

    @Test
    void obtenerPorId_found() {
        Curso expected = new Curso();
        expected.setId(1);
        when(cursoDao.findById(1)).thenReturn(Optional.of(expected));

        Optional<Curso> opt = cursoService.obtenerPorId(1);
        assertTrue(opt.isPresent());
        assertEquals(1, opt.get().getId());
    }

    @Test
    void obtenerTodos_returnsList() {
        List<Curso> list = Arrays.asList(new Curso(), new Curso());
        when(cursoDao.findAll()).thenReturn(list);

        List<Curso> result = cursoService.obtenerTodos();
        assertEquals(2, result.size());
    }

    @Test
    void eliminar_delegatesToDao() {
        cursoService.eliminar(5);
        verify(cursoDao).deleteById(5);
    }
}
