package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.CursoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para CursoRepository
 */
@DataJpaTest
class CursoRepositoryTest {

    @Autowired
    private CursoRepository cursoRepository;

    private CursoEntity testCurso;

    @BeforeEach
    void setUp() {
        cursoRepository.deleteAll();

        testCurso = new CursoEntity();
        testCurso.setCodigo("MAT-101");
        testCurso.setNombre("Matemáticas Básicas");
        testCurso.setDescripcion("Curso introductorio de matemáticas");
        testCurso.setProfesorId(1);
        testCurso = cursoRepository.save(testCurso);
    }

    @Test
    void findById_existingId_returnsCurso() {
        // Act
        Optional<CursoEntity> result = cursoRepository.findById(testCurso.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("MAT-101", result.get().getCodigo());
        assertEquals("Matemáticas Básicas", result.get().getNombre());
    }

    @Test
    void findByCodigo_existingCodigo_returnsCurso() {
        // Act
        Optional<CursoEntity> result = cursoRepository.findByCodigo("MAT-101");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Matemáticas Básicas", result.get().getNombre());
    }

    @Test
    void findByCodigo_nonExistingCodigo_returnsEmpty() {
        // Act
        Optional<CursoEntity> result = cursoRepository.findByCodigo("XXX-999");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByProfesorId_existingProfesor_returnsCursos() {
        // Arrange
        CursoEntity otro = new CursoEntity();
        otro.setCodigo("FIS-101");
        otro.setNombre("Física Básica");
        otro.setProfesorId(1);
        cursoRepository.save(otro);

        // Act
        List<CursoEntity> result = cursoRepository.findByProfesorId(1);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findByProfesorId_nonExistingProfesor_returnsEmpty() {
        // Act
        List<CursoEntity> result = cursoRepository.findByProfesorId(999);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void existsByCodigo_existingCodigo_returnsTrue() {
        // Act
        boolean exists = cursoRepository.existsByCodigo("MAT-101");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByCodigo_nonExistingCodigo_returnsFalse() {
        // Act
        boolean exists = cursoRepository.existsByCodigo("XXX-999");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_newCurso_persistsCorrectly() {
        // Arrange
        CursoEntity nuevo = new CursoEntity();
        nuevo.setCodigo("QUI-101");
        nuevo.setNombre("Química Básica");
        nuevo.setDescripcion("Introducción a la química");
        nuevo.setProfesorId(2);

        // Act
        CursoEntity saved = cursoRepository.save(nuevo);

        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("QUI-101", saved.getCodigo());
    }

    @Test
    void save_updateCurso_updatesCorrectly() {
        // Arrange
        testCurso.setNombre("Matemáticas Avanzadas");
        testCurso.setDescripcion("Curso avanzado");

        // Act
        CursoEntity updated = cursoRepository.save(testCurso);

        // Assert
        assertEquals("Matemáticas Avanzadas", updated.getNombre());
        assertEquals("Curso avanzado", updated.getDescripcion());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void deleteById_existingCurso_removesCurso() {
        // Act
        cursoRepository.deleteById(testCurso.getId());

        // Assert
        Optional<CursoEntity> result = cursoRepository.findById(testCurso.getId());
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_multipleCursos_returnsAll() {
        // Arrange
        CursoEntity curso2 = new CursoEntity();
        curso2.setCodigo("FIS-101");
        curso2.setNombre("Física");
        curso2.setProfesorId(1);
        cursoRepository.save(curso2);

        CursoEntity curso3 = new CursoEntity();
        curso3.setCodigo("QUI-101");
        curso3.setNombre("Química");
        curso3.setProfesorId(2);
        cursoRepository.save(curso3);

        // Act
        List<CursoEntity> result = cursoRepository.findAll();

        // Assert
        assertEquals(3, result.size());
    }
}
