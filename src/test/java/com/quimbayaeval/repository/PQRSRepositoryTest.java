package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.PQRSEntity;
import com.quimbayaeval.model.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PQRSRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PQRSRepository pqrsRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setName("Test User");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setRole("estudiante");
        testUser.setCreatedAt(LocalDateTime.now());
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void save_validPQRS_returnsSavedPQRS() {
        // Arrange
        PQRSEntity pqrs = new PQRSEntity();
        pqrs.setTipo("Peticion");
        pqrs.setAsunto("Test Subject");
        pqrs.setDescripcion("Test description");
        pqrs.setEstado("Pendiente");
        pqrs.setUsuarioId(testUser.getId());
        pqrs.setFechaCreacion(LocalDateTime.now());

        // Act
        PQRSEntity saved = pqrsRepository.save(pqrs);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTipo()).isEqualTo("Peticion");
        assertThat(saved.getAsunto()).isEqualTo("Test Subject");
        assertThat(saved.getDescripcion()).isEqualTo("Test description");
        assertThat(saved.getEstado()).isEqualTo("Pendiente");
    }

    @Test
    void findById_existingPQRS_returnsPQRS() {
        // Arrange
        PQRSEntity pqrs = new PQRSEntity();
        pqrs.setTipo("Queja");
        pqrs.setAsunto("Test Subject");
        pqrs.setDescripcion("Test queja");
        pqrs.setEstado("Pendiente");
        pqrs.setUsuarioId(testUser.getId());
        pqrs.setFechaCreacion(LocalDateTime.now());
        PQRSEntity saved = entityManager.persist(pqrs);
        entityManager.flush();

        // Act
        Optional<PQRSEntity> found = pqrsRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTipo()).isEqualTo("Queja");
    }

    @Test
    void findAll_multiplePQRS_returnsAllPQRS() {
        // Arrange
        PQRSEntity pqrs1 = new PQRSEntity();
        pqrs1.setTipo("Peticion");
        pqrs1.setAsunto("Subject 1");
        pqrs1.setDescripcion("Test 1");
        pqrs1.setEstado("Pendiente");
        pqrs1.setUsuarioId(testUser.getId());
        pqrs1.setFechaCreacion(LocalDateTime.now());

        PQRSEntity pqrs2 = new PQRSEntity();
        pqrs2.setTipo("Reclamo");
        pqrs2.setAsunto("Subject 2");
        pqrs2.setDescripcion("Test 2");
        pqrs2.setEstado("En Proceso");
        pqrs2.setUsuarioId(testUser.getId());
        pqrs2.setFechaCreacion(LocalDateTime.now());

        entityManager.persist(pqrs1);
        entityManager.persist(pqrs2);
        entityManager.flush();

        // Act
        List<PQRSEntity> all = pqrsRepository.findAll();

        // Assert
        assertThat(all).hasSize(2);
    }

    @Test
    void delete_existingPQRS_removesPQRS() {
        // Arrange
        PQRSEntity pqrs = new PQRSEntity();
        pqrs.setTipo("Sugerencia");
        pqrs.setAsunto("Delete Subject");
        pqrs.setDescripcion("Test delete");
        pqrs.setEstado("Pendiente");
        pqrs.setUsuarioId(testUser.getId());
        pqrs.setFechaCreacion(LocalDateTime.now());
        PQRSEntity saved = entityManager.persist(pqrs);
        entityManager.flush();

        // Act
        pqrsRepository.deleteById(saved.getId());
        Optional<PQRSEntity> found = pqrsRepository.findById(saved.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findByEstado_existingEstado_returnsPQRSList() {
        // Arrange
        PQRSEntity pqrs1 = new PQRSEntity();
        pqrs1.setTipo("Peticion");
        pqrs1.setAsunto("Subject Pendiente");
        pqrs1.setDescripcion("Test 1");
        pqrs1.setEstado("Pendiente");
        pqrs1.setUsuarioId(testUser.getId());
        pqrs1.setFechaCreacion(LocalDateTime.now());

        PQRSEntity pqrs2 = new PQRSEntity();
        pqrs2.setTipo("Queja");
        pqrs2.setAsunto("Subject Resuelta");
        pqrs2.setDescripcion("Test 2");
        pqrs2.setEstado("Resuelta");
        pqrs2.setUsuarioId(testUser.getId());
        pqrs2.setFechaCreacion(LocalDateTime.now());

        entityManager.persist(pqrs1);
        entityManager.persist(pqrs2);
        entityManager.flush();

        // Act
        List<PQRSEntity> pendientes = pqrsRepository.findByEstado("Pendiente");

        // Assert
        assertThat(pendientes).hasSize(1);
        assertThat(pendientes.get(0).getEstado()).isEqualTo("Pendiente");
    }
}
