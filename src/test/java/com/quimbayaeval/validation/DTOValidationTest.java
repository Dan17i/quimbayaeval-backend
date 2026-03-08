package com.quimbayaeval.validation;

import com.quimbayaeval.model.dto.request.CrearEvaluacionRequestDTO;
import com.quimbayaeval.model.dto.request.CrearPQRSRequestDTO;
import com.quimbayaeval.model.dto.request.LoginRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DTOValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== LoginRequestDTO Tests ==========

    @Test
    void loginRequestDTO_validData_noViolations() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        dto.setRole("estudiante");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void loginRequestDTO_blankEmail_hasViolation() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("");
        dto.setPassword("password123");
        dto.setRole("estudiante");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void loginRequestDTO_invalidEmail_hasViolation() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("invalid-email");
        dto.setPassword("password123");
        dto.setRole("estudiante");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void loginRequestDTO_blankPassword_hasViolation() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("");
        dto.setRole("estudiante");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    void loginRequestDTO_invalidRole_hasViolation() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setEmail("test@test.com");
        dto.setPassword("password123");
        dto.setRole("invalid-role");

        // Act
        Set<ConstraintViolation<LoginRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    // ========== CrearEvaluacionRequestDTO Tests ==========

    @Test
    void crearEvaluacionRequestDTO_validData_noViolations() {
        // Arrange
        CrearEvaluacionRequestDTO dto = new CrearEvaluacionRequestDTO();
        dto.setNombre("Test Evaluacion");
        dto.setDescripcion("Test description");
        dto.setTipo("Examen");
        dto.setDeadline(LocalDateTime.now().plusDays(7));
        dto.setDuracionMinutos(60);
        dto.setCursoId(1);
        dto.setProfesorId(1);
        dto.setIntentosPermitidos(3);

        // Act
        Set<ConstraintViolation<CrearEvaluacionRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void crearEvaluacionRequestDTO_blankNombre_hasViolation() {
        // Arrange
        CrearEvaluacionRequestDTO dto = new CrearEvaluacionRequestDTO();
        dto.setNombre("");
        dto.setDescripcion("Test description");
        dto.setTipo("Examen");
        dto.setDeadline(LocalDateTime.now().plusDays(7));
        dto.setDuracionMinutos(60);
        dto.setCursoId(1);
        dto.setProfesorId(1);

        // Act
        Set<ConstraintViolation<CrearEvaluacionRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nombre"));
    }

    @Test
    void crearEvaluacionRequestDTO_negativeDuracion_hasViolation() {
        // Arrange
        CrearEvaluacionRequestDTO dto = new CrearEvaluacionRequestDTO();
        dto.setNombre("Test Evaluacion");
        dto.setDescripcion("Test description");
        dto.setTipo("Examen");
        dto.setDeadline(LocalDateTime.now().plusDays(7));
        dto.setDuracionMinutos(-10);
        dto.setCursoId(1);
        dto.setProfesorId(1);

        // Act
        Set<ConstraintViolation<CrearEvaluacionRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("duracionMinutos"));
    }

    @Test
    void crearEvaluacionRequestDTO_excessiveDuracion_hasViolation() {
        // Arrange
        CrearEvaluacionRequestDTO dto = new CrearEvaluacionRequestDTO();
        dto.setNombre("Test Evaluacion");
        dto.setDescripcion("Test description");
        dto.setTipo("Examen");
        dto.setDeadline(LocalDateTime.now().plusDays(7));
        dto.setDuracionMinutos(500); // Max is 480
        dto.setCursoId(1);
        dto.setProfesorId(1);

        // Act
        Set<ConstraintViolation<CrearEvaluacionRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("duracionMinutos"));
    }

    // ========== CrearPQRSRequestDTO Tests ==========

    @Test
    void crearPQRSRequestDTO_validData_noViolations() {
        // Arrange
        CrearPQRSRequestDTO dto = new CrearPQRSRequestDTO();
        dto.setTipo("Pregunta");
        dto.setAsunto("Test asunto con suficiente longitud");
        dto.setDescripcion("Test description with enough length for validation");
        dto.setCursoId(1);

        // Act
        Set<ConstraintViolation<CrearPQRSRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    void crearPQRSRequestDTO_blankTipo_hasViolation() {
        // Arrange
        CrearPQRSRequestDTO dto = new CrearPQRSRequestDTO();
        dto.setTipo("");
        dto.setAsunto("Test asunto con suficiente longitud");
        dto.setDescripcion("Test description with enough length for validation");
        dto.setCursoId(1);

        // Act
        Set<ConstraintViolation<CrearPQRSRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tipo"));
    }

    @Test
    void crearPQRSRequestDTO_invalidTipo_hasViolation() {
        // Arrange
        CrearPQRSRequestDTO dto = new CrearPQRSRequestDTO();
        dto.setTipo("InvalidType");
        dto.setAsunto("Test asunto con suficiente longitud");
        dto.setDescripcion("Test description with enough length for validation");
        dto.setCursoId(1);

        // Act
        Set<ConstraintViolation<CrearPQRSRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("tipo"));
    }

    @Test
    void crearPQRSRequestDTO_shortDescripcion_hasViolation() {
        // Arrange
        CrearPQRSRequestDTO dto = new CrearPQRSRequestDTO();
        dto.setTipo("Pregunta");
        dto.setAsunto("Test asunto");
        dto.setDescripcion("Short");
        dto.setCursoId(1);

        // Act
        Set<ConstraintViolation<CrearPQRSRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("descripcion"));
    }

    @Test
    void crearPQRSRequestDTO_blankAsunto_hasViolation() {
        // Arrange
        CrearPQRSRequestDTO dto = new CrearPQRSRequestDTO();
        dto.setTipo("Pregunta");
        dto.setAsunto("");
        dto.setDescripcion("Test description with enough length for validation");
        dto.setCursoId(1);

        // Act
        Set<ConstraintViolation<CrearPQRSRequestDTO>> violations = validator.validate(dto);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("asunto"));
    }
}
