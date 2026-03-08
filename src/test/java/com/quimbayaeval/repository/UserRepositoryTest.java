package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para UserRepository
 */
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole("estudiante");
        testUser.setActive(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void findByEmail_existingEmail_returnsUser() {
        // Act
        Optional<UserEntity> result = userRepository.findByEmail("test@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        assertEquals("Test User", result.get().getName());
    }

    @Test
    void findByEmail_nonExistingEmail_returnsEmpty() {
        // Act
        Optional<UserEntity> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmailAndRole_correctEmailAndRole_returnsUser() {
        // Act
        Optional<UserEntity> result = userRepository.findByEmailAndRole("test@example.com", "estudiante");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("estudiante", result.get().getRole());
    }

    @Test
    void findByEmailAndRole_correctEmailWrongRole_returnsEmpty() {
        // Act
        Optional<UserEntity> result = userRepository.findByEmailAndRole("test@example.com", "maestro");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void existsByEmail_existingEmail_returnsTrue() {
        // Act
        boolean exists = userRepository.existsByEmail("test@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_nonExistingEmail_returnsFalse() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_newUser_persistsCorrectly() {
        // Arrange
        UserEntity newUser = new UserEntity();
        newUser.setName("New User");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        newUser.setRole("maestro");
        newUser.setActive(true);

        // Act
        UserEntity saved = userRepository.save(newUser);

        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("new@example.com", saved.getEmail());
    }

    @Test
    void save_updateUser_updatesCorrectly() {
        // Arrange
        testUser.setName("Updated Name");

        // Act
        UserEntity updated = userRepository.save(testUser);

        // Assert
        assertEquals("Updated Name", updated.getName());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void deleteById_existingUser_removesUser() {
        // Act
        userRepository.deleteById(testUser.getId());

        // Assert
        Optional<UserEntity> result = userRepository.findById(testUser.getId());
        assertFalse(result.isPresent());
    }
}
