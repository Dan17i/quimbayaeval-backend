package com.quimbayaeval.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO enriquecido para Submissions.
 * Incluye datos relacionales del estudiante, evaluación, curso y resultado consolidado,
 * eliminando la necesidad de realizar 'client-side joins' en el frontend.
 */
public class SubmissionDetalleDTO {

    private Integer id;
    private Integer evaluacionId;
    private String evaluacionNombre;
    private Integer cursoId;
    private String cursoNombre;
    private String cursoCodigo;
    private Integer estudianteId;
    private String estudianteNombre;
    private String estudianteEmail;
    private String respuestasJson;
    private String estado;
    private Integer intentoNumero;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaEnvio;
    private BigDecimal puntuacionTotal;
    private BigDecimal puntuacionMaxima;
    private BigDecimal porcentaje;
    private BigDecimal notaEscala;
    private String estadoAprobacion;
    private String observaciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SubmissionDetalleDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Integer evaluacionId) { this.evaluacionId = evaluacionId; }

    public String getEvaluacionNombre() { return evaluacionNombre; }
    public void setEvaluacionNombre(String evaluacionNombre) { this.evaluacionNombre = evaluacionNombre; }

    public Integer getCursoId() { return cursoId; }
    public void setCursoId(Integer cursoId) { this.cursoId = cursoId; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public String getCursoCodigo() { return cursoCodigo; }
    public void setCursoCodigo(String cursoCodigo) { this.cursoCodigo = cursoCodigo; }

    public Integer getEstudianteId() { return estudianteId; }
    public void setEstudianteId(Integer estudianteId) { this.estudianteId = estudianteId; }

    public String getEstudianteNombre() { return estudianteNombre; }
    public void setEstudianteNombre(String estudianteNombre) { this.estudianteNombre = estudianteNombre; }

    public String getEstudianteEmail() { return estudianteEmail; }
    public void setEstudianteEmail(String estudianteEmail) { this.estudianteEmail = estudianteEmail; }

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

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
