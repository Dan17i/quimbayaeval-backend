package com.quimbayaeval.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA para evaluaciones
 */
@Entity
@Table(name = "evaluaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String nombre;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "curso_id", nullable = false)
    private Integer cursoId;
    
    @Column(name = "profesor_id", nullable = false)
    private Integer profesorId;
    
    @Column(nullable = false, length = 50)
    private String tipo;  // 'Examen', 'Quiz', 'Taller', 'Proyecto', 'Tarea'
    
    @Column(nullable = false, length = 50)
    private String estado = "Borrador";  // 'Borrador', 'Programada', 'Activa', 'Cerrada'
    
    private LocalDateTime deadline;
    
    @Column(name = "duracion_minutos")
    private Integer duracionMinutos = 60;
    
    @Column(name = "intentos_permitidos")
    private Integer intentosPermitidos = 1;
    
    @Column(nullable = false)
    private Boolean publicada = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
