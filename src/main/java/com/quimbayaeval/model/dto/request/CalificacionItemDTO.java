package com.quimbayaeval.model.dto.request;

/**
 * Item individual para calificación de una pregunta en una submission.
 */
public class CalificacionItemDTO {

    private Integer preguntaId;
    private Double puntuacionObtenida;
    private Double puntuacionMaxima;
    private String retroalimentacion;

    public CalificacionItemDTO() {}

    public CalificacionItemDTO(Integer preguntaId, Double puntuacionObtenida, Double puntuacionMaxima, String retroalimentacion) {
        this.preguntaId = preguntaId;
        this.puntuacionObtenida = puntuacionObtenida;
        this.puntuacionMaxima = puntuacionMaxima;
        this.retroalimentacion = retroalimentacion;
    }

    public Integer getPreguntaId() { return preguntaId; }
    public void setPreguntaId(Integer preguntaId) { this.preguntaId = preguntaId; }

    public Double getPuntuacionObtenida() { return puntuacionObtenida; }
    public void setPuntuacionObtenida(Double puntuacionObtenida) { this.puntuacionObtenida = puntuacionObtenida; }

    public Double getPuntuacionMaxima() { return puntuacionMaxima; }
    public void setPuntuacionMaxima(Double puntuacionMaxima) { this.puntuacionMaxima = puntuacionMaxima; }

    public String getRetroalimentacion() { return retroalimentacion; }
    public void setRetroalimentacion(String retroalimentacion) { this.retroalimentacion = retroalimentacion; }
}
