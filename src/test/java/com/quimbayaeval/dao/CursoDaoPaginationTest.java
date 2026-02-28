package com.quimbayaeval.dao;

import com.quimbayaeval.model.Curso;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class CursoDaoPaginationTest {

    @Autowired
    private CursoDao cursoDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM cursos");
        // insertar 12 cursos
        for (int i = 1; i <= 12; i++) {
            String sql = String.format(
                    "INSERT INTO cursos (codigo, nombre, descripcion, profesor_id) VALUES ('C%d', 'Curso %d', 'Desc %d', 1)",
                    i, i, i);
            jdbcTemplate.execute(sql);
        }
    }

    @Test
    void testFindAllNoPagination() {
        List<Curso> result = cursoDao.findAll();
        assertEquals(12, result.size());
    }

    @Test
    void testFindAllWithPaginationFirstPage() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 0, 5, null, null);
        assertEquals(5, result.size());
    }

    @Test
    void testFindAllWithPaginationSecondPage() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 1, 5, null, null);
        assertEquals(5, result.size());
    }

    @Test
    void testFindAllWithPaginationLastPage() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 2, 5, null, null);
        assertEquals(2, result.size());
    }

    @Test
    void testFindAllWithPaginationBeyondBounds() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 10, 5, null, null);
        assertEquals(0, result.size());
    }

    @Test
    void testFindAllWithSorting() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 0, 12, "codigo", "ASC");
        assertEquals(12, result.size());
        assertEquals("C1", result.get(0).getCodigo());
    }

    @Test
    void testFindAllWithSortingDescending() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 0, 12, "codigo", "DESC");
        assertEquals(12, result.size());
        assertEquals("C9", result.get(0).getCodigo());
    }

    @Test
    void testFindAllWithZeroPageSize() {
        Map<String, Object> filters = new HashMap<>();
        List<Curso> result = cursoDao.findAll(filters, 0, 0, null, null);
        assertEquals(0, result.size());
    }
}
