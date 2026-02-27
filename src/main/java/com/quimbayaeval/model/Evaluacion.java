package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Evaluacion
 */
public class Evaluacion {
    private Integer id;
    private String nombre;
    private String descripcion;
    private Integer cursoId;
    private Integer profesorId;
    private String tipo;  // 'Examen', 'Quiz', 'Taller', 'Proyecto', 'Tarea'
    private String estado;  // 'Borrador', 'Programada', 'Activa', 'Cerrada'
    private LocalDateTime deadline;
    private Integer duracionMinutos;
    private Integer intentosPermitidos;
    private Boolean publicada;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public Evaluacion() {
    }

    public Evaluacion(String nombre, Integer cursoId, Integer profesorId, String tipo) {
        this.nombre = nombre;
        this.cursoId = cursoId;
        this.profesorId = profesorId;
        this.tipo = tipo;
        this.estado = "Borrador";
        this.publicada = false;
        this.duracionMinutos = 60;
        this.intentosPermitidos = 1;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Integer getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Integer profesorId) {
        this.profesorId = profesorId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public Integer getIntentosPermitidos() {
        return intentosPermitidos;
    }

    public void setIntentosPermitidos(Integer intentosPermitidos) {
        this.intentosPermitidos = intentosPermitidos;
    }

    public Boolean getPublicada() {
        return publicada;
    }

    public void setPublicada(Boolean publicada) {
        this.publicada = publicada;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Evaluacion{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
