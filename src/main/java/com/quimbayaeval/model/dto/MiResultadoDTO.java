package com.quimbayaeval.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para el historial de resultados del estudiante autenticado.
 * Incluye nombres de evaluación, curso y profesor para mostrar en HistorialPage.
 */
public class MiResultadoDTO {

    private Integer id;
    private Integer submissionId;
    private String evaluacionNombre;
    private String cursoNombre;
    private String profesorNombre;
    private BigDecimal puntuacionTotal;
    private BigDecimal puntuacionMaxima;
    private BigDecimal porcentaje;
    private BigDecimal notaEscala;       // 1 + (porcentaje/100)*4
    private String estadoAprobacion;
    private LocalDateTime createdAt;

    public MiResultadoDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public String getEvaluacionNombre() { return evaluacionNombre; }
    public void setEvaluacionNombre(String evaluacionNombre) { this.evaluacionNombre = evaluacionNombre; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public String getProfesorNombre() { return profesorNombre; }
    public void setProfesorNombre(String profesorNombre) { this.profesorNombre = profesorNombre; }

    public BigDecimal getPuntuacionTotal() { return puntuacionTotal; }
    public void setPuntuacionTotal(BigDecimal puntuacionTotal) { this.puntuacionTotal = puntuacionTotal; }

    public BigDecimal getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(BigDecimal puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public BigDecimal getNotaEscala() { return notaEscala; }
    public void setNotaEscala(BigDecimal notaEscala) { this.notaEscala = notaEscala; }

    public String getEstadoAprobacion() { return estadoAprobacion; }
    public void setEstadoAprobacion(String estadoAprobacion) { this.estadoAprobacion = estadoAprobacion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
