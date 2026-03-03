package com.quimbayaeval.service;

import com.quimbayaeval.dao.CalificacionDao;
import com.quimbayaeval.model.Calificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de calificaciones
 */
@Service
public class CalificacionService {

    @Autowired
    private CalificacionDao calificacionDao;

    public Calificacion crear(Calificacion c) {
        return calificacionDao.save(c);
    }

    public Optional<Calificacion> obtenerPorId(Integer id) {
        return calificacionDao.findById(id);
    }

    public List<Calificacion> obtenerTodos() {
        return calificacionDao.findAll();
    }

    /**
     * Obtiene calificaciones con parámetros dinámicos
     */
    public List<Calificacion> obtenerTodos(Map<String, Object> filters,
                                           Integer page,
                                           Integer size,
                                           String sort,
                                           String dir) {
        return calificacionDao.findAll(filters, page, size, sort, dir);
    }

    public List<Calificacion> obtenerPorSubmission(Integer submissionId) {
        return calificacionDao.findBySubmission(submissionId);
    }

    public void actualizar(Calificacion c) {
        calificacionDao.update(c);
    }

    public void eliminar(Integer id) {
        calificacionDao.deleteById(id);
    }
}
