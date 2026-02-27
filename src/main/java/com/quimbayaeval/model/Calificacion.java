package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Calificacion - calificación otorgada a una pregunta de una submission
 */
public class Calificacion {
    private Integer id;
    private Integer submissionId;
    private Integer preguntaId;
    private Double calificacion;
    private String comentario;
    private Integer calificadaPorId;
    private LocalDateTime fechaCalificacion;

    public Calificacion() {
    }

    public Calificacion(Integer submissionId, Integer preguntaId, Double calificacion) {
        this.submissionId = submissionId;
        this.preguntaId = preguntaId;
        this.calificacion = calificacion;
        this.fechaCalificacion = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Integer submissionId) {
        this.submissionId = submissionId;
    }

    public Integer getPreguntaId() {
        return preguntaId;
    }

    public void setPreguntaId(Integer preguntaId) {
        this.preguntaId = preguntaId;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getCalificadaPorId() {
        return calificadaPorId;
    }

    public void setCalificadaPorId(Integer calificadaPorId) {
        this.calificadaPorId = calificadaPorId;
    }

    public LocalDateTime getFechaCalificacion() {
        return fechaCalificacion;
    }

    public void setFechaCalificacion(LocalDateTime fechaCalificacion) {
        this.fechaCalificacion = fechaCalificacion;
    }
}
