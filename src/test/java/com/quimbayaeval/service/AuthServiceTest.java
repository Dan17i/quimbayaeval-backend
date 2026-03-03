package com.quimbayaeval.service;

import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.User;
import com.quimbayaeval.model.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticate_validCredentials_returnsUser() {
        User user = new User(1, "Jane", "jane@example.com", "estudiante");
        when(userDao.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", "hash")).thenReturn(true);

        LoginRequest req = new LoginRequest();
        req.setEmail("jane@example.com");
        req.setPassword("plain");

        User result = authService.authenticate(req);
        assertEquals(user, result);
    }

    @Test
    void authenticate_wrongPassword_throws() {
        User user = new User(1, "Jane", "jane@example.com", "estudiante");
        when(userDao.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        LoginRequest req = new LoginRequest();
        req.setEmail("jane@example.com");
        req.setPassword("bad");

        assertThrows(RuntimeException.class, () -> authService.authenticate(req));
    }

    @Test
    void register_existingEmail_throws() {
        when(userDao.existsByEmail("a@b.c")).thenReturn(true);
        User u = new User("Jane", "a@b.c", "pwd", "estudiante");
        assertThrows(RuntimeException.class, () -> authService.register(u, "pwd"));
    }

    @Test
    void register_success() {
        when(userDao.existsByEmail("a@b.c")).thenReturn(false);
        when(passwordEncoder.encode("pwd")).thenReturn("hash");
        User saved = new User("Jane", "a@b.c", "hash", "estudiante");
        saved.setId(1);
        when(userDao.save(any())).thenReturn(saved);

        User u = new User("Jane", "a@b.c", "pwd", "estudiante");
        User result = authService.register(u, "pwd");
        assertEquals(1, result.getId());
        verify(userDao).save(any(User.class));
    }
}
