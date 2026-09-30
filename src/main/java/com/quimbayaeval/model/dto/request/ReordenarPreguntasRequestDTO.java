package com.quimbayaeval.model.dto.request;

import java.util.ArrayList;
import java.util.List;

/**
 * Petición para reordenar un conjunto de preguntas de una evaluación de forma atómica.
 */
public class ReordenarPreguntasRequestDTO {

    private List<PreguntaOrdenDTO> ordenes = new ArrayList<>();

    public ReordenarPreguntasRequestDTO() {}

    public ReordenarPreguntasRequestDTO(List<PreguntaOrdenDTO> ordenes) {
        this.ordenes = ordenes;
    }

    public List<PreguntaOrdenDTO> getOrdenes() { return ordenes; }
    public void setOrdenes(List<PreguntaOrdenDTO> ordenes) { this.ordenes = ordenes; }
}
