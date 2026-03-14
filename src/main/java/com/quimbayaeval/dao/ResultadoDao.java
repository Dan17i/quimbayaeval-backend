package com.quimbayaeval.dao;

import com.quimbayaeval.model.Resultado;
import com.quimbayaeval.model.dto.ResumenCursoDTO;
import com.quimbayaeval.model.dto.ResultadoDetalleDTO;
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

    /**
     * Retorna el detalle completo de resultados por curso:
     * nota de cada estudiante con su nombre, email y nombre de evaluación.
     * Usado por docentes.
     */
    public List<ResultadoDetalleDTO> findDetallesByCurso(Integer cursoId) {
        String sql =
            "SELECT r.id, r.submission_id, u.id as estudiante_id, u.name as estudiante_nombre, " +
            "u.email as estudiante_email, e.id as evaluacion_id, e.nombre as evaluacion_nombre, " +
            "c.nombre as curso_nombre, r.puntuacion_total, r.puntuacion_maxima, r.porcentaje, " +
            "r.estado_aprobacion, r.fecha_resultado " +
            "FROM resultados r " +
            "JOIN submissions s ON r.submission_id = s.id " +
            "JOIN users u ON s.estudiante_id = u.id " +
            "JOIN evaluaciones e ON s.evaluacion_id = e.id " +
            "JOIN cursos c ON e.curso_id = c.id " +
            "WHERE e.curso_id = ? " +
            "ORDER BY e.nombre, u.name";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ResultadoDetalleDTO dto = new ResultadoDetalleDTO();
            dto.setId(rs.getInt("id"));
            dto.setSubmissionId(rs.getInt("submission_id"));
            dto.setEstudianteId(rs.getInt("estudiante_id"));
            dto.setEstudianteNombre(rs.getString("estudiante_nombre"));
            dto.setEstudianteEmail(rs.getString("estudiante_email"));
            dto.setEvaluacionId(rs.getInt("evaluacion_id"));
            dto.setEvaluacionNombre(rs.getString("evaluacion_nombre"));
            dto.setCursoNombre(rs.getString("curso_nombre"));
            dto.setPuntuacionTotal(rs.getBigDecimal("puntuacion_total"));
            dto.setPuntuacionMaxima(rs.getBigDecimal("puntuacion_maxima"));
            dto.setPorcentaje(rs.getBigDecimal("porcentaje"));
            dto.setEstadoAprobacion(rs.getString("estado_aprobacion"));
            dto.setFechaResultado(rs.getTimestamp("fecha_resultado") != null ?
                rs.getTimestamp("fecha_resultado").toLocalDateTime() : null);
            return dto;
        }, cursoId);
    }

    /**
     * Retorna resumen estadístico por evaluación dentro de un curso:
     * promedio del grupo, total estudiantes, aprobados y reprobados.
     * Usado por coordinadores.
     */
    public List<ResumenCursoDTO> findResumenByCurso(Integer cursoId) {
        String sql =
            "SELECT e.id as evaluacion_id, e.nombre as evaluacion_nombre, " +
            "ROUND(AVG(r.porcentaje), 2) as promedio_grupo, " +
            "COUNT(r.id) as total_estudiantes, " +
            "SUM(CASE WHEN r.estado_aprobacion = 'Aprobado' THEN 1 ELSE 0 END) as aprobados, " +
            "SUM(CASE WHEN r.estado_aprobacion != 'Aprobado' OR r.estado_aprobacion IS NULL THEN 1 ELSE 0 END) as reprobados " +
            "FROM evaluaciones e " +
            "JOIN submissions s ON s.evaluacion_id = e.id " +
            "JOIN resultados r ON r.submission_id = s.id " +
            "WHERE e.curso_id = ? " +
            "GROUP BY e.id, e.nombre " +
            "ORDER BY e.nombre";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ResumenCursoDTO dto = new ResumenCursoDTO();
            dto.setEvaluacionId(rs.getInt("evaluacion_id"));
            dto.setEvaluacionNombre(rs.getString("evaluacion_nombre"));
            dto.setPromedioGrupo(rs.getBigDecimal("promedio_grupo"));
            dto.setTotalEstudiantes(rs.getInt("total_estudiantes"));
            dto.setAprobados(rs.getInt("aprobados"));
            dto.setReprobados(rs.getInt("reprobados"));
            return dto;
        }, cursoId);
    }
}
