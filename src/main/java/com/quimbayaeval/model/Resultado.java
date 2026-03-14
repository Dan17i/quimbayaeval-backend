package com.quimbayaeval.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Resultado {
    private Integer id;
    private Integer submissionId;
    private Integer evaluacionId;
    private Integer estudianteId;
    private BigDecimal puntuacionTotal;
    private BigDecimal puntuacionMaxima;
    private BigDecimal porcentaje;
    private String estadoAprobacion;
    private String observaciones;
    private LocalDateTime fechaResultado;
    private LocalDateTime createdAt;

    public Resultado() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public Integer getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Integer evaluacionId) { this.evaluacionId = evaluacionId; }

    public Integer getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Integer estudianteId) { this.estudianteId = estudianteId; }

    public BigDecimal getPuntuacionTotal() { return puntuacionTotal; }
    public void setPuntuacionTotal(BigDecimal puntuacionTotal) { this.puntuacionTotal = puntuacionTotal; }

    public BigDecimal getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(BigDecimal puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public BigDecimal getPorcentaje() { return porcentaje; }
    public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }

    public String getEstadoAprobacion() { return estadoAprobacion; }
    public void setEstadoAprobacion(String estadoAprobacion) { this.estadoAprobacion = estadoAprobacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getFechaResultado() { return fechaResultado; }
    public void setFechaResultado(LocalDateTime fechaResultado) { this.fechaResultado = fechaResultado; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
