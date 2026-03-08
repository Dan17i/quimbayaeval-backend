package com.quimbayaeval.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para crear evaluación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearEvaluacionRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 255, message = "El nombre debe tener entre 3 y 255 caracteres")
    private String nombre;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El curso es obligatorio")
    @Positive(message = "El ID del curso debe ser positivo")
    private Integer cursoId;
    
    @NotNull(message = "El profesor es obligatorio")
    @Positive(message = "El ID del profesor debe ser positivo")
    private Integer profesorId;
    
    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "Examen|Quiz|Taller|Proyecto|Tarea", 
             message = "Tipo inválido. Debe ser: Examen, Quiz, Taller, Proyecto o Tarea")
    private String tipo;
    
    @Future(message = "La fecha límite debe ser futura")
    private LocalDateTime deadline;
    
    @Positive(message = "La duración debe ser positiva")
    @Max(value = 480, message = "La duración no puede exceder 480 minutos (8 horas)")
    private Integer duracionMinutos;
    
    @Positive(message = "Los intentos deben ser positivos")
    @Max(value = 10, message = "No se permiten más de 10 intentos")
    private Integer intentosPermitidos;
}
