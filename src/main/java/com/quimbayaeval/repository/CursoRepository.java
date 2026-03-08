package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.CursoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para cursos
 */
@Repository
public interface CursoRepository extends JpaRepository<CursoEntity, Integer> {
    
    Optional<CursoEntity> findByCodigo(String codigo);
    
    List<CursoEntity> findByProfesorId(Integer profesorId);
    
    boolean existsByCodigo(String codigo);
}
