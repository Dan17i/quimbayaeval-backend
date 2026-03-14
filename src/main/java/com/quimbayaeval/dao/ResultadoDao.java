package com.quimbayaeval.dao;

import com.quimbayaeval.model.Resultado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ResultadoDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // JOIN con submissions para obtener evaluacion_id y estudiante_id
    private static final String SQL_BASE =
        "SELECT r.id, r.submission_id, s.evaluacion_id, s.estudiante_id, " +
        "r.puntuacion_total, r.puntuacion_maxima, r.porcentaje, " +
        "r.estado_aprobacion, r.observaciones, r.fecha_resultado, r.submission_id as created_at " +
        "FROM resultados r JOIN submissions s ON r.submission_id = s.id";

    private final RowMapper<Resultado> rowMapper = new RowMapper<Resultado>() {
        @Override
        public Resultado mapRow(ResultSet rs, int rowNum) throws SQLException {
            Resultado r = new Resultado();
            r.setId(rs.getInt("id"));
            r.setSubmissionId(rs.getInt("submission_id"));
            r.setEvaluacionId(rs.getInt("evaluacion_id"));
            r.setEstudianteId(rs.getInt("estudiante_id"));
            r.setPuntuacionTotal(rs.getBigDecimal("puntuacion_total"));
            r.setPuntuacionMaxima(rs.getBigDecimal("puntuacion_maxima"));
            r.setPorcentaje(rs.getBigDecimal("porcentaje"));
            r.setEstadoAprobacion(rs.getString("estado_aprobacion"));
            r.setObservaciones(rs.getString("observaciones"));
            r.setFechaResultado(rs.getTimestamp("fecha_resultado") != null ?
                rs.getTimestamp("fecha_resultado").toLocalDateTime() : null);
            return r;
        }
    };

    public List<Resultado> findByEstudiante(Integer estudianteId) {
        return jdbcTemplate.query(SQL_BASE + " WHERE s.estudiante_id = ? ORDER BY r.fecha_resultado DESC",
            rowMapper, estudianteId);
    }

    public List<Resultado> findByEvaluacion(Integer evaluacionId) {
        return jdbcTemplate.query(SQL_BASE + " WHERE s.evaluacion_id = ? ORDER BY r.fecha_resultado DESC",
            rowMapper, evaluacionId);
    }

    public Optional<Resultado> findBySubmission(Integer submissionId) {
        List<Resultado> list = jdbcTemplate.query(SQL_BASE + " WHERE r.submission_id = ?",
            rowMapper, submissionId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
