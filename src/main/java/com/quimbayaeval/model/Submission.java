package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Submission - Representa una evaluación respondida por un estudiante
 */
public class Submission {
    private Integer id;
    private Integer evaluacionId;
    private Integer estudianteId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;
    private String estado;  // 'en_progreso', 'completada', 'calificada'

    // Constructores
    public Submission() {
    }

    public Submission(Integer evaluacionId, Integer estudianteId) {
        this.evaluacionId = evaluacionId;
        this.estudianteId = estudianteId;
        this.estado = "en_progreso";
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEvaluacionId() {
        return evaluacionId;
    }

    public void setEvaluacionId(Integer evaluacionId) {
        this.evaluacionId = evaluacionId;
    }

    public Integer getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Integer estudianteId) {
        this.estudianteId = estudianteId;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", evaluacionId=" + evaluacionId +
                ", estudianteId=" + estudianteId +
                ", estado='" + estado + '\'' +
                '}';
    }
}
