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
 * DAO para la entidad Submission.
 * Administra los envíos e intentos de respuesta de evaluaciones por parte de los estudiantes.
 */
@Repository
public class SubmissionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO submissions (evaluacion_id, estudiante_id, fecha_inicio, estado, intento_numero, respuestas_json, fecha_envio) VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, evaluacion_id, estudiante_id, respuestas_json, estado, intento_numero, fecha_inicio, fecha_envio, created_at, updated_at FROM submissions WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, evaluacion_id, estudiante_id, respuestas_json, estado, intento_numero, fecha_inicio, fecha_envio, created_at, updated_at FROM submissions";
    private static final String SQL_SELECT_BY_EVALUACION =
        "SELECT id, evaluacion_id, estudiante_id, respuestas_json, estado, intento_numero, fecha_inicio, fecha_envio, created_at, updated_at FROM submissions WHERE evaluacion_id = ?";
    private static final String SQL_SELECT_BY_ESTUDIANTE =
        "SELECT id, evaluacion_id, estudiante_id, respuestas_json, estado, intento_numero, fecha_inicio, fecha_envio, created_at, updated_at FROM submissions WHERE estudiante_id = ?";
    private static final String SQL_UPDATE =
        "UPDATE submissions SET evaluacion_id = ?, estudiante_id = ?, respuestas_json = ?, estado = ?, intento_numero = ?, fecha_envio = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM submissions WHERE id = ?";

    private final RowMapper<Submission> rowMapper = new RowMapper<Submission>() {
        @Override
        public Submission mapRow(ResultSet rs, int rowNum) throws SQLException {
            Submission s = new Submission();
            s.setId(rs.getInt("id"));
            s.setEvaluacionId(rs.getInt("evaluacion_id"));
            s.setEstudianteId(rs.getInt("estudiante_id"));
            s.setRespuestasJson(rs.getString("respuestas_json"));
            s.setEstado(rs.getString("estado"));
            s.setIntentoNumero(rs.getInt("intento_numero"));
            s.setFechaInicio(rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toLocalDateTime() : null);
            s.setFechaEnvio(rs.getTimestamp("fecha_envio") != null ? rs.getTimestamp("fecha_envio").toLocalDateTime() : null);
            s.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            s.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return s;
        }
    };

    public Submission save(Submission sub) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, new String[]{"id"});
            ps.setInt(1, sub.getEvaluacionId());
            ps.setInt(2, sub.getEstudianteId());
            ps.setString(3, sub.getEstado() != null ? sub.getEstado() : "Borrador");
            ps.setInt(4, sub.getIntentoNumero() != null ? sub.getIntentoNumero() : 1);
            ps.setString(5, sub.getRespuestasJson());
            ps.setTimestamp(6, sub.getFechaEnvio() != null ? java.sql.Timestamp.valueOf(sub.getFechaEnvio()) : null);
            return ps;
        }, keyHolder);
        Integer generatedId = KeyHolderUtils.extractId(keyHolder);
        if (generatedId != null) {
            sub.setId(generatedId);
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

    public int countByEvaluacionAndEstudiante(Integer evaluacionId, Integer estudianteId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM submissions WHERE evaluacion_id = ? AND estudiante_id = ?",
            Integer.class, evaluacionId, estudianteId
        );
        return count != null ? count : 0;
    }

    public void update(Submission sub) {
        jdbcTemplate.update(SQL_UPDATE,
            sub.getEvaluacionId(),
            sub.getEstudianteId(),
            sub.getRespuestasJson(),
            sub.getEstado(),
            sub.getIntentoNumero(),
            sub.getFechaEnvio(),
            sub.getId()
        );
    }

    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }

    private static final String SQL_SELECT_DETALLE_BASE =
        "SELECT s.id, s.evaluacion_id, s.estudiante_id, s.respuestas_json, s.estado, s.intento_numero, " +
        "s.fecha_inicio, s.fecha_envio, s.created_at, s.updated_at, " +
        "u.name AS estudiante_nombre, u.email AS estudiante_email, " +
        "e.nombre AS evaluacion_nombre, e.curso_id, c.nombre AS curso_nombre, c.codigo AS curso_codigo, " +
        "r.puntuacion_total, r.puntuacion_maxima, r.porcentaje, " +
        "ROUND(CAST(1 + (r.porcentaje / 100.0) * 4 AS numeric), 2) AS nota_escala, " +
        "r.estado_aprobacion, r.observaciones " +
        "FROM submissions s " +
        "JOIN users u ON s.estudiante_id = u.id " +
        "JOIN evaluaciones e ON s.evaluacion_id = e.id " +
        "JOIN cursos c ON e.curso_id = c.id " +
        "LEFT JOIN resultados r ON r.submission_id = s.id";

    private final RowMapper<com.quimbayaeval.model.dto.SubmissionDetalleDTO> detalleRowMapper = new RowMapper<>() {
        @Override
        public com.quimbayaeval.model.dto.SubmissionDetalleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            com.quimbayaeval.model.dto.SubmissionDetalleDTO d = new com.quimbayaeval.model.dto.SubmissionDetalleDTO();
            d.setId(rs.getInt("id"));
            d.setEvaluacionId(rs.getInt("evaluacion_id"));
            d.setEvaluacionNombre(rs.getString("evaluacion_nombre"));
            d.setCursoId(rs.getInt("curso_id"));
            d.setCursoNombre(rs.getString("curso_nombre"));
            d.setCursoCodigo(rs.getString("curso_codigo"));
            d.setEstudianteId(rs.getInt("estudiante_id"));
            d.setEstudianteNombre(rs.getString("estudiante_nombre"));
            d.setEstudianteEmail(rs.getString("estudiante_email"));
            d.setRespuestasJson(rs.getString("respuestas_json"));
            d.setEstado(rs.getString("estado"));
            d.setIntentoNumero(rs.getInt("intento_numero"));
            d.setFechaInicio(rs.getTimestamp("fecha_inicio") != null ? rs.getTimestamp("fecha_inicio").toLocalDateTime() : null);
            d.setFechaEnvio(rs.getTimestamp("fecha_envio") != null ? rs.getTimestamp("fecha_envio").toLocalDateTime() : null);
            d.setPuntuacionTotal(rs.getBigDecimal("puntuacion_total"));
            d.setPuntuacionMaxima(rs.getBigDecimal("puntuacion_maxima"));
            d.setPorcentaje(rs.getBigDecimal("porcentaje"));
            d.setNotaEscala(rs.getBigDecimal("nota_escala"));
            d.setEstadoAprobacion(rs.getString("estado_aprobacion"));
            d.setObservaciones(rs.getString("observaciones"));
            d.setCreatedAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null);
            d.setUpdatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return d;
        }
    };

    public List<com.quimbayaeval.model.dto.SubmissionDetalleDTO> findDetallesByEvaluacion(Integer evaluacionId) {
        return jdbcTemplate.query(
            SQL_SELECT_DETALLE_BASE + " WHERE s.evaluacion_id = ? ORDER BY u.name ASC, s.intento_numero DESC",
            detalleRowMapper, evaluacionId
        );
    }

    public Optional<com.quimbayaeval.model.dto.SubmissionDetalleDTO> findDetalleById(Integer id) {
        List<com.quimbayaeval.model.dto.SubmissionDetalleDTO> list = jdbcTemplate.query(
            SQL_SELECT_DETALLE_BASE + " WHERE s.id = ?",
            detalleRowMapper, id
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<com.quimbayaeval.model.dto.SubmissionDetalleDTO> findDetallesByEstudiante(Integer estudianteId) {
        return jdbcTemplate.query(
            SQL_SELECT_DETALLE_BASE + " WHERE s.estudiante_id = ? ORDER BY s.created_at DESC",
            detalleRowMapper, estudianteId
        );
    }
}
