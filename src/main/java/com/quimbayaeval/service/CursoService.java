package com.quimbayaeval.service;

import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.model.Curso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para Cursos
 */
@Service
public class CursoService {

    @Autowired
    private CursoDao cursoDao;

    /**
     * Crea un nuevo curso
     */
    public Curso crear(Curso curso) {
        return cursoDao.save(curso);
    }

    /**
     * Obtiene curso por ID
     */
    public Optional<Curso> obtenerPorId(Integer id) {
        return cursoDao.findById(id);
    }

    /**
     * Obtiene curso por código
     */
    public Optional<Curso> obtenerPorCodigo(String codigo) {
        return cursoDao.findByCodigo(codigo);
    }

    /**
     * Obtiene todos los cursos
     */
    public List<Curso> obtenerTodos() {
        return cursoDao.findAll();
    }

    /**
     * Obtiene cursos aplicando filtros, orden y paginación
     */
    public List<Curso> obtenerTodos(Map<String, Object> filters,
                                   Integer page,
                                   Integer size,
                                   String sort,
                                   String dir) {
        return cursoDao.findAll(filters, page, size, sort, dir);
    }

    /**
     * Obtiene cursos de un profesor
     */
    public List<Curso> obtenerDelProfesor(Integer profesorId) {
        return cursoDao.findByProfesor(profesorId);
    }

    /**
     * Actualiza un curso
     */
    public void actualizar(Curso curso) {
        cursoDao.update(curso);
    }

    /**
     * Elimina un curso
     */
    public void eliminar(Integer id) {
        cursoDao.deleteById(id);
    }
}
