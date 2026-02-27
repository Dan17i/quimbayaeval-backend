package com.quimbayaeval.model;

/**
 * Entidad Pregunta
 */
public class Pregunta {
    private Integer id;
    private Integer evaluacionId;
    private String enunciado;
    private String tipo;  // 'seleccion_multiple', 'verdadero_falso', 'respuesta_corta', 'ensayo'
    private Double puntuacion;
    private Integer orden;
    private String opcionesJson;  // Almacena opciones como JSON
    private String respuestaCorrectaJson;  // Almacena respuesta correcta como JSON
    private String createdAt;
    private String updatedAt;

    // Constructores
    public Pregunta() {
    }

    public Pregunta(Integer evaluacionId, String enunciado, String tipo) {
        this.evaluacionId = evaluacionId;
        this.enunciado = enunciado;
        this.tipo = tipo;
        this.puntuacion = 1.0;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getEvaluacionId() {
        return evaluacionId;
    }

    public void setEvaluacionId(Integer evaluacionId) {
        this.evaluacionId = evaluacionId;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(Double puntuacion) {
        this.puntuacion = puntuacion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getOpcionesJson() {
        return opcionesJson;
    }

    public void setOpcionesJson(String opcionesJson) {
        this.opcionesJson = opcionesJson;
    }

    public String getRespuestaCorrectaJson() {
        return respuestaCorrectaJson;
    }

    public void setRespuestaCorrectaJson(String respuestaCorrectaJson) {
        this.respuestaCorrectaJson = respuestaCorrectaJson;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Pregunta{" +
                "id=" + id +
                ", evaluacionId=" + evaluacionId +
                ", tipo='" + tipo + '\'' +
                ", puntuacion=" + puntuacion +
                '}';
    }
}
