package com.quimbayaeval.repository;

import com.quimbayaeval.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para usuarios
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findByEmailAndRole(String email, String role);
    
    boolean existsByEmail(String email);
}
