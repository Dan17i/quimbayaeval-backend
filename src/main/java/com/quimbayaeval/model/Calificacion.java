package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Calificacion - Puntuación otorgada a una submission.
 */
public class Calificacion {
    private Integer id;
    private Integer submissionId;
    private Integer preguntaId;
    private Double puntuacionObtenida;
    private Double puntuacionMaxima;
    private String retroalimentacion;
    private Integer calificadoPorId;
    private LocalDateTime fechaCalificacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Calificacion() {}

    public Calificacion(Integer submissionId, Integer preguntaId, Double puntuacionObtenida) {
        this.submissionId = submissionId;
        this.preguntaId = preguntaId;
        this.puntuacionObtenida = puntuacionObtenida;
        this.fechaCalificacion = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public Integer getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Integer preguntaId) { this.preguntaId = preguntaId; }

    public Double getPuntuacionObtenida() { return puntuacionObtenida; }
    public void setPuntuacionObtenida(Double puntuacionObtenida) { this.puntuacionObtenida = puntuacionObtenida; }

    public Double getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(Double puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }

    public Integer getCalificadoPorId() { return calificadoPorId; }
    public void setCalificadoPorId(Integer calificadoPorId) { this.calificadoPorId = calificadoPorId; }

    public LocalDateTime getFechaCalificacion() { return fechaCalificacion; }
    public void setFechaCalificacion(LocalDateTime fechaCalificacion) { this.fechaCalificacion = fechaCalificacion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
