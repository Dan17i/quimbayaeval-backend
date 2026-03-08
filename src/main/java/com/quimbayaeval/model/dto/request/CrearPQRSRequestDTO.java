package com.quimbayaeval.model.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear PQRS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPQRSRequestDTO {
    
    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "Pregunta|Reclamo|Sugerencia|Queja", 
             message = "Tipo inválido. Debe ser: Pregunta, Reclamo, Sugerencia o Queja")
    private String tipo;
    
    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 5, max = 255, message = "El asunto debe tener entre 5 y 255 caracteres")
    private String asunto;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    private String descripcion;
    
    @Positive(message = "El ID del curso debe ser positivo")
    private Integer cursoId;
}
