package com.quimbayaeval.service;

import com.quimbayaeval.exception.ResourceNotFoundException;
import com.quimbayaeval.exception.UnauthorizedException;
import com.quimbayaeval.model.dto.request.LoginRequestDTO;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.repository.UserRepository;
import com.quimbayaeval.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para AuthService con JPA
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceJpaTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private UserEntity testUser;
    private LoginRequestDTO loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId(1);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole("estudiante");
        testUser.setActive(true);

        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");
        loginRequest.setRole("estudiante");
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$10$encodedPassword")).thenReturn(true);

        // Act
        UserEntity result = authService.authenticate(loginRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("estudiante", result.getRole());
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).matches("password", "$2a$10$encodedPassword");
    }

    @Test
    void authenticate_userNotFound_throwsResourceNotFoundException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            authService.authenticate(loginRequest);
        });
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authenticate_wrongPassword_throwsUnauthorizedException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$10$encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticate(loginRequest);
        });
        verify(passwordEncoder).matches("password", "$2a$10$encodedPassword");
    }

    @Test
    void authenticate_inactiveUser_throwsUnauthorizedException() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticate(loginRequest);
        });
    }

    @Test
    void authenticate_wrongRole_throwsUnauthorizedException() {
        // Arrange
        testUser.setRole("maestro");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "$2a$10$encodedPassword")).thenReturn(true);

        // Act & Assert
        assertThrows(UnauthorizedException.class, () -> {
            authService.authenticate(loginRequest);
        });
    }

    @Test
    void register_newUser_success() {
        // Arrange
        UserEntity newUser = new UserEntity();
        newUser.setEmail("new@example.com");
        newUser.setPassword("plainPassword");
        newUser.setRole("estudiante");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(newUser);

        // Act
        UserEntity result = authService.register(newUser, "plainPassword");

        // Assert
        assertNotNull(result);
        assertTrue(result.getActive());
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_existingEmail_throwsIllegalArgumentException() {
        // Arrange
        UserEntity newUser = new UserEntity();
        newUser.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authService.register(newUser, "password");
        });
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void generateToken_validUser_returnsToken() {
        // Arrange
        when(tokenProvider.generateToken(1, "test@example.com", "estudiante"))
            .thenReturn("jwt.token.here");

        // Act
        String token = authService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertEquals("jwt.token.here", token);
        verify(tokenProvider).generateToken(1, "test@example.com", "estudiante");
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        // Arrange
        when(tokenProvider.validateToken("valid.token")).thenReturn(true);

        // Act
        boolean result = authService.validateToken("valid.token");

        // Assert
        assertTrue(result);
        verify(tokenProvider).validateToken("valid.token");
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        // Arrange
        when(tokenProvider.validateToken("invalid.token")).thenReturn(false);

        // Act
        boolean result = authService.validateToken("invalid.token");

        // Assert
        assertFalse(result);
        verify(tokenProvider).validateToken("invalid.token");
    }
}
