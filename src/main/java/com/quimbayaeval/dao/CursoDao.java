package com.quimbayaeval.dao;

import com.quimbayaeval.model.Curso;
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
 * DAO para entidad Curso
 */
@Repository
public class CursoDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO cursos (codigo, nombre, descripcion, profesor_id, created_at) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, codigo, nombre, descripcion, profesor_id, created_at, updated_at FROM cursos WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, codigo, nombre, descripcion, profesor_id, created_at, updated_at FROM cursos";
    private static final String SQL_SELECT_BY_PROFESOR =
        "SELECT id, codigo, nombre, descripcion, profesor_id, created_at, updated_at FROM cursos WHERE profesor_id = ?";
    private static final String SQL_SELECT_BY_CODIGO =
        "SELECT id, codigo, nombre, descripcion, profesor_id, created_at, updated_at FROM cursos WHERE codigo = ?";
    private static final String SQL_UPDATE =
        "UPDATE cursos SET codigo = ?, nombre = ?, descripcion = ?, profesor_id = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM cursos WHERE id = ?";

    private final RowMapper<Curso> rowMapper = new RowMapper<Curso>() {
        @Override
        public Curso mapRow(ResultSet rs, int rowNum) throws SQLException {
            Curso curso = new Curso();
            curso.setId(rs.getInt("id"));
            curso.setCodigo(rs.getString("codigo"));
            curso.setNombre(rs.getString("nombre"));
            curso.setDescripcion(rs.getString("descripcion"));
            curso.setProfesorId(rs.getInt("profesor_id"));
            curso.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
            curso.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return curso;
        }
    };

    /**
     * Inserta un nuevo curso
     */
    public Curso save(Curso curso) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, curso.getCodigo());
            ps.setString(2, curso.getNombre());
            ps.setString(3, curso.getDescripcion());
            ps.setInt(4, curso.getProfesorId() != null ? curso.getProfesorId() : 0);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            curso.setId(keyHolder.getKey().intValue());
        }
        return curso;
    }

    /**
     * Encuentra curso por ID
     */
    public Optional<Curso> findById(Integer id) {
        List<Curso> cursos = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return cursos.isEmpty() ? Optional.empty() : Optional.of(cursos.get(0));
    }

    /**
     * Encuentra curso por código
     */
    public Optional<Curso> findByCodigo(String codigo) {
        List<Curso> cursos = jdbcTemplate.query(SQL_SELECT_BY_CODIGO, rowMapper, codigo);
        return cursos.isEmpty() ? Optional.empty() : Optional.of(cursos.get(0));
    }

    /**
     * Obtiene todos los cursos sin paginar/filtrar.
     */
    public List<Curso> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }

    /**
     * Convierte Map en lista de criterios
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
     * Obtiene cursos aplicando filtros opcionales, ordenación y paginación
     */
    public List<Curso> findAll(Map<String, Object> filters,
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
     * Obtiene cursos por profesor
     */
    public List<Curso> findByProfesor(Integer profesorId) {
        return jdbcTemplate.query(SQL_SELECT_BY_PROFESOR, rowMapper, profesorId);
    }

    /**
     * Actualiza un curso
     */
    public void update(Curso curso) {
        jdbcTemplate.update(SQL_UPDATE,
            curso.getCodigo(),
            curso.getNombre(),
            curso.getDescripcion(),
            curso.getProfesorId(),
            curso.getId()
        );
    }

    /**
     * Elimina un curso
     */
    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
