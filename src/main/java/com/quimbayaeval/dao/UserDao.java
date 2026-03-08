package com.quimbayaeval.dao;

import com.quimbayaeval.model.User;
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
import java.util.List;
import java.util.Optional;

/**
 * DAO para entidad User - Acceso a datos con JDBC
 */
@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT = 
        "INSERT INTO users (name, email, password, role, active, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
    private static final String SQL_SELECT_BY_ID = 
        "SELECT id, name, email, password, role, active, created_at, updated_at FROM users WHERE id = ?";
    private static final String SQL_SELECT_BY_EMAIL = 
        "SELECT id, name, email, password, role, active, created_at, updated_at FROM users WHERE email = ?";
    private static final String SQL_SELECT_ALL = 
        "SELECT id, name, email, password, role, active, created_at, updated_at FROM users WHERE active = true";
    private static final String SQL_SELECT_BY_ROLE = 
        "SELECT id, name, email, password, role, active, created_at, updated_at FROM users WHERE role = ? AND active = true";
    private static final String SQL_UPDATE = 
        "UPDATE users SET name = ?, email = ?, role = ?, active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
    private static final String SQL_DELETE = 
        "UPDATE users SET active = false, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private final RowMapper<User> rowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            user.setRole(rs.getString("role"));
            user.setActive(rs.getBoolean("active"));
            user.setCreatedAt(rs.getTimestamp("created_at") != null ?
                    rs.getTimestamp("created_at").toLocalDateTime() : null);
            user.setUpdatedAt(rs.getTimestamp("updated_at") != null ?
                    rs.getTimestamp("updated_at").toLocalDateTime() : null);
            return user;
        }
    };

    /**
     * Inserta un nuevo usuario
     */
    public User save(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole());
            ps.setBoolean(5, user.getActive() != null ? user.getActive() : true);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().intValue());
        }
        return user;
    }

    /**
     * Encuentra usuario por ID
     */
    public Optional<User> findById(Integer id) {
        List<User> users = jdbcTemplate.query(SQL_SELECT_BY_ID, rowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Encuentra usuario por email
     */
    public Optional<User> findByEmail(String email) {
        List<User> users = jdbcTemplate.query(SQL_SELECT_BY_EMAIL, rowMapper, email);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Obtiene todos los usuarios activos
     */
    public List<User> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL, rowMapper);
    }

    /**
     * Obtiene usuarios por rol
     */
    public List<User> findByRole(String role) {
        return jdbcTemplate.query(SQL_SELECT_BY_ROLE, rowMapper, role);
    }

    /**
     * Actualiza un usuario
     */
    public void update(User user) {
        jdbcTemplate.update(SQL_UPDATE,
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getActive() != null ? user.getActive() : true,
            user.getId()
        );
    }

    /**
     * Elimina lógicamente un usuario (soft delete)
     */
    public void deleteById(Integer id) {
        jdbcTemplate.update(SQL_DELETE, id);
    }

    /**
     * Verifica si existe un usuario con email
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND active = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}
