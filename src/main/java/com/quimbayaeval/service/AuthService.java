package com.quimbayaeval.service;

import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.User;
import com.quimbayaeval.model.dto.LoginRequest;
import com.quimbayaeval.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de autenticación
 */
@Service
public class AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Realiza login de usuario
     */
    public User authenticate(LoginRequest loginRequest) {
        Optional<User> userOpt = userDao.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        User user = userOpt.get();

        // Validar contraseña
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Validar role si se proporciona
        if (loginRequest.getRole() != null && !loginRequest.getRole().equals(user.getRole())) {
            throw new RuntimeException("El rol no coincide");
        }

        return user;
    }

    /**
     * Registra un nuevo usuario
     */
    public User register(User user, String passwordPlain) {
        // Validar email único
        if (userDao.existsByEmail(user.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(passwordPlain));
        user.setActive(true);

        return userDao.save(user);
    }

    /**
     * Obtiene usuario por ID
     */
    public Optional<User> getUserById(Integer id) {
        return userDao.findById(id);
    }

    /**
     * Obtiene usuario por email
     */
    public Optional<User> getUserByEmail(String email) {
        return userDao.findByEmail(email);
    }

    /**
     * Genera JWT para usuario autenticado
     */
    public String generateToken(User user) {
        return tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    /**
     * Valida un JWT
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}
