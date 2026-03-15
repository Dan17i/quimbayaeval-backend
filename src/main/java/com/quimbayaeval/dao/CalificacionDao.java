package com.quimbayaeval.dao;

import com.quimbayaeval.model.Calificacion;
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
 * DAO para Calificacion
 */
@Repository
public class CalificacionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO calificaciones (submission_id, pregunta_id, puntuacion_obtenida, puntuacion_maxima, retroalimentacion, calificado_por_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, submission_id, pregunta_id, puntuacion_obtenida, puntuacion_maxima, retroalimentacion, calificado_por_id, fecha_calificacion, created_at, updated_at FROM calificaciones WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, submission_id, pregunta_id, puntuacion_obtenida, puntuacion_maxima, retroalimentacion, calificado_por_id, fecha_calificacion, created_at, updated_at FROM calificaciones";
    private static final String SQL_SELECT_BY_SUBMISSION =
        "SELECT id, submission_id, pregunta_id, puntuacion_obtenida, puntuacion_maxima, retroalimentacion, calificado_por_id, fecha_calificacion, created_at, updated_at FROM calificaciones WHERE submission_id = ?";
    private static final String SQL_UPDATE =
        "UPDATE calificaciones SET submission_id = ?, pregunta_id = ?, puntuacion_obtenida = ?, puntuacion_maxima = ?, retroalimentacion = ?, calificado_por_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM calificaciones WHERE id = ?";

    private final RowMapper<Calificacion> rowMapper = new RowMapper<Calificacion>() {
        @Override
        public Calificacion mapRow(ResultSet rs, int rowNum) throws SQLException {
            Calificacion c = new Calificacion();
            c.setId(rs.getInt("id"));
            c.setSubmissionId(rs.getInt("submission_id"));
            c.setPreguntaId(rs.getInt("pregunta_id"));
            c.setPuntuacionObtenida(rs.getDouble("puntuacion_obtenida"));
            c.setPuntuacionMaxima(rs.getObject("puntuacion_maxima") != null ? rs.getDouble("puntuacion_maxima") : null);
            c.setRetroalimentacion(rs.getString("retroalimentacion"));
            c.setCalificadoPorId(rs.getObject("calificado_por_id") != null ? rs.getInt("calificado_por_id") : null);
            c.setFechaCalificacion(rs.getTimestamp("fecha_calificacion") != null ? rs.getTimestamp("fecha_calificacion").toLocalDateTime() : null);
            c.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            c.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return c;
        }
    };

    public Calificacion save(Calificacion cal) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setInt(1, cal.getSubmissionId());
            ps.setInt(2, cal.getPreguntaId());
            ps.setObject(3, cal.getPuntuacionObtenida());
            ps.setObject(4, cal.getPuntuacionMaxima());
            ps.setString(5, cal.getRetroalimentacion());
            ps.setObject(6, cal.getCalificadoPorId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            cal.setId(keyHolder.getKey().intValue());
        }
        return cal;
    }

    public Optional<Calificacion> findById(Integer id) {
        List<Calificacion> list = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Calificacion> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }

/**
 * Crea lista de criterios a partir de un Map simple
 */
private List<JdbcQueryBuilder.FilterCriteria> mapToCriteria(Map<String, Object> filters) {
    List<JdbcQueryBuilder.FilterCriteria> criterios = new ArrayList<>();
    if (filters != null) {
        filters.forEach((campo, valor) -> {
            criterios.add(new JdbcQueryBuilder.FilterCriteria(
                campo,
                JdbcQueryBuilder.FilterOperator.EQUALS, // operador por defecto (=)
                valor
            ));
        });
    }
    return criterios;
}

/**
 * Obtiene calificaciones con paginación/orden/filtros
 */
public List<Calificacion> findAll(Map<String, Object> filters,
                                  Integer page,
                                  Integer size,
                                  String sortBy,
                                  String direction) {
    List<JdbcQueryBuilder.FilterCriteria> criterios = mapToCriteria(filters);

    JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(
            SQL_SELECT_ALL, criterios, sortBy, direction, page, size);

    return jdbcTemplate.query(q.sql, rowMapper, q.args);
}


    public List<Calificacion> findBySubmission(Integer submissionId) {
        return jdbcTemplate.query(SQL_SELECT_BY_SUBMISSION, rowMapper, submissionId);
    }

    public void update(Calificacion cal) {
        jdbcTemplate.update(SQL_UPDATE,
            cal.getSubmissionId(),
            cal.getPreguntaId(),
            cal.getPuntuacionObtenida(),
            cal.getPuntuacionMaxima(),
            cal.getRetroalimentacion(),
            cal.getCalificadoPorId(),
            cal.getId()
        );
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
