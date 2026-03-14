package com.quimbayaeval.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO con detalle completo de un resultado: incluye nombre del estudiante y evaluación.
 * Usado por docentes para ver notas individuales por curso.
 */
public class ResultadoDetalleDTO {

    private Integer id;
    private Integer submissionId;
    private Integer estudianteId;
    private String estudianteNombre;
    private String estudianteEmail;
    private Integer evaluacionId;
    private String evaluacionNombre;
    private String cursoNombre;
    private BigDecimal puntuacionTotal;
    private BigDecimal puntuacionMaxima;
    private BigDecimal porcentaje;
    private BigDecimal notaEscala;  // escala 1-5: 1 + (porcentaje/100)*4
    private String estadoAprobacion;
    private LocalDateTime fechaResultado;

    public ResultadoDetalleDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public Integer getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Integer estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getEstudianteEmail() { return estudianteEmail; }
    public void setEstudianteEmail(String estudianteEmail) { this.estudianteEmail = estudianteEmail; }

    public Integer getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Integer evaluacionId) { this.evaluacionId = evaluacionId; }

    public String getEvaluacionNombre() { return evaluacionNombre; }
    public void setEvaluacionNombre(String evaluacionNombre) { this.evaluacionNombre = evaluacionNombre; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

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

    public LocalDateTime getFechaResultado() { return fechaResultado; }
    public void setFechaResultado(LocalDateTime fechaResultado) { this.fechaResultado = fechaResultado; }
}
