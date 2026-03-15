package com.quimbayaeval.dao;

import com.quimbayaeval.model.PQRS;
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
 * DAO para entidad PQRS
 */
@Repository
public class PQRSDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_COLS =
        "id, tipo, asunto, descripcion, curso_id, usuario_id, estado, fecha_creacion, fecha_respuesta, respuesta, respondido_por_id, updated_at, created_at";
    private static final String SQL_INSERT =
        "INSERT INTO pqrs (tipo, asunto, descripcion, curso_id, usuario_id, estado, fecha_creacion, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT " + SQL_COLS + " FROM pqrs WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT " + SQL_COLS + " FROM pqrs";
    private static final String SQL_SELECT_BY_USUARIO =
        "SELECT " + SQL_COLS + " FROM pqrs WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
    private static final String SQL_SELECT_BY_ESTADO =
        "SELECT " + SQL_COLS + " FROM pqrs WHERE estado = ? ORDER BY fecha_creacion DESC";
    private static final String SQL_SELECT_BY_TIPO =
        "SELECT " + SQL_COLS + " FROM pqrs WHERE tipo = ? ORDER BY fecha_creacion DESC";
    private static final String SQL_UPDATE =
        "UPDATE pqrs SET tipo = ?, asunto = ?, descripcion = ?, curso_id = ?, estado = ?, respuesta = ?, respondido_por_id = ?, fecha_respuesta = CASE WHEN respuesta IS NOT NULL THEN CURRENT_TIMESTAMP ELSE fecha_respuesta END, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM pqrs WHERE id = ?";

    private final RowMapper<PQRS> rowMapper = new RowMapper<PQRS>() {
        @Override
        public PQRS mapRow(ResultSet rs, int rowNum) throws SQLException {
            PQRS pqrs = new PQRS();
            pqrs.setId(rs.getInt("id"));
            pqrs.setTipo(rs.getString("tipo"));
            pqrs.setAsunto(rs.getString("asunto"));
            pqrs.setDescripcion(rs.getString("descripcion"));
            pqrs.setCursoId(rs.getObject("curso_id") != null ? rs.getInt("curso_id") : null);
            pqrs.setUsuarioId(rs.getInt("usuario_id"));
            pqrs.setEstado(rs.getString("estado"));
            pqrs.setFechaCreacion(rs.getTimestamp("fecha_creacion") != null ?
                    rs.getTimestamp("fecha_creacion").toLocalDateTime() : null);
            pqrs.setFechaRespuesta(rs.getTimestamp("fecha_respuesta") != null ?
                    rs.getTimestamp("fecha_respuesta").toLocalDateTime() : null);
            pqrs.setRespuesta(rs.getString("respuesta"));
            pqrs.setRespondidoPorId(rs.getObject("respondido_por_id") != null ? rs.getInt("respondido_por_id") : null);
            pqrs.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
            pqrs.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
            return pqrs;
        }
    };

    /**
     * Inserta un nuevo PQRS
     */
    public PQRS save(PQRS pqrs) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setString(1, pqrs.getTipo());
            ps.setString(2, pqrs.getAsunto());
            ps.setString(3, pqrs.getDescripcion());
            ps.setObject(4, pqrs.getCursoId());
            ps.setInt(5, pqrs.getUsuarioId());
            ps.setString(6, pqrs.getEstado());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            pqrs.setId(keyHolder.getKey().intValue());
        }
        return pqrs;
    }

    /**
     * Encuentra PQRS por ID
     */
    public Optional<PQRS> findById(Integer id) {
        List<PQRS> pqrsList = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return pqrsList.isEmpty() ? Optional.empty() : Optional.of(pqrsList.get(0));
    }

    /**
     * Obtiene todos los PQRS
     */
    public List<PQRS> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL + " ORDER BY fecha_creacion DESC", rowMapper);
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
 * Obtiene tickets con criterios dinámicos, ordenación y paginación.
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
 * @return Lista de tickets PQRS que cumplen con los filtros y la paginación indicada.
 */
public List<PQRS> findAll(Map<String, Object> filters,
                          Integer page,
                          Integer size,
                          String sortBy,
                          String direction) {
    List<JdbcQueryBuilder.FilterCriteria> criterios = mapToCriteria(filters);

    JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(
            SQL_SELECT_ALL, criterios, sortBy, direction, page, size);

    return jdbcTemplate.query(q.sql, rowMapper, q.args);
}

    /**
     * Obtiene PQRS por usuario
     */
    public List<PQRS> findByUsuario(Integer usuarioId) {
        return jdbcTemplate.query(SQL_SELECT_BY_USUARIO, rowMapper, usuarioId);
    }

    /**
     * Obtiene PQRS por estado
     */
    public List<PQRS> findByEstado(String estado) {
        return jdbcTemplate.query(SQL_SELECT_BY_ESTADO, rowMapper, estado);
    }

    /**
     * Obtiene PQRS por tipo
     */
    public List<PQRS> findByTipo(String tipo) {
        return jdbcTemplate.query(SQL_SELECT_BY_TIPO, rowMapper, tipo);
    }

    /**
     * Actualiza un PQRS
     */
    public void update(PQRS pqrs) {
        jdbcTemplate.update(SQL_UPDATE,
            pqrs.getTipo(),
            pqrs.getAsunto(),
            pqrs.getDescripcion(),
            pqrs.getCursoId(),
            pqrs.getEstado(),
            pqrs.getRespuesta(),
            pqrs.getRespondidoPorId(),
            pqrs.getId()
        );
    }

    /**
     * Elimina un PQRS
     */
    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
