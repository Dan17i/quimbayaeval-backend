package com.quimbayaeval.dao;

import com.quimbayaeval.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class InscripcionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> estudianteMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setName(rs.getString("name"));
            u.setEmail(rs.getString("email"));
            u.setRole(rs.getString("role"));
            u.setActive(rs.getBoolean("active"));
            return u;
        }
    };

    /** Estudiantes matriculados en un curso */
    public List<User> findEstudiantesByCurso(Integer cursoId) {
        String sql =
            "SELECT u.id, u.name, u.email, u.role, u.active " +
            "FROM users u " +
            "JOIN inscripciones i ON u.id = i.estudiante_id " +
            "WHERE i.curso_id = ? ORDER BY u.name";
        return jdbcTemplate.query(sql, estudianteMapper, cursoId);
    }

    /** Matricular estudiante en curso — ignora si ya existe */
    public void inscribir(Integer cursoId, Integer estudianteId) {
        jdbcTemplate.update(
            "INSERT INTO inscripciones (estudiante_id, curso_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
            estudianteId, cursoId
        );
    }

    /** Desmatricular estudiante de curso */
    public void desinscribir(Integer cursoId, Integer estudianteId) {
        jdbcTemplate.update(
            "DELETE FROM inscripciones WHERE curso_id = ? AND estudiante_id = ?",
            cursoId, estudianteId
        );
    }

    /** Verifica si un estudiante ya está inscrito */
    public boolean existeInscripcion(Integer cursoId, Integer estudianteId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM inscripciones WHERE curso_id = ? AND estudiante_id = ?",
            Integer.class, cursoId, estudianteId
        );
        return count != null && count > 0;
    }
}
