package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.EvaluacionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para evaluaciones con soporte para filtros dinámicos
 */
@Repository
public interface EvaluacionRepository extends JpaRepository<EvaluacionEntity, Integer>, 
                                              JpaSpecificationExecutor<EvaluacionEntity> {
    
    List<EvaluacionEntity> findByCursoId(Integer cursoId);
    
    List<EvaluacionEntity> findByProfesorId(Integer profesorId);
    
    List<EvaluacionEntity> findByEstado(String estado);
    
    Page<EvaluacionEntity> findByCursoId(Integer cursoId, Pageable pageable);
    
    Page<EvaluacionEntity> findByProfesorId(Integer profesorId, Pageable pageable);
}
