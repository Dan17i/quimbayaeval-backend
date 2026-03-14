package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad PQRS - Peticiones, Quejas, Reclamos, Sugerencias
 */
public class PQRS {
    private Integer id;
    private String tipo;  // 'Pregunta', 'Reclamo', 'Sugerencia', 'Queja'
    private String asunto;
    private String descripcion;
    private Integer cursoId;
    private Integer usuarioId;
    private String estado;  // 'Pendiente', 'En Proceso', 'Resuelto'
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRespuesta;
    private String respuesta;
    private Integer respondidoPorId;
    private LocalDateTime updatedAt;
    private LocalDateTime createdAt;

    // Constructores
    public PQRS() {
    }

    public PQRS(String tipo, String asunto, String descripcion, Integer usuarioId) {
        this.tipo = tipo;
        this.asunto = asunto;
        this.descripcion = descripcion;
        this.usuarioId = usuarioId;
        this.estado = "Pendiente";
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCursoId() {
        return cursoId;
    }

    public void setCursoId(Integer cursoId) {
        this.cursoId = cursoId;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public Integer getRespondidoPorId() {
        return respondidoPorId;
    }

    public void setRespondidoPorId(Integer respondidoPorId) {
        this.respondidoPorId = respondidoPorId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PQRS{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", asunto='" + asunto + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
