package com.quimbayaeval.service;

import com.quimbayaeval.dao.SubmissionDao;
import com.quimbayaeval.model.Submission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de submissions
 */
@Service
public class SubmissionService {

    @Autowired
    private SubmissionDao submissionDao;

    public Submission crear(Submission s) {
        return submissionDao.save(s);
    }

    public Optional<Submission> obtenerPorId(Integer id) {
        return submissionDao.findById(id);
    }

    public List<Submission> obtenerTodos() {
        return submissionDao.findAll();
    }

    /**
     * Obtiene submissions con criterios dinámicos
     */
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

    public List<Submission> obtenerPorEstudiante(Integer estudianteId) {
        return submissionDao.findByEstudiante(estudianteId);
    }

    public void actualizar(Submission s) {
        submissionDao.update(s);
    }

    public void eliminar(Integer id) {
        submissionDao.deleteById(id);
    }
}
