package com.quimbayaeval.service;

import com.quimbayaeval.config.CustomMetrics;
import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.dao.JdbcQueryBuilder;
import com.quimbayaeval.model.Evaluacion;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para Evaluaciones con soporte para filtros avanzados y métricas
 */
@Slf4j
@Service
public class EvaluacionService {

    @Autowired
    private EvaluacionDao evaluacionDao;
    
    @Autowired(required = false)
    private CustomMetrics customMetrics;

    /**
     * Crea una nueva evaluación
     */
    @Timed(value = "evaluacion.create", description = "Tiempo de creación de evaluación")
    public Evaluacion crear(Evaluacion evaluacion) {
        log.info("Creando evaluación: nombre={}, cursoId={}", 
                 evaluacion.getNombre(), evaluacion.getCursoId());
        
        Timer.Sample sample = customMetrics != null ? customMetrics.startEvaluacionCreationTimer() : null;
        try {
            Evaluacion nueva = evaluacionDao.save(evaluacion);
            if (customMetrics != null) {
                customMetrics.incrementEvaluacionCreated();
            }
            log.info("Evaluación creada exitosamente: id={}", nueva.getId());
            return nueva;
        } finally {
            if (sample != null && customMetrics != null) {
                customMetrics.stopEvaluacionCreationTimer(sample);
            }
        }
    }

    /**
     * Obtiene evaluación por ID
     */
    public Optional<Evaluacion> obtenerPorId(Integer id) {
        return evaluacionDao.findById(id);
    }

    /**
     * Obtiene todas las evaluaciones
     */
    public List<Evaluacion> obtenerTodas() {
        return evaluacionDao.findAll();
    }

    /**
     * Obtiene evaluaciones con criterios dinámicos (sin filtros avanzados)
     */
    public List<Evaluacion> obtenerTodas(Map<String, Object> filters,
                                         Integer page,
                                         Integer size,
                                         String sort,
                                         String dir) {
        return evaluacionDao.findAll(filters, page, size, sort, dir);
    }

    /**
     * Obtiene evaluaciones con filtros avanzados (LIKE, ENTRE, etc.)
     */
    public List<Evaluacion> obtenerConFiltrosAvanzados(List<JdbcQueryBuilder.FilterCriteria> filters,
                                                       Integer page,
                                                       Integer size,
                                                       String sort,
                                                       String direction) {
        return evaluacionDao.findAllAdvanced(filters, page, size, sort, direction);
    }

    /**
     * Obtiene evaluaciones por curso
     */
    public List<Evaluacion> obtenerPorCurso(Integer cursoId) {
        return evaluacionDao.findByCurso(cursoId);
    }

    /**
     * Obtiene evaluaciones del profesor
     */
    public List<Evaluacion> obtenerDelProfesor(Integer profesorId) {
        return evaluacionDao.findByProfesor(profesorId);
    }

    /**
     * Obtiene evaluaciones activas
     */
    public List<Evaluacion> obtenerActivas() {
        return evaluacionDao.findByEstado("Activa");
    }

    /**
     * Obtiene evaluaciones por estado
     */
    public List<Evaluacion> obtenerPorEstado(String estado) {
        return evaluacionDao.findByEstado(estado);
    }

    /**
     * Actualiza una evaluación
     */
    public void actualizar(Evaluacion evaluacion) {
        evaluacionDao.update(evaluacion);
    }

    /**
     * Publica una evaluación
     */
    public void publicar(Integer id) {
        Optional<Evaluacion> evalOpt = evaluacionDao.findById(id);
        if (evalOpt.isPresent()) {
            Evaluacion eval = evalOpt.get();
            eval.setPublicada(true);
            eval.setEstado("Activa");
            evaluacionDao.update(eval);
        }
    }
    
    /**
     * Elimina una evaluación por su ID
     *
     * @param id Identificador de la evaluación a eliminar
     */
    public void eliminar(Integer id) {
        evaluacionDao.deleteById(id);
    }

}
