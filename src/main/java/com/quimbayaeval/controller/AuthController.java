package com.quimbayaeval.controller;

import com.quimbayaeval.model.User;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.model.dto.LoginRequest;
import com.quimbayaeval.model.dto.LoginResponse;
import com.quimbayaeval.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador para autenticación
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Login de usuario
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar usuario
            User user = authService.authenticate(loginRequest);

            // Generar JWT
            String token = authService.generateToken(user);

            // Crear response
            LoginResponse response = new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());

            return ResponseEntity.ok(
                ApiResponse.success("Login exitoso", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Error en login: " + e.getMessage())
            );
        }
    }

    /**
     * Registro de nuevo usuario
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> register(@RequestBody LoginRequest registerRequest) {
        try {
            // Se asume que el password viene en texto plano en el request
            User newUser = new User("", registerRequest.getEmail(), registerRequest.getPassword(), registerRequest.getRole());
            newUser = authService.register(newUser, registerRequest.getPassword());

            // Generar JWT
            String token = authService.generateToken(newUser);

            LoginResponse response = new LoginResponse(token, newUser.getId(), newUser.getName(), newUser.getEmail(), newUser.getRole());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Usuario registrado exitosamente", response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error en registro: " + e.getMessage())
            );
        }
    }

    /**
     * Valida token JWT
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken(@RequestHeader("Authorization") String bearerToken) {
        try {
            if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
                String token = bearerToken.substring(7);
                if (authService.validateToken(token)) {
                    return ResponseEntity.ok(ApiResponse.success("Token válido", "valid"));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Token inválido o no proporcionado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error("Error validando token: " + e.getMessage())
            );
        }
    }
}
