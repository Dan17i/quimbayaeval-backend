package com.quimbayaeval.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.support.KeyHolder;

import java.util.Map;

/**
 * Utilidad transversal para la extracción segura del ID autogenerado desde un {@link KeyHolder}.
 * <p>
 * Resuelve la incompatibilidad entre distintos motores de base de datos (PostgreSQL, H2, MySQL, etc.)
 * y drivers JDBC donde una operación {@code INSERT} puede retornar un mapa con múltiples columnas
 * autogeneradas o con valores por defecto (por ejemplo: {@code id}, {@code created_at}, {@code updated_at}).
 * En tales casos, invocar directamente {@code keyHolder.getKey()} produce un error de tipo
 * {@link InvalidDataAccessApiUsageException} ("The getKey method should only be used when a single key is returned").
 * </p>
 */
@Slf4j
public final class KeyHolderUtils {

    private KeyHolderUtils() {
        // Prevenir instanciación directa de clase utilitaria
    }

    /**
     * Extrae de forma segura el identificador numérico autogenerado a partir del {@link KeyHolder}.
     *
     * <p>Estrategia de resolución:</p>
     * <ol>
     *   <li>Examina {@code keyHolder.getKeys()} buscando específicamente una columna llamada {@code "id"}
     *       (ignorando mayúsculas y minúsculas para compatibilidad universal entre PostgreSQL y H2).</li>
     *   <li>Si no existe una clave llamada {@code "id"}, busca cualquier valor dentro del mapa que sea una instancia de {@link Number}.</li>
     *   <li>Si no se encontraron llaves en el mapa, intenta de forma segura invocar {@code keyHolder.getKey()}.</li>
     * </ol>
     *
     * @param keyHolder contenedor provisto por Spring JDBC
     * @return el ID generado como {@link Integer}, o {@code null} si no se pudo determinar
     */
    public static Integer extractId(KeyHolder keyHolder) {
        if (keyHolder == null) {
            return null;
        }

        try {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && !keys.isEmpty()) {
                // 1. Búsqueda prioritaria por nombre de columna "id" (case-insensitive)
                for (Map.Entry<String, Object> entry : keys.entrySet()) {
                    if ("id".equalsIgnoreCase(entry.getKey()) && entry.getValue() instanceof Number) {
                        return ((Number) entry.getValue()).intValue();
                    }
                }

                // 2. Si no hay columna con nombre "id", buscar el primer Number disponible
                for (Object value : keys.values()) {
                    if (value instanceof Number) {
                        return ((Number) value).intValue();
                    }
                }
            }
        } catch (InvalidDataAccessApiUsageException e) {
            log.debug("No se pudo obtener el mapa getKeys() del KeyHolder: {}", e.getMessage());
        }

        // 3. Fallback seguro a getKey() cuando sólo existe una clave escalar
        try {
            Number key = keyHolder.getKey();
            if (key != null) {
                return key.intValue();
            }
        } catch (InvalidDataAccessApiUsageException e) {
            log.warn("Excepción al invocar keyHolder.getKey() debido a múltiples claves retornadas: {}", e.getMessage());
        }

        return null;
    }
}
