package com.quimbayaeval.model.dto.request;

/**
 * Representa la nueva posición / orden asignado a una pregunta específica.
 */
public class PreguntaOrdenDTO {

    private Integer id;
    private Integer orden;

    public PreguntaOrdenDTO() {}

    public PreguntaOrdenDTO(Integer id, Integer orden) {
        this.id = id;
        this.orden = orden;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
