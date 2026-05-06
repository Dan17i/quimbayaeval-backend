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
        "id, tipo, asunto, descripcion, curso_id, usuario_id, estado, destinatario, " +
        "fecha_creacion, fecha_respuesta, respuesta, respondido_por_id, updated_at, created_at";

    private static final String SQL_INSERT =
        "INSERT INTO pqrs (tipo, asunto, descripcion, curso_id, usuario_id, estado, destinatario, fecha_creacion, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

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

    // PQRS visibles para un maestro: destinatario='maestro' y curso pertenece al maestro
    private static final String SQL_SELECT_PARA_MAESTRO =
        "SELECT p.id, p.tipo, p.asunto, p.descripcion, p.curso_id, p.usuario_id, p.estado, p.destinatario, " +
        "p.fecha_creacion, p.fecha_respuesta, p.respuesta, p.respondido_por_id, p.updated_at, p.created_at " +
        "FROM pqrs p " +
        "JOIN cursos c ON p.curso_id = c.id " +
        "WHERE p.destinatario = 'maestro' AND c.profesor_id = ? " +
        "ORDER BY p.fecha_creacion DESC";

    private static final String SQL_UPDATE =
        "UPDATE pqrs SET tipo = ?, asunto = ?, descripcion = ?, curso_id = ?, estado = ?, " +
        "destinatario = ?, respuesta = ?, respondido_por_id = ?, " +
        "fecha_respuesta = CASE WHEN respuesta IS NOT NULL THEN CURRENT_TIMESTAMP ELSE fecha_respuesta END, " +
        "updated_at = CURRENT_TIMESTAMP WHERE id = ?";

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
            pqrs.setDestinatario(rs.getString("destinatario"));
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

    public PQRS save(PQRS pqrs) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setString(1, pqrs.getTipo());
            ps.setString(2, pqrs.getAsunto());
            ps.setString(3, pqrs.getDescripcion());
            ps.setObject(4, pqrs.getCursoId());
            ps.setInt(5, pqrs.getUsuarioId());
            ps.setString(6, pqrs.getEstado() != null ? pqrs.getEstado() : "Pendiente");
            ps.setString(7, pqrs.getDestinatario() != null ? pqrs.getDestinatario() : "maestro");
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            pqrs.setId(keyHolder.getKey().intValue());
        }
        return pqrs;
    }

    public Optional<PQRS> findById(Integer id) {
        List<PQRS> list = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<PQRS> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL + " ORDER BY fecha_creacion DESC", rowMapper);
    }

    /** PQRS visibles para un maestro: destinatario='maestro' en sus propios cursos. */
    public List<PQRS> findParaMaestro(Integer maestroId) {
        return jdbcTemplate.query(SQL_SELECT_PARA_MAESTRO, rowMapper, maestroId);
    }

    private List<JdbcQueryBuilder.FilterCriteria> mapToCriteria(Map<String, Object> filters) {
        List<JdbcQueryBuilder.FilterCriteria> criterios = new ArrayList<>();
        if (filters != null) {
            filters.forEach((campo, valor) -> criterios.add(
                new JdbcQueryBuilder.FilterCriteria(campo, JdbcQueryBuilder.FilterOperator.EQUALS, valor)
            ));
        }
        return criterios;
    }

    public List<PQRS> findAll(Map<String, Object> filters, Integer page, Integer size,
                              String sortBy, String direction) {
        List<JdbcQueryBuilder.FilterCriteria> criterios = mapToCriteria(filters);
        JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(SQL_SELECT_ALL, criterios, sortBy, direction, page, size);
        return jdbcTemplate.query(q.sql, rowMapper, q.args);
    }

    public List<PQRS> findByUsuario(Integer usuarioId) {
        return jdbcTemplate.query(SQL_SELECT_BY_USUARIO, rowMapper, usuarioId);
    }

    public List<PQRS> findByEstado(String estado) {
        return jdbcTemplate.query(SQL_SELECT_BY_ESTADO, rowMapper, estado);
    }

    public List<PQRS> findByTipo(String tipo) {
        return jdbcTemplate.query(SQL_SELECT_BY_TIPO, rowMapper, tipo);
    }

    public void update(PQRS pqrs) {
        jdbcTemplate.update(SQL_UPDATE,
            pqrs.getTipo(),
            pqrs.getAsunto(),
            pqrs.getDescripcion(),
            pqrs.getCursoId(),
            pqrs.getEstado(),
            pqrs.getDestinatario() != null ? pqrs.getDestinatario() : "maestro",
            pqrs.getRespuesta(),
            pqrs.getRespondidoPorId(),
            pqrs.getId()
        );
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
