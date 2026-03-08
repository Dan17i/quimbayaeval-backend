package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.PQRSEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para PQRS
 */
@Repository
public interface PQRSRepository extends JpaRepository<PQRSEntity, Integer> {
    
    List<PQRSEntity> findByUsuarioId(Integer usuarioId);
    
    List<PQRSEntity> findByEstado(String estado);
    
    List<PQRSEntity> findByTipo(String tipo);
    
    List<PQRSEntity> findByCursoId(Integer cursoId);
}
