package com.quimbayaeval.service;

import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.model.Curso;
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

class CursoServiceValidationTest {

    @Mock
    private CursoDao cursoDao;

    @InjectMocks
    private CursoService cursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCrearWithValidCurso() {
        Curso c = new Curso();
        c.setCodigo("VALID");
        c.setNombre("Valid Course");
        when(cursoDao.save(c)).thenReturn(c);

        Curso result = cursoService.crear(c);
        assertNotNull(result);
        assertEquals("VALID", result.getCodigo());
        verify(cursoDao).save(c);
    }

    @Test
    void testCrearWithNullCurso() {
        // Debería manejar null o lanzar excepción
        assertThrows(Exception.class, () -> cursoService.crear(null));
    }

    @Test
    void testObtenerPorIdFound() {
        Curso expected = new Curso();
        expected.setId(1);
        expected.setCodigo("ABC");
        when(cursoDao.findById(1)).thenReturn(Optional.of(expected));

        Optional<Curso> result = cursoService.obtenerPorId(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
    }

    @Test
    void testObtenerPorIdNotFound() {
        when(cursoDao.findById(999)).thenReturn(Optional.empty());

        Optional<Curso> result = cursoService.obtenerPorId(999);
        assertFalse(result.isPresent());
    }

    @Test
    void testObtenerTodosSinPaginacion() {
        List<Curso> expected = Arrays.asList(new Curso(), new Curso(), new Curso());
        when(cursoDao.findAll()).thenReturn(expected);

        List<Curso> result = cursoService.obtenerTodos();
        assertEquals(3, result.size());
        verify(cursoDao).findAll();
    }

    @Test
    void testObtenerTodosConPaginacion() {
        List<Curso> expected = Arrays.asList(new Curso(), new Curso());
        Map<String, Object> filters = new HashMap<>();
        when(cursoDao.findAll(filters, 0, 2, "codigo", "ASC"))
                .thenReturn(expected);

        List<Curso> result = cursoService.obtenerTodos(filters, 0, 2, "codigo", "ASC");
        assertEquals(2, result.size());
        verify(cursoDao).findAll(filters, 0, 2, "codigo", "ASC");
    }

    @Test
    void testObtenerTodosConPaginacionVacía() {
        List<Curso> expected = Arrays.asList();
        Map<String, Object> filters = new HashMap<>();
        when(cursoDao.findAll(filters, 100, 10, null, null))
                .thenReturn(expected);

        List<Curso> result = cursoService.obtenerTodos(filters, 100, 10, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testActualizarCurso() {
        Curso c = new Curso();
        c.setId(1);
        c.setCodigo("UPDATED");

        cursoService.actualizar(c);
        verify(cursoDao).update(c);
    }

    @Test
    void testEliminarCurso() {
        cursoService.eliminar(5);
        verify(cursoDao).deleteById(5);
    }

    @Test
    void testObtenerDelProfesor() {
        List<Curso> expected = Arrays.asList(new Curso(), new Curso());
        when(cursoDao.findByProfesor(3)).thenReturn(expected);

        List<Curso> result = cursoService.obtenerDelProfesor(3);
        assertEquals(2, result.size());
    }

    @Test
    void testObtenerDelProfesorSinCursos() {
        when(cursoDao.findByProfesor(999)).thenReturn(Arrays.asList());

        List<Curso> result = cursoService.obtenerDelProfesor(999);
        assertTrue(result.isEmpty());
    }
}
