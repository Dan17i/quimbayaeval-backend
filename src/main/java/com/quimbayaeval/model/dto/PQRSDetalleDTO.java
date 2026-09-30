package com.quimbayaeval.model.dto;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * DTO enriquecido para PQRS con nombres relacionales, curso y métricas de SLA/Trazabilidad.
 * Evita la sobrecarga de consultas 'client-side joins' en el frontend.
 */
public class PQRSDetalleDTO {

    private Integer id;
    private String tipo;
    private String asunto;
    private String descripcion;
    private Integer cursoId;
    private String cursoNombre;
    private String cursoCodigo;
    private Integer usuarioId;
    private String usuarioNombre;
    private String usuarioEmail;
    private String estado;
    private String destinatario;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRespuesta;
    private String respuesta;
    private Integer respondidoPorId;
    private String respondidoPorNombre;
    private Long diasTranscurridos;
    private Boolean enPlazo;
    private String estadoSLA; // 'A_TIEMPO', 'PROXIMO_A_VENCER', 'VENCIDO', 'ATENDIDO_A_TIEMPO', 'ATENDIDO_FUERA_DE_PLAZO'
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PQRSDetalleDTO() {}

    public void calcularSLA() {
        LocalDateTime baseInicio = fechaCreacion != null ? fechaCreacion : createdAt;
        if (baseInicio == null) {
            this.diasTranscurridos = 0L;
            this.enPlazo = true;
            this.estadoSLA = "A_TIEMPO";
            return;
        }

        LocalDateTime finCalculo = fechaRespuesta != null ? fechaRespuesta : LocalDateTime.now();
        this.diasTranscurridos = Math.max(0, Duration.between(baseInicio, finCalculo).toDays());

        final long PLAZO_MAX_DIAS = 15;
        this.enPlazo = this.diasTranscurridos <= PLAZO_MAX_DIAS;

        if (fechaRespuesta != null) {
            this.estadoSLA = this.enPlazo ? "ATENDIDO_A_TIEMPO" : "ATENDIDO_FUERA_DE_PLAZO";
        } else {
            if (this.diasTranscurridos >= PLAZO_MAX_DIAS) {
                this.estadoSLA = "VENCIDO";
            } else if (this.diasTranscurridos >= 10) {
                this.estadoSLA = "PROXIMO_A_VENCER";
            } else {
                this.estadoSLA = "A_TIEMPO";
            }
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getCursoId() { return cursoId; }
    public void setCursoId(Integer cursoId) { this.cursoId = cursoId; }

    public String getCursoNombre() { return cursoNombre; }
    public void setCursoNombre(String cursoNombre) { this.cursoNombre = cursoNombre; }

    public String getCursoCodigo() { return cursoCodigo; }
    public void setCursoCodigo(String cursoCodigo) { this.cursoCodigo = cursoCodigo; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getUsuarioEmail() { return usuarioEmail; }
    public void setUsuarioEmail(String usuarioEmail) { this.usuarioEmail = usuarioEmail; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) { this.fechaRespuesta = fechaRespuesta; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public Integer getRespondidoPorId() { return respondidoPorId; }
    public void setRespondidoPorId(Integer respondidoPorId) { this.respondidoPorId = respondidoPorId; }

    public String getRespondidoPorNombre() { return respondidoPorNombre; }
    public void setRespondidoPorNombre(String respondidoPorNombre) { this.respondidoPorNombre = respondidoPorNombre; }

    public Long getDiasTranscurridos() { return diasTranscurridos; }
    public void setDiasTranscurridos(Long diasTranscurridos) { this.diasTranscurridos = diasTranscurridos; }

    public Boolean getEnPlazo() { return enPlazo; }
    public void setEnPlazo(Boolean enPlazo) { this.enPlazo = enPlazo; }

    public String getEstadoSLA() { return estadoSLA; }
    public void setEstadoSLA(String estadoSLA) { this.estadoSLA = estadoSLA; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
