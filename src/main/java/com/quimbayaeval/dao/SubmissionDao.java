package com.quimbayaeval.dao;

import com.quimbayaeval.model.Submission;
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
 * DAO para Submission
 */
@Repository
public class SubmissionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO submissions (evaluacion_id, estudiante_id, fecha_inicio, estado) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, evaluacion_id, estudiante_id, fecha_inicio, fecha_finalizacion, estado FROM submissions WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, evaluacion_id, estudiante_id, fecha_inicio, fecha_finalizacion, estado FROM submissions";
    private static final String SQL_SELECT_BY_EVALUACION =
        "SELECT id, evaluacion_id, estudiante_id, fecha_inicio, fecha_finalizacion, estado FROM submissions WHERE evaluacion_id = ?";
    private static final String SQL_SELECT_BY_ESTUDIANTE =
        "SELECT id, evaluacion_id, estudiante_id, fecha_inicio, fecha_finalizacion, estado FROM submissions WHERE estudiante_id = ?";
    private static final String SQL_UPDATE =
        "UPDATE submissions SET evaluacion_id = ?, estudiante_id = ?, fecha_inicio = ?, fecha_finalizacion = ?, estado = ? WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM submissions WHERE id = ?";

    private final RowMapper<Submission> rowMapper = new RowMapper<Submission>() {
        @Override
        public Submission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Submission s = new Submission();
            s.setId(rs.getInt("id"));
            s.setEvaluacionId(rs.getInt("evaluacion_id"));
            s.setEstudianteId(rs.getInt("estudiante_id"));
            s.setFechaInicio(rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toLocalDateTime() : null);
            s.setFechaFinalizacion(rs.getTimestamp("fecha_finalizacion") != null ? rs.getTimestamp("fecha_finalizacion").toLocalDateTime() : null);
            s.setEstado(rs.getString("estado"));
            return s;
        }
    };

    public Submission save(Submission sub) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, sub.getEvaluacionId());
            ps.setInt(2, sub.getEstudianteId());
            ps.setString(3, sub.getEstado());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            sub.setId(keyHolder.getKey().intValue());
        }
        return sub;
    }

    public Optional<Submission> findById(Integer id) {
        List<Submission> list = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Submission> findAll() {
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
     * Consulta genérica de submissions con filtros, ordenación y paginación.
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
     * @return Lista de submissions que cumplen con los filtros y la paginación indicada.
     */
    public List<Submission> findAll(Map<String, Object> filters,
                                    Integer page,
                                    Integer size,
                                    String sortBy,
                                    String direction) {
        List<JdbcQueryBuilder.FilterCriteria> criterios = mapToCriteria(filters);

        JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(
                SQL_SELECT_ALL, criterios, sortBy, direction, page, size);

        return jdbcTemplate.query(q.sql, rowMapper, q.args);
    }

    public List<Submission> findByEvaluacion(Integer evaluacionId) {
        return jdbcTemplate.query(SQL_SELECT_BY_EVALUACION, rowMapper, evaluacionId);
    }

    public List<Submission> findByEstudiante(Integer estudianteId) {
        return jdbcTemplate.query(SQL_SELECT_BY_ESTUDIANTE, rowMapper, estudianteId);
    }

    public void update(Submission sub) {
        jdbcTemplate.update(SQL_UPDATE,
            sub.getEvaluacionId(),
            sub.getEstudianteId(),
            sub.getFechaInicio(),
            sub.getFechaFinalizacion(),
            sub.getEstado(),
            sub.getId()
        );
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
