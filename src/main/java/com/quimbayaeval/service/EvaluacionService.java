package com.quimbayaeval.service;

import com.quimbayaeval.config.CustomMetrics;
import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.dao.JdbcQueryBuilder;
import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.exception.UnauthorizedException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private com.quimbayaeval.dao.PreguntaDao preguntaDao;
    
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
     * Obtiene evaluaciones activas (maestro/coordinador: todas; estudiante: ver obtenerActivasParaEstudiante)
     */
    public List<Evaluacion> obtenerActivas() {
        return evaluacionDao.findByEstado("Activa");
    }

    /**
     * Obtiene evaluaciones activas y publicadas de los cursos donde el estudiante está matriculado.
     */
    public List<Evaluacion> obtenerActivasParaEstudiante(Integer estudianteId) {
        return evaluacionDao.findActivasByEstudiante(estudianteId);
    }

    /**
     * Obtiene evaluaciones por estado
     */
    public List<Evaluacion> obtenerPorEstado(String estado) {
        return evaluacionDao.findByEstado(estado);
    }

    /**
     * Actualiza una evaluación.
     * El coordinador puede editar cualquiera; el maestro solo las suyas.
     */
    @Transactional
    public void actualizar(Evaluacion evaluacion, Integer profesorIdJwt, String rolJwt) {
        if (!"coordinador".equals(rolJwt)) {
            Evaluacion existente = evaluacionDao.findById(evaluacion.getId())
                .orElseThrow(() -> new com.quimbayaeval.exception.ResourceNotFoundException(
                    "Evaluacion", "id", evaluacion.getId()));
            if (!existente.getProfesorId().equals(profesorIdJwt)) {
                throw new UnauthorizedException(
                    "Solo el profesor propietario puede modificar esta evaluación");
            }
        }
        evaluacionDao.update(evaluacion);
    }

    /**
     * Publica una evaluación validando que cuente con al menos una pregunta registrada.
     */
    @Transactional
    public void publicar(Integer id) {
        Optional<Evaluacion> evalOpt = evaluacionDao.findById(id);
        if (evalOpt.isPresent()) {
            Evaluacion eval = evalOpt.get();
            if (preguntaDao != null) {
                List<com.quimbayaeval.model.Pregunta> preguntas = preguntaDao.findByEvaluacion(id);
                if (preguntas == null || preguntas.isEmpty()) {
                    throw new com.quimbayaeval.exception.BusinessValidationException(
                        "No se puede publicar una evaluación que no contenga preguntas registradas");
                }
            }
            eval.setPublicada(true);
            eval.setEstado("Activa");
            evaluacionDao.update(eval);
        }
    }

    /**
     * Clona una evaluación completa junto con todas sus preguntas en una sola transacción.
     */
    @Transactional
    public Evaluacion duplicar(Integer id, Integer profesorIdJwt, String rolJwt) {
        Evaluacion original = evaluacionDao.findById(id)
            .orElseThrow(() -> new com.quimbayaeval.exception.ResourceNotFoundException("Evaluacion", "id", id));

        if (!"coordinador".equals(rolJwt) && profesorIdJwt != null && !original.getProfesorId().equals(profesorIdJwt)) {
            throw new UnauthorizedException("Solo el profesor propietario o un coordinador pueden duplicar esta evaluación");
        }

        Evaluacion clon = new Evaluacion();
        clon.setNombre("Copia de " + original.getNombre());
        clon.setDescripcion(original.getDescripcion());
        clon.setCursoId(original.getCursoId());
        clon.setProfesorId(profesorIdJwt != null ? profesorIdJwt : original.getProfesorId());
        clon.setTipo(original.getTipo());
        clon.setEstado("Programada");
        clon.setPublicada(false);
        clon.setDuracionMinutos(original.getDuracionMinutos());
        clon.setIntentosPermitidos(original.getIntentosPermitidos());
        clon.setDeadline(original.getDeadline());

        Evaluacion guardada = evaluacionDao.save(clon);

        List<com.quimbayaeval.model.Pregunta> preguntasOriginales = preguntaDao.findByEvaluacion(id);
        for (com.quimbayaeval.model.Pregunta p : preguntasOriginales) {
            com.quimbayaeval.model.Pregunta clonP = new com.quimbayaeval.model.Pregunta();
            clonP.setEvaluacionId(guardada.getId());
            clonP.setEnunciado(p.getEnunciado());
            clonP.setTipo(p.getTipo());
            clonP.setPuntuacion(p.getPuntuacion());
            clonP.setOrden(p.getOrden());
            clonP.setOpcionesJson(p.getOpcionesJson());
            clonP.setRespuestaCorrectaJson(p.getRespuestaCorrectaJson());
            preguntaDao.save(clonP);
        }

        return guardada;
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
