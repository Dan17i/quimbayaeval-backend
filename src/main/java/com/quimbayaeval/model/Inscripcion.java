package com.quimbayaeval.model;

/**
 * Entidad Inscripcion - Relación entre Estudiante y Curso
 */
public class Inscripcion {
    private Integer id;
    private Integer estudianteId;
    private Integer cursoId;
    private String fechaInscripcion;

    // Constructores
    public Inscripcion() {
    }

    public Inscripcion(Integer estudianteId, Integer cursoId) {
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Integer estudianteId) {
        this.estudianteId = estudianteId;
    }

    public Integer getCursoId() {
        return cursoId;
    }

    public void setCursoId(Integer cursoId) {
        this.cursoId = cursoId;
    }

    public String getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(String fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    @Override
    public String toString() {
        return "Inscripcion{" +
                "id=" + id +
                ", estudianteId=" + estudianteId +
                ", cursoId=" + cursoId +
                '}';
    }
}
