package com.quimbayaeval.model.dto.request;

import java.util.ArrayList;
import java.util.List;

/**
 * Petición para calificar de manera atómica y en lote todas las preguntas de una entrega (submission),
 * incluyendo observaciones generales para el resultado final.
 */
public class CalificacionBatchRequestDTO {

    private Integer submissionId;
    private List<CalificacionItemDTO> calificaciones = new ArrayList<>();
    private String observacionesGenerales;

    public CalificacionBatchRequestDTO() {}

    public Integer getSubmissionId() { return submissionId; }
    public void setSubmissionId(Integer submissionId) { this.submissionId = submissionId; }

    public List<CalificacionItemDTO> getCalificaciones() { return calificaciones; }
    public void setCalificaciones(List<CalificacionItemDTO> calificaciones) { this.calificaciones = calificaciones; }

    public String getObservacionesGenerales() { return observacionesGenerales; }
    public void setObservacionesGenerales(String observacionesGenerales) { this.observacionesGenerales = observacionesGenerales; }
}
