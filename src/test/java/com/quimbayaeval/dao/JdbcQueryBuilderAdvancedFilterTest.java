package com.quimbayaeval.dao;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para JdbcQueryBuilder con filtros avanzados
 */
public class JdbcQueryBuilderAdvancedFilterTest {

    @Test
    void testBuildWithFilterEquals() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("estado", JdbcQueryBuilder.FilterOperator.EQUALS, "Activa")
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                "nombre",
                "ASC",
                0,
                10
        );

        assertTrue(result.sql.contains("WHERE estado = ?"));
        assertTrue(result.sql.contains("ORDER BY nombre ASC"));
        assertTrue(result.sql.contains("LIMIT ? OFFSET ?"));
        assertEquals(3, result.args.length);  // estado, limit, offset
        assertEquals("Activa", result.args[0]);
        assertEquals(10, result.args[1]);
        assertEquals(0, result.args[2]);
    }

    @Test
    void testBuildWithFilterLike() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("nombre", JdbcQueryBuilder.FilterOperator.LIKE, "Parcial")
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE nombre LIKE ?"));
        assertEquals(1, result.args.length);
        assertEquals("%Parcial%", result.args[0]);
    }

    @Test
    void testBuildWithFilterILike() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("nombre", JdbcQueryBuilder.FilterOperator.ILIKE, "cálculo")
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE nombre ILIKE ?"));
        assertEquals("%cálculo%", result.args[0]);
    }

    @Test
    void testBuildWithFilterGT() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("duracion_minutos", JdbcQueryBuilder.FilterOperator.GT, 60)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE duracion_minutos > ?"));
        assertEquals(60, result.args[0]);
    }

    @Test
    void testBuildWithFilterBetween() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("duracion_minutos", 
                        JdbcQueryBuilder.FilterOperator.BETWEEN, 30, 120)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE duracion_minutos BETWEEN ? AND ?"));
        assertEquals(30, result.args[0]);
        assertEquals(120, result.args[1]);
    }

    @Test
    void testBuildWithFilterIN() {
        List<Integer> valores = Arrays.asList(1, 2, 3);
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("curso_id", JdbcQueryBuilder.FilterOperator.IN, valores)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE curso_id IN (?,?,?)"));
        assertEquals(3, result.args.length);
        assertEquals(1, result.args[0]);
        assertEquals(2, result.args[1]);
        assertEquals(3, result.args[2]);
    }

    @Test
    void testBuildWithMultipleFilters() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("estado", JdbcQueryBuilder.FilterOperator.EQUALS, "Activa"),
                new JdbcQueryBuilder.FilterCriteria("tipo", JdbcQueryBuilder.FilterOperator.EQUALS, "Examen"),
                new JdbcQueryBuilder.FilterCriteria("nombre", JdbcQueryBuilder.FilterOperator.LIKE, "Parcial")
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                "nombre",
                "DESC",
                0,
                20
        );

        assertTrue(result.sql.contains("WHERE estado = ? AND tipo = ? AND nombre LIKE ?"));
        assertTrue(result.sql.contains("ORDER BY nombre DESC"));
        assertTrue(result.sql.contains("LIMIT ? OFFSET ?"));
        assertEquals(5, result.args.length);
        assertEquals("Activa", result.args[0]);
        assertEquals("Examen", result.args[1]);
        assertEquals("%Parcial%", result.args[2]);
        assertEquals(20, result.args[3]);
        assertEquals(0, result.args[4]);
    }

    @Test
    void testBuildWithFilterIsNull() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("descripcion", JdbcQueryBuilder.FilterOperator.IS_NULL, null)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE descripcion IS NULL"));
        assertEquals(0, result.args.length);
    }

    @Test
    void testBuildWithFilterIsNotNull() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("deadline", JdbcQueryBuilder.FilterOperator.IS_NOT_NULL, null)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("WHERE deadline IS NOT NULL"));
        assertEquals(0, result.args.length);
    }

    @Test
    void testBuildInvalidColumnName() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("'; DROP TABLE evaluaciones; --", 
                        JdbcQueryBuilder.FilterOperator.EQUALS, "Activa")
        );

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcQueryBuilder.build(
                    "SELECT * FROM evaluaciones",
                    filters,
                    null,
                    null,
                    null,
                    null
            );
        });
    }

    @Test
    void testBuildInvalidSortByName() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList();

        assertThrows(IllegalArgumentException.class, () -> {
            JdbcQueryBuilder.build(
                    "SELECT * FROM evaluaciones",
                    filters,
                    "nombre); DELETE FROM evaluaciones WHERE 1=1; SELECT (", 
                    "ASC",
                    null,
                    null
            );
        });
    }

    @Test
        void testBuildWithNullFilters() {
        List<JdbcQueryBuilder.FilterCriteria> criterios = new ArrayList<>(); // lista vacía

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                criterios,
                "id",
                "ASC",
                0,
                10
        );

        assertTrue(result.sql.contains("SELECT * FROM evaluaciones"));
        assertTrue(result.sql.contains("ORDER BY id ASC"));
        assertTrue(result.sql.contains("LIMIT ? OFFSET ?"));
        }


    @Test
    void testBuildWithEmptyFilters() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList();

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                "nombre",
                "DESC",
                0,
                5
        );

        assertFalse(result.sql.contains("WHERE"));
        assertTrue(result.sql.contains("ORDER BY nombre DESC"));
        assertEquals(2, result.args.length);  // solo limit y offset
    }

    @Test
    void testBuildFilterLTEAndGTE() {
        List<JdbcQueryBuilder.FilterCriteria> filters = Arrays.asList(
                new JdbcQueryBuilder.FilterCriteria("duracion_minutos", 
                        JdbcQueryBuilder.FilterOperator.GTE, 30),
                new JdbcQueryBuilder.FilterCriteria("duracion_minutos", 
                        JdbcQueryBuilder.FilterOperator.LTE, 120)
        );

        JdbcQueryBuilder.QueryData result = JdbcQueryBuilder.build(
                "SELECT * FROM evaluaciones",
                filters,
                null,
                null,
                null,
                null
        );

        assertTrue(result.sql.contains("duracion_minutos >= ?"));
        assertTrue(result.sql.contains("duracion_minutos <= ?"));
    }
}
