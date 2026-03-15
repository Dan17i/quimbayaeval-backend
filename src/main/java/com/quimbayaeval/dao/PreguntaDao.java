package com.quimbayaeval.dao;

import com.quimbayaeval.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO para entidad Pregunta
 */
@Repository
public class PreguntaDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json, created_at, updated_at FROM preguntas WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json, created_at, updated_at FROM preguntas";
    private static final String SQL_SELECT_BY_EVALUACION =
        "SELECT id, evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json, created_at, updated_at FROM preguntas WHERE evaluacion_id = ?";
    private static final String SQL_UPDATE =
        "UPDATE preguntas SET evaluacion_id = ?, enunciado = ?, tipo = ?, puntuacion = ?, orden = ?, opciones_json = ?, respuesta_correcta_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM preguntas WHERE id = ?";

    private final RowMapper<Pregunta> rowMapper = new RowMapper<Pregunta>() {
        @Override
        public Pregunta mapRow(ResultSet rs, int rowNum) throws SQLException {
            Pregunta p = new Pregunta();
            p.setId(rs.getInt("id"));
            p.setEvaluacionId(rs.getInt("evaluacion_id"));
            p.setEnunciado(rs.getString("enunciado"));
            p.setTipo(rs.getString("tipo"));
            p.setPuntuacion(rs.getDouble("puntuacion"));
            p.setOrden(rs.getInt("orden"));
            p.setOpcionesJson(rs.getString("opciones_json"));
            p.setRespuestaCorrectaJson(rs.getString("respuesta_correcta_json"));
            p.setCreatedAt(rs.getString("created_at"));
            p.setUpdatedAt(rs.getString("updated_at"));
            return p;
        }
    };

    public Pregunta save(Pregunta pregunta) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setInt(1, pregunta.getEvaluacionId());
            ps.setString(2, pregunta.getEnunciado());
            ps.setString(3, pregunta.getTipo());
            ps.setDouble(4, pregunta.getPuntuacion() != null ? pregunta.getPuntuacion() : 1.0);
            ps.setObject(5, pregunta.getOrden());
            ps.setString(6, pregunta.getOpcionesJson());
            ps.setString(7, pregunta.getRespuestaCorrectaJson());
            return ps;
        }, keyHolder);
        
        // Obtener el ID generado - compatible con H2 que retorna múltiples columnas
        if (keyHolder.getKeys() != null && keyHolder.getKeys().containsKey("ID")) {
            pregunta.setId(((Number) keyHolder.getKeys().get("ID")).intValue());
        } else if (keyHolder.getKey() != null) {
            pregunta.setId(keyHolder.getKey().intValue());
        } else {
            // Fallback: consultar el último ID insertado
            Integer lastId = jdbcTemplate.queryForObject(
                "SELECT id FROM preguntas WHERE evaluacion_id = ? AND enunciado = ? ORDER BY id DESC LIMIT 1",
                Integer.class,
                pregunta.getEvaluacionId(),
                pregunta.getEnunciado()
            );
            if (lastId != null) {
                pregunta.setId(lastId);
            }
        }
        return pregunta;
    }

    public Optional<Pregunta> findById(Integer id) {
        List<Pregunta> list = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Pregunta> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }

    /**
     * Convierte un Map en lista de criterios de igualdad (=)
     */
    private List<JdbcQueryBuilder.FilterCriteria> mapToCriteria(Map<String, Object> filters) {
        List<JdbcQueryBuilder.FilterCriteria> criterios = new ArrayList<>();
        if (filters != null) {
            filters.forEach((campo, valor) -> {
                criterios.add(new JdbcQueryBuilder.FilterCriteria(
                    campo,
                    JdbcQueryBuilder.FilterOperator.EQUALS, // operador por defecto
                    valor
                ));
            });
        }
        return criterios;
    }

    /**
     * Búsqueda dinámica de preguntas con filtros, ordenación y paginación.
     *
     * <p>Convierte el {@code Map<String,Object>} recibido en una lista de
     * {@link JdbcQueryBuilder.FilterCriteria}, usando por defecto el operador de igualdad (=).
     * Esto evita el uso del método deprecado y permite soportar operadores más avanzados
     * en el futuro (LIKE, BETWEEN, IN, etc.).</p>
     *
     * @param filters   Mapa de filtros simples (campo = valor). Se convierte internamente en criterios.
     * @param page      Número de página para la paginación (puede ser null).
     * @param size      Tamaño de página para la paginación (puede ser null).
     * @param sortBy    Campo por el cual ordenar los resultados.
     * @param direction Dirección de la ordenación ("ASC" o "DESC").
     * @return Lista de preguntas que cumplen con los filtros y la paginación indicada.
     */
    public List<Pregunta> findAll(Map<String, Object> filters,
                                Integer page,
                                Integer size,
                                String sortBy,
                                String direction) {
        List<JdbcQueryBuilder.FilterCriteria> criterios = mapToCriteria(filters);

        JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(
                SQL_SELECT_ALL, criterios, sortBy, direction, page, size);

        return jdbcTemplate.query(q.sql, rowMapper, q.args);
    }


    public List<Pregunta> findByEvaluacion(Integer evaluacionId) {
        return jdbcTemplate.query(SQL_SELECT_BY_EVALUACION, rowMapper, evaluacionId);
    }

    public void update(Pregunta pregunta) {
        jdbcTemplate.update(SQL_UPDATE,
            pregunta.getEvaluacionId(),
            pregunta.getEnunciado(),
            pregunta.getTipo(),
            pregunta.getPuntuacion(),
            pregunta.getOrden(),
            pregunta.getOpcionesJson(),
            pregunta.getRespuestaCorrectaJson(),
            pregunta.getId()
        );
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
