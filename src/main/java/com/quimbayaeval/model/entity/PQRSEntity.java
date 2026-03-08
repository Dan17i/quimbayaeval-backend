package com.quimbayaeval.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad JPA para PQRS
 */
@Entity
@Table(name = "pqrs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PQRSEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 50)
    private String tipo;  // 'Pregunta', 'Reclamo', 'Sugerencia', 'Queja'
    
    @Column(nullable = false)
    private String asunto;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "curso_id")
    private Integer cursoId;
    
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;
    
    @Column(nullable = false, length = 50)
    private String estado = "Pendiente";  // 'Pendiente', 'En Proceso', 'Resuelto'
    
    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_respuesta")
    private LocalDateTime fechaRespuesta;
    
    @Column(columnDefinition = "TEXT")
    private String respuesta;
    
    @Column(name = "respondido_por_id")
    private Integer respondidoPorId;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
