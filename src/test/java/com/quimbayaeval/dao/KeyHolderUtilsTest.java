package com.quimbayaeval.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas unitarias para KeyHolderUtils")
class KeyHolderUtilsTest {

    @Test
    @DisplayName("Debe retornar null cuando el KeyHolder es nulo")
    void testExtractIdNullKeyHolder() {
        assertNull(KeyHolderUtils.extractId(null));
    }

    @Test
    @DisplayName("Debe retornar null cuando el KeyHolder está vacío")
    void testExtractIdEmptyKeyHolder() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        assertNull(KeyHolderUtils.extractId(keyHolder));
    }

    @Test
    @DisplayName("Debe extraer ID en minúsculas 'id' cuando hay múltiples columnas retornadas (escenario PostgreSQL)")
    void testExtractIdPostgresMultipleColumnsLowerCase() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> map = new HashMap<>();
        map.put("id", 42);
        map.put("created_at", LocalDateTime.now());
        map.put("updated_at", LocalDateTime.now());
        keyHolder.getKeyList().add(map);

        Integer extractedId = KeyHolderUtils.extractId(keyHolder);
        assertNotNull(extractedId);
        assertEquals(42, extractedId);
    }

    @Test
    @DisplayName("Debe extraer ID en mayúsculas 'ID' cuando hay múltiples columnas retornadas (escenario H2 / Oracle)")
    void testExtractIdUpperCaseId() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> map = new HashMap<>();
        map.put("ID", 108L);
        map.put("STATUS", "ACTIVE");
        keyHolder.getKeyList().add(map);

        Integer extractedId = KeyHolderUtils.extractId(keyHolder);
        assertNotNull(extractedId);
        assertEquals(108, extractedId);
    }

    @Test
    @DisplayName("Debe extraer el primer número disponible si ninguna columna se llama 'id'")
    void testExtractIdFirstNumberFallback() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        Map<String, Object> map = new HashMap<>();
        map.put("generated_key", 99);
        map.put("description", "test");
        keyHolder.getKeyList().add(map);

        Integer extractedId = KeyHolderUtils.extractId(keyHolder);
        assertNotNull(extractedId);
        assertEquals(99, extractedId);
    }

    @Test
    @DisplayName("Debe extraer ID cuando sólo hay una clave escalar")
    void testExtractIdSingleScalarKey() {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        keyHolder.getKeyList().add(Map.of("id", 77));

        Integer extractedId = KeyHolderUtils.extractId(keyHolder);
        assertNotNull(extractedId);
        assertEquals(77, extractedId);
    }
}
