package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Submission - Representa una evaluación respondida por un estudiante.
 * Estados válidos: Borrador, Enviada, Calificada
 */
public class Submission {
    private Integer id;
    private Integer evaluacionId;
    private Integer estudianteId;
    private String respuestasJson;
    private String estado;
    private Integer intentoNumero;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaEnvio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Submission() {}

    public Submission(Integer evaluacionId, Integer estudianteId) {
        this.evaluacionId = evaluacionId;
        this.estudianteId = estudianteId;
        this.estado = "Borrador";
        this.intentoNumero = 1;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Integer evaluacionId) { this.evaluacionId = evaluacionId; }

    public Integer getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Integer estudianteId) { this.estudianteId = estudianteId; }

    public String getRespuestasJson() { return respuestasJson; }
    public void setRespuestasJson(String respuestasJson) { this.respuestasJson = respuestasJson; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getIntentoNumero() { return intentoNumero; }
    public void setIntentoNumero(Integer intentoNumero) { this.intentoNumero = intentoNumero; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Submission{id=" + id + ", evaluacionId=" + evaluacionId + ", estado='" + estado + "'}";
    }
}
