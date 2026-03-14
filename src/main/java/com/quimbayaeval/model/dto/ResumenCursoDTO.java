package com.quimbayaeval.model.dto;

import java.math.BigDecimal;

/**
 * DTO con resumen estadístico por evaluación dentro de un curso.
 * Usado por coordinadores para ver promedios grupales.
 */
public class ResumenCursoDTO {

    private Integer evaluacionId;
    private String evaluacionNombre;
    private BigDecimal promedioGrupo;    // porcentaje promedio
    private BigDecimal promedioEscala;   // escala 1-5: 1 + (promedioGrupo/100)*4
    private Integer totalEstudiantes;
    private Integer aprobados;
    private Integer reprobados;

    public ResumenCursoDTO() {}

    public Integer getEvaluacionId() { return evaluacionId; }
    public void setEvaluacionId(Integer evaluacionId) { this.evaluacionId = evaluacionId; }

    public String getEvaluacionNombre() { return evaluacionNombre; }
    public void setEvaluacionNombre(String evaluacionNombre) { this.evaluacionNombre = evaluacionNombre; }

    public BigDecimal getPromedioGrupo() { return promedioGrupo; }
    public void setPromedioGrupo(BigDecimal promedioGrupo) { this.promedioGrupo = promedioGrupo; }

    public BigDecimal getPromedioEscala() { return promedioEscala; }
    public void setPromedioEscala(BigDecimal promedioEscala) { this.promedioEscala = promedioEscala; }

    public Integer getTotalEstudiantes() { return totalEstudiantes; }
    public void setTotalEstudiantes(Integer totalEstudiantes) { this.totalEstudiantes = totalEstudiantes; }

    public Integer getAprobados() { return aprobados; }
    public void setAprobados(Integer aprobados) { this.aprobados = aprobados; }

    public Integer getReprobados() { return reprobados; }
    public void setReprobados(Integer reprobados) { this.reprobados = reprobados; }
}
