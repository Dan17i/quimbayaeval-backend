package com.quimbayaeval.service;

import com.quimbayaeval.dao.PreguntaDao;
import com.quimbayaeval.model.Pregunta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de preguntas
 */
@Service
public class PreguntaService {

    @Autowired
    private PreguntaDao preguntaDao;

    public Pregunta crear(Pregunta p) {
        return preguntaDao.save(p);
    }

    public Optional<Pregunta> obtenerPorId(Integer id) {
        return preguntaDao.findById(id);
    }

    public List<Pregunta> obtenerTodos() {
        return preguntaDao.findAll();
    }

    /**
     * Obtiene preguntas con parámetros de consulta (filtros/orden/paginación)
     */
    public List<Pregunta> obtenerTodos(Map<String, Object> filters,
                                      Integer page,
                                      Integer size,
                                      String sort,
                                      String dir) {
        return preguntaDao.findAll(filters, page, size, sort, dir);
    }

    public List<Pregunta> obtenerPorEvaluacion(Integer evaluacionId) {
        return preguntaDao.findByEvaluacion(evaluacionId);
    }

    public void actualizar(Pregunta p) {
        preguntaDao.update(p);
    }

    public void eliminar(Integer id) {
        preguntaDao.deleteById(id);
    }
}
