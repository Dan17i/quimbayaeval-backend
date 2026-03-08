package com.quimbayaeval.dao;

import com.quimbayaeval.model.Evaluacion;
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
 * DAO para entidad Evaluacion
 */
@Repository
public class EvaluacionDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT =
        "INSERT INTO evaluaciones (nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at, updated_at " +
        "FROM evaluaciones WHERE id = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT id, nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at, updated_at " +
        "FROM evaluaciones";
    private static final String SQL_SELECT_BY_CURSO =
        "SELECT id, nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at, updated_at " +
        "FROM evaluaciones WHERE curso_id = ?";
    private static final String SQL_SELECT_BY_PROFESOR =
        "SELECT id, nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at, updated_at " +
        "FROM evaluaciones WHERE profesor_id = ?";
    private static final String SQL_SELECT_BY_ESTADO =
        "SELECT id, nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada, created_at, updated_at " +
        "FROM evaluaciones WHERE estado = ?";
    private static final String SQL_UPDATE =
        "UPDATE evaluaciones SET nombre = ?, descripcion = ?, curso_id = ?, profesor_id = ?, tipo = ?, estado = ?, deadline = ?, duracion_minutos = ?, intentos_permitidos = ?, publicada = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE =
        "DELETE FROM evaluaciones WHERE id = ?";

    private final RowMapper<Evaluacion> rowMapper = new RowMapper<Evaluacion>() {
        @Override
        public Evaluacion mapRow(ResultSet rs, int rowNum) throws SQLException {
            Evaluacion eval = new Evaluacion();
            eval.setId(rs.getInt("id"));
            eval.setNombre(rs.getString("nombre"));
            eval.setDescripcion(rs.getString("descripcion"));
            eval.setCursoId(rs.getInt("curso_id"));
            eval.setProfesorId(rs.getInt("profesor_id"));
            eval.setTipo(rs.getString("tipo"));
            eval.setEstado(rs.getString("estado"));
            eval.setDeadline(rs.getTimestamp("deadline") != null ?
                    rs.getTimestamp("deadline").toLocalDateTime() : null);
            eval.setDuracionMinutos(rs.getInt("duracion_minutos"));
            eval.setIntentosPermitidos(rs.getInt("intentos_permitidos"));
            eval.setPublicada(rs.getBoolean("publicada"));
            eval.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
            eval.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return eval;
        }
    };

    /**
     * Inserta una nueva evaluación
     */
    public Evaluacion save(Evaluacion evaluacion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, evaluacion.getNombre());
            ps.setString(2, evaluacion.getDescripcion());
            ps.setInt(3, evaluacion.getCursoId());
            ps.setInt(4, evaluacion.getProfesorId());
            ps.setString(5, evaluacion.getTipo());
            ps.setString(6, evaluacion.getEstado());
            ps.setTimestamp(7, evaluacion.getDeadline() != null ? java.sql.Timestamp.valueOf(evaluacion.getDeadline()) : null);
            ps.setInt(8, evaluacion.getDuracionMinutos() != null ? evaluacion.getDuracionMinutos() : 60);
            ps.setInt(9, evaluacion.getIntentosPermitidos() != null ? evaluacion.getIntentosPermitidos() : 1);
            ps.setBoolean(10, evaluacion.getPublicada() != null ? evaluacion.getPublicada() : false);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            evaluacion.setId(keyHolder.getKey().intValue());
        }
        return evaluacion;
    }

    /**
     * Encuentra evaluación por ID
     */
    public Optional<Evaluacion> findById(Integer id) {
        List<Evaluacion> evals = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return evals.isEmpty() ? Optional.empty() : Optional.of(evals.get(0));
    }

    /**
     * Obtiene todas las evaluaciones
     */
    public List<Evaluacion> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }

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
     * Obtiene evaluaciones aplicando filtros, ordenación y paginación.
     *
     * <p>Este método convierte el {@code Map<String,Object>} recibido en una lista de
     * {@link JdbcQueryBuilder.FilterCriteria}, usando por defecto el operador de igualdad (=).
     * De esta forma se evita el uso del método deprecado de {@link JdbcQueryBuilder} y se
     * habilita la posibilidad de usar operadores más avanzados en el futuro.</p>
     *
     * @param filters   Mapa de filtros simples (campo = valor). Se convierte internamente en criterios.
     * @param page      Número de página para la paginación (puede ser null).
     * @param size      Tamaño de página para la paginación (puede ser null).
     * @param sortBy    Campo por el cual ordenar los resultados.
     * @param direction Dirección de la ordenación ("ASC" o "DESC").
     * @return Lista de evaluaciones que cumplen con los filtros y la paginación indicada.
     */
    public List<Evaluacion> findAll(Map<String, Object> filters,
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
     * Obtiene evaluaciones por curso
     */
    public List<Evaluacion> findByCurso(Integer cursoId) {
        return jdbcTemplate.query(SQL_SELECT_BY_CURSO, rowMapper, cursoId);
    }

    /**
     * Obtiene evaluaciones por profesor
     */
    public List<Evaluacion> findByProfesor(Integer profesorId) {
        return jdbcTemplate.query(SQL_SELECT_BY_PROFESOR, rowMapper, profesorId);
    }

    /**
     * Obtiene evaluaciones por estado
     */
    public List<Evaluacion> findByEstado(String estado) {
        return jdbcTemplate.query(SQL_SELECT_BY_ESTADO, rowMapper, estado);
    }

    /**
     * Obtiene evaluaciones con filtros avanzados (LIKE, ENTRE, etc.)
     */
    public List<Evaluacion> findAllAdvanced(List<JdbcQueryBuilder.FilterCriteria> filters,
                                           Integer page,
                                           Integer size,
                                           String sortBy,
                                           String direction) {
        JdbcQueryBuilder.QueryData q = JdbcQueryBuilder.build(
                SQL_SELECT_ALL, filters, sortBy, direction, page, size);
        return jdbcTemplate.query(q.sql, rowMapper, q.args);
    }

    /**
     * Actualiza una evaluación
     */
    public void update(Evaluacion evaluacion) {
        jdbcTemplate.update(SQL_UPDATE,
            evaluacion.getNombre(),
            evaluacion.getDescripcion(),
            evaluacion.getCursoId(),
            evaluacion.getProfesorId(),
            evaluacion.getTipo(),
            evaluacion.getEstado(),
            evaluacion.getDeadline() != null ? java.sql.Timestamp.valueOf(evaluacion.getDeadline()) : null,
            evaluacion.getDuracionMinutos(),
            evaluacion.getIntentosPermitidos(),
            evaluacion.getPublicada(),
            evaluacion.getId()
        );
    }

    /**
     * Elimina una evaluación
     */
    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }
}
