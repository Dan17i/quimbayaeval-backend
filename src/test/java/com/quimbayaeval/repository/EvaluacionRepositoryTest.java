package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.EvaluacionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para EvaluacionRepository
 */
@DataJpaTest
class EvaluacionRepositoryTest {

    @Autowired
    private EvaluacionRepository evaluacionRepository;

    private EvaluacionEntity testEvaluacion;

    @BeforeEach
    void setUp() {
        evaluacionRepository.deleteAll();

        testEvaluacion = new EvaluacionEntity();
        testEvaluacion.setNombre("Parcial 1");
        testEvaluacion.setDescripcion("Evaluación de prueba");
        testEvaluacion.setCursoId(1);
        testEvaluacion.setProfesorId(2);
        testEvaluacion.setTipo("Examen");
        testEvaluacion.setEstado("Activa");
        testEvaluacion.setDeadline(LocalDateTime.now().plusDays(7));
        testEvaluacion.setDuracionMinutos(120);
        testEvaluacion.setIntentosPermitidos(1);
        testEvaluacion.setPublicada(true);
        testEvaluacion = evaluacionRepository.save(testEvaluacion);
    }

    @Test
    void findById_existingId_returnsEvaluacion() {
        // Act
        Optional<EvaluacionEntity> result = evaluacionRepository.findById(testEvaluacion.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Parcial 1", result.get().getNombre());
        assertEquals("Examen", result.get().getTipo());
    }

    @Test
    void findByCursoId_existingCurso_returnsEvaluaciones() {
        // Arrange
        EvaluacionEntity otra = new EvaluacionEntity();
        otra.setNombre("Quiz 1");
        otra.setCursoId(1);
        otra.setProfesorId(2);
        otra.setTipo("Quiz");
        otra.setEstado("Activa");
        otra.setPublicada(true);
        evaluacionRepository.save(otra);

        // Act
        List<EvaluacionEntity> result = evaluacionRepository.findByCursoId(1);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findByProfesorId_existingProfesor_returnsEvaluaciones() {
        // Act
        List<EvaluacionEntity> result = evaluacionRepository.findByProfesorId(2);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Parcial 1", result.get(0).getNombre());
    }

    @Test
    void findByEstado_activas_returnsEvaluaciones() {
        // Arrange
        EvaluacionEntity cerrada = new EvaluacionEntity();
        cerrada.setNombre("Parcial Cerrado");
        cerrada.setCursoId(1);
        cerrada.setProfesorId(2);
        cerrada.setTipo("Examen");
        cerrada.setEstado("Cerrada");
        cerrada.setPublicada(true);
        evaluacionRepository.save(cerrada);

        // Act
        List<EvaluacionEntity> activas = evaluacionRepository.findByEstado("Activa");
        List<EvaluacionEntity> cerradas = evaluacionRepository.findByEstado("Cerrada");

        // Assert
        assertEquals(1, activas.size());
        assertEquals(1, cerradas.size());
        assertEquals("Activa", activas.get(0).getEstado());
    }

    @Test
    void findByCursoId_withPagination_returnsPage() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            EvaluacionEntity eval = new EvaluacionEntity();
            eval.setNombre("Evaluacion " + i);
            eval.setCursoId(1);
            eval.setProfesorId(2);
            eval.setTipo("Quiz");
            eval.setEstado("Activa");
            eval.setPublicada(true);
            evaluacionRepository.save(eval);
        }

        // Act
        Page<EvaluacionEntity> page = evaluacionRepository.findByCursoId(1, PageRequest.of(0, 3));

        // Assert
        assertEquals(3, page.getContent().size());
        assertEquals(6, page.getTotalElements()); // 1 inicial + 5 nuevas
        assertEquals(2, page.getTotalPages());
    }

    @Test
    void save_newEvaluacion_persistsCorrectly() {
        // Arrange
        EvaluacionEntity nueva = new EvaluacionEntity();
        nueva.setNombre("Nueva Evaluacion");
        nueva.setCursoId(2);
        nueva.setProfesorId(3);
        nueva.setTipo("Taller");
        nueva.setEstado("Borrador");
        nueva.setPublicada(false);

        // Act
        EvaluacionEntity saved = evaluacionRepository.save(nueva);

        // Assert
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Borrador", saved.getEstado());
        assertFalse(saved.getPublicada());
    }

    @Test
    void save_updateEvaluacion_updatesCorrectly() {
        // Arrange
        testEvaluacion.setEstado("Cerrada");
        testEvaluacion.setPublicada(false);

        // Act
        EvaluacionEntity updated = evaluacionRepository.save(testEvaluacion);

        // Assert
        assertEquals("Cerrada", updated.getEstado());
        assertFalse(updated.getPublicada());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void deleteById_existingEvaluacion_removesEvaluacion() {
        // Act
        evaluacionRepository.deleteById(testEvaluacion.getId());

        // Assert
        Optional<EvaluacionEntity> result = evaluacionRepository.findById(testEvaluacion.getId());
        assertFalse(result.isPresent());
    }
}
