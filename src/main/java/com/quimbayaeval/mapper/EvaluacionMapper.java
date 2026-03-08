package com.quimbayaeval.mapper;

import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.model.dto.request.CrearEvaluacionRequestDTO;
import com.quimbayaeval.model.entity.EvaluacionEntity;

/**
 * Mapper para convertir entre Evaluacion Entity/DTO
 */
public class EvaluacionMapper {
    
    public static Evaluacion toDTO(EvaluacionEntity entity) {
        if (entity == null) return null;
        
        Evaluacion dto = new Evaluacion();
        dto.setId(entity.getId());
        dto.setNombre(entity.getNombre());
        dto.setDescripcion(entity.getDescripcion());
        dto.setCursoId(entity.getCursoId());
        dto.setProfesorId(entity.getProfesorId());
        dto.setTipo(entity.getTipo());
        dto.setEstado(entity.getEstado());
        dto.setDeadline(entity.getDeadline());
        dto.setDuracionMinutos(entity.getDuracionMinutos());
        dto.setIntentosPermitidos(entity.getIntentosPermitidos());
        dto.setPublicada(entity.getPublicada());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
    
    public static EvaluacionEntity toEntity(Evaluacion dto) {
        if (dto == null) return null;
        
        EvaluacionEntity entity = new EvaluacionEntity();
        entity.setId(dto.getId());
        entity.setNombre(dto.getNombre());
        entity.setDescripcion(dto.getDescripcion());
        entity.setCursoId(dto.getCursoId());
        entity.setProfesorId(dto.getProfesorId());
        entity.setTipo(dto.getTipo());
        entity.setEstado(dto.getEstado());
        entity.setDeadline(dto.getDeadline());
        entity.setDuracionMinutos(dto.getDuracionMinutos());
        entity.setIntentosPermitidos(dto.getIntentosPermitidos());
        entity.setPublicada(dto.getPublicada());
        return entity;
    }
    
    public static EvaluacionEntity fromRequest(CrearEvaluacionRequestDTO request) {
        if (request == null) return null;
        
        EvaluacionEntity entity = new EvaluacionEntity();
        entity.setNombre(request.getNombre());
        entity.setDescripcion(request.getDescripcion());
        entity.setCursoId(request.getCursoId());
        entity.setProfesorId(request.getProfesorId());
        entity.setTipo(request.getTipo());
        entity.setEstado("Borrador");
        entity.setDeadline(request.getDeadline());
        entity.setDuracionMinutos(request.getDuracionMinutos() != null ? request.getDuracionMinutos() : 60);
        entity.setIntentosPermitidos(request.getIntentosPermitidos() != null ? request.getIntentosPermitidos() : 1);
        entity.setPublicada(false);
        return entity;
    }
}
