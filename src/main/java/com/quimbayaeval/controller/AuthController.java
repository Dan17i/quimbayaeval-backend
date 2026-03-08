package com.quimbayaeval.controller;

import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.model.dto.LoginResponse;
import com.quimbayaeval.model.dto.request.LoginRequestDTO;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para autenticación - Compatible con frontend React
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login de usuario
     * POST /api/auth/login
     * 
     * Request body esperado por el frontend:
     * {
     *   "email": "estudiante@test.com",
     *   "password": "password",
     *   "role": "estudiante"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        log.info("Request de login recibido para: {}", loginRequest.getEmail());
        
        // Autenticar usuario
        UserEntity user = authService.authenticate(loginRequest);

        // Generar JWT
        String token = authService.generateToken(user);

        // Crear response compatible con frontend
        LoginResponse response = new LoginResponse(
            token, 
            user.getId(), 
            user.getName(), 
            user.getEmail(), 
            user.getRole()
        );

        log.info("Login exitoso para usuario: {}", user.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    }

    /**
     * Registro de nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@Valid @RequestBody LoginRequestDTO registerRequest) {
        log.info("Request de registro recibido para: {}", registerRequest.getEmail());
        
        // Crear nueva entidad de usuario
        UserEntity newUser = new UserEntity();
        newUser.setName(registerRequest.getEmail().split("@")[0]); // Nombre temporal del email
        newUser.setEmail(registerRequest.getEmail());
        newUser.setRole(registerRequest.getRole());
        
        // Registrar usuario
        newUser = authService.register(newUser, registerRequest.getPassword());

        // Generar JWT
        String token = authService.generateToken(newUser);

        LoginResponse response = new LoginResponse(
            token, 
            newUser.getId(), 
            newUser.getName(), 
            newUser.getEmail(), 
            newUser.getRole()
        );

        log.info("Usuario registrado exitosamente: {}", newUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Usuario registrado exitosamente", response));
    }

    /**
     * Valida token JWT
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            if (authService.validateToken(token)) {
                log.debug("Token validado exitosamente");
                return ResponseEntity.ok(ApiResponse.success("Token válido", "valid"));
            }
        }
        log.warn("Token inválido o no proporcionado");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Token inválido o no proporcionado"));
    }
}
