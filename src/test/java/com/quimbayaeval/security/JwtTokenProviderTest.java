package com.quimbayaeval.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para JwtTokenProvider
 */
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String testSecret = "test-secret-key-that-is-long-enough-for-hmac-sha-256-algorithm-minimum-256-bits";
    private final long testExpiration = 3600000; // 1 hora

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void generateToken_validData_returnsToken() {
        // Act
        String token = tokenProvider.generateToken(1, "test@example.com", "estudiante");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT tiene 3 partes
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        // Arrange
        String token = tokenProvider.generateToken(1, "test@example.com", "estudiante");

        // Act
        boolean isValid = tokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_invalidToken_returnsFalse() {
        // Act
        boolean isValid = tokenProvider.validateToken("invalid.token.here");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_emptyToken_returnsFalse() {
        // Act
        boolean isValid = tokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUserEmailFromToken_validToken_returnsEmail() {
        // Arrange
        String token = tokenProvider.generateToken(1, "test@example.com", "estudiante");

        // Act
        String email = tokenProvider.getUserEmailFromToken(token);

        // Assert
        assertEquals("test@example.com", email);
    }

    @Test
    void getUserIdFromToken_validToken_returnsId() {
        // Arrange
        String token = tokenProvider.generateToken(123, "test@example.com", "estudiante");

        // Act
        Integer userId = tokenProvider.getUserIdFromToken(token);

        // Assert
        assertEquals(123, userId);
    }

    @Test
    void getRoleFromToken_validToken_returnsRole() {
        // Arrange
        String token = tokenProvider.generateToken(1, "test@example.com", "maestro");

        // Act
        String role = tokenProvider.getRoleFromToken(token);

        // Assert
        assertEquals("maestro", role);
    }

    @Test
    void generateToken_differentUsers_generatesDifferentTokens() {
        // Act
        String token1 = tokenProvider.generateToken(1, "user1@example.com", "estudiante");
        String token2 = tokenProvider.generateToken(2, "user2@example.com", "maestro");

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_sameUserDifferentTimes_generatesDifferentTokens() throws InterruptedException {
        // Act
        String token1 = tokenProvider.generateToken(1, "test@example.com", "estudiante");
        Thread.sleep(1000); // Esperar 1 segundo
        String token2 = tokenProvider.generateToken(1, "test@example.com", "estudiante");

        // Assert
        assertNotEquals(token1, token2); // Diferentes porque tienen diferente timestamp
    }
}
