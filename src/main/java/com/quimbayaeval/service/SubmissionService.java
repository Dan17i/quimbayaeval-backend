package com.quimbayaeval.service;

import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.dao.SubmissionDao;
import com.quimbayaeval.exception.BusinessValidationException;
import com.quimbayaeval.exception.ResourceNotFoundException;
import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.model.Submission;
import com.quimbayaeval.model.dto.SubmissionDetalleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de submissions con soporte para autocalificación y consultas enriquecidas.
 */
@Service
public class SubmissionService {

    @Autowired
    private SubmissionDao submissionDao;

    @Autowired
    private EvaluacionDao evaluacionDao;

    @Autowired
    private CalificacionService calificacionService;

    @Transactional
    public Submission crear(Submission s) {
        // ISSUE-01: la evaluación debe estar Activa y publicada
        Evaluacion eval = evaluacionDao.findById(s.getEvaluacionId())
            .orElseThrow(() -> new ResourceNotFoundException("Evaluacion", "id", s.getEvaluacionId()));

        if (!"Activa".equals(eval.getEstado()) || !Boolean.TRUE.equals(eval.getPublicada())) {
            throw new BusinessValidationException(
                "La evaluación no está disponible para presentar (estado: " + eval.getEstado() + ")");
        }

        // ISSUE-02: validar intentos permitidos
        int intentosUsados = submissionDao.countByEvaluacionAndEstudiante(
            s.getEvaluacionId(), s.getEstudianteId());
        int intentosPermitidos = eval.getIntentosPermitidos() != null ? eval.getIntentosPermitidos() : 1;
        if (intentosUsados >= intentosPermitidos) {
            throw new BusinessValidationException(
                "Se alcanzó el límite de intentos permitidos (" + intentosPermitidos + ")");
        }

        if ("Enviada".equalsIgnoreCase(s.getEstado()) && s.getFechaEnvio() == null) {
            s.setFechaEnvio(LocalDateTime.now());
        }

        Submission saved = submissionDao.save(s);

        if ("Enviada".equalsIgnoreCase(s.getEstado()) && saved.getId() != null) {
            calificacionService.autocalificarSubmission(saved.getId());
        }

        return saved;
    }

    public Optional<Submission> obtenerPorId(Integer id) {
        return submissionDao.findById(id);
    }

    public Optional<SubmissionDetalleDTO> obtenerDetallePorId(Integer id) {
        return submissionDao.findDetalleById(id);
    }

    public List<Submission> obtenerTodos() {
        return submissionDao.findAll();
    }

    public List<Submission> obtenerTodos(Map<String, Object> filters,
                                         Integer page,
                                         Integer size,
                                         String sort,
                                         String dir) {
        return submissionDao.findAll(filters, page, size, sort, dir);
    }

    public List<Submission> obtenerPorEvaluacion(Integer evaluacionId) {
        return submissionDao.findByEvaluacion(evaluacionId);
    }

    public List<SubmissionDetalleDTO> obtenerDetallesPorEvaluacion(Integer evaluacionId) {
        return submissionDao.findDetallesByEvaluacion(evaluacionId);
    }

    public List<Submission> obtenerPorEstudiante(Integer estudianteId) {
        return submissionDao.findByEstudiante(estudianteId);
    }

    public List<SubmissionDetalleDTO> obtenerDetallesPorEstudiante(Integer estudianteId) {
        return submissionDao.findDetallesByEstudiante(estudianteId);
    }

    @Transactional
    public void actualizar(Submission s) {
        if ("Enviada".equalsIgnoreCase(s.getEstado()) && s.getFechaEnvio() == null) {
            s.setFechaEnvio(LocalDateTime.now());
        }
        submissionDao.update(s);

        if ("Enviada".equalsIgnoreCase(s.getEstado()) && s.getId() != null) {
            calificacionService.autocalificarSubmission(s.getId());
        }
    }

    public void eliminar(Integer id) {
        submissionDao.deleteById(id);
    }
}
