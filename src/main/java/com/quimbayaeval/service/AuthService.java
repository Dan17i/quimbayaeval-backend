package com.quimbayaeval.service;

import com.quimbayaeval.config.CustomMetrics;
import com.quimbayaeval.exception.ResourceNotFoundException;
import com.quimbayaeval.exception.UnauthorizedException;
import com.quimbayaeval.model.dto.request.LoginRequestDTO;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.repository.UserRepository;
import com.quimbayaeval.security.JwtTokenProvider;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de autenticación con JPA, logging y métricas
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired(required = false)
    private CustomMetrics customMetrics;

    /**
     * Realiza login de usuario
     */
    @Timed(value = "auth.login", description = "Tiempo de autenticación de usuario")
    public UserEntity authenticate(LoginRequestDTO loginRequest) {
        log.info("Intento de login para email: {}", loginRequest.getEmail());
        
        try {
            Optional<UserEntity> userOpt = userRepository.findByEmail(loginRequest.getEmail());

            if (userOpt.isEmpty()) {
                log.warn("Usuario no encontrado: {}", loginRequest.getEmail());
                if (customMetrics != null) {
                    customMetrics.incrementLoginFailure();
                }
                throw new ResourceNotFoundException("Usuario", "email", loginRequest.getEmail());
            }

            UserEntity user = userOpt.get();

            // Validar que el usuario esté activo
            if (!user.getActive()) {
                log.warn("Usuario inactivo intentó login: {}", loginRequest.getEmail());
                if (customMetrics != null) {
                    customMetrics.incrementLoginFailure();
                }
                throw new UnauthorizedException("Usuario inactivo");
            }

            // Validar contraseña
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Contraseña incorrecta para usuario: {}", loginRequest.getEmail());
                if (customMetrics != null) {
                    customMetrics.incrementLoginFailure();
                }
                throw new UnauthorizedException("Credenciales inválidas");
            }

            // Validar role si se proporciona
            if (loginRequest.getRole() != null && !loginRequest.getRole().equals(user.getRole())) {
                log.warn("Rol no coincide para usuario: {}. Esperado: {}, Recibido: {}", 
                         loginRequest.getEmail(), user.getRole(), loginRequest.getRole());
                if (customMetrics != null) {
                    customMetrics.incrementLoginFailure();
                }
                throw new UnauthorizedException("El rol no coincide con el usuario");
            }

            log.info("Login exitoso para usuario: {} con rol: {}", user.getEmail(), user.getRole());
            if (customMetrics != null) {
                customMetrics.incrementLoginSuccess();
            }
            return user;
        } catch (ResourceNotFoundException | UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado durante login: {}", e.getMessage(), e);
            if (customMetrics != null) {
                customMetrics.incrementLoginFailure();
            }
            throw e;
        }
    }

    /**
     * Registra un nuevo usuario
     */
    public UserEntity register(UserEntity user, String passwordPlain) {
        log.info("Registrando nuevo usuario: {}", user.getEmail());
        
        // Validar email único
        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Intento de registro con email duplicado: {}", user.getEmail());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Encriptar contraseña
        user.setPassword(passwordEncoder.encode(passwordPlain));
        user.setActive(true);

        UserEntity saved = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {} con ID: {}", saved.getEmail(), saved.getId());
        return saved;
    }

    /**
     * Obtiene usuario por ID
     */
    public Optional<UserEntity> getUserById(Integer id) {
        return userRepository.findById(id);
    }

    /**
     * Obtiene usuario por email
     */
    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Genera JWT para usuario autenticado
     */
    public String generateToken(UserEntity user) {
        return tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    /**
     * Valida un JWT
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }
}
