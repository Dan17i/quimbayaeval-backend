package com.quimbayaeval.model;

import java.time.LocalDateTime;

/**
 * Entidad Curso
 */
public class Curso {
    private Integer id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Integer profesorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructores
    public Curso() {
    }

    public Curso(String codigo, String nombre, Integer profesorId) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.profesorId = profesorId;
    }

    public Curso(Integer id, String codigo, String nombre, String descripcion, Integer profesorId) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.profesorId = profesorId;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
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

    public Integer getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Integer profesorId) {
        this.profesorId = profesorId;
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
        return "Curso{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}
