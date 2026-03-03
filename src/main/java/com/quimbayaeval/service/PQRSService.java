package com.quimbayaeval.service;

import com.quimbayaeval.dao.PQRSDao;
import com.quimbayaeval.model.PQRS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para PQRS
 */
@Service
public class PQRSService {

    @Autowired
    private PQRSDao pqrsDao;

    /**
     * Crea un nuevo PQRS
     */
    public PQRS crear(PQRS pqrs) {
        return pqrsDao.save(pqrs);
    }

    /**
     * Obtiene PQRS por ID
     */
    public Optional<PQRS> obtenerPorId(Integer id) {
        return pqrsDao.findById(id);
    }

    /**
     * Obtiene todos los PQRS
     */
    public List<PQRS> obtenerTodos() {
        return pqrsDao.findAll();
    }

    /**
     * Lista tickets con filtros, ordenación y paginación
     */
    public List<PQRS> obtenerTodos(Map<String, Object> filters,
                                  Integer page,
                                  Integer size,
                                  String sort,
                                  String dir) {
        return pqrsDao.findAll(filters, page, size, sort, dir);
    }

    /**
     * Obtiene PQRS del usuario
     */
    public List<PQRS> obtenerPorUsuario(Integer usuarioId) {
        return pqrsDao.findByUsuario(usuarioId);
    }

    /**
     * Obtiene PQRS por estado
     */
    public List<PQRS> obtenerPorEstado(String estado) {
        return pqrsDao.findByEstado(estado);
    }

    /**
     * Obtiene PQRS por tipo
     */
    public List<PQRS> obtenerPorTipo(String tipo) {
        return pqrsDao.findByTipo(tipo);
    }

    /**
     * Responde un PQRS
     */
    public void responder(Integer id, String respuesta, Integer respondidoPorId) {
        Optional<PQRS> pqrsOpt = pqrsDao.findById(id);
        if (pqrsOpt.isPresent()) {
            PQRS pqrs = pqrsOpt.get();
            pqrs.setRespuesta(respuesta);
            pqrs.setRespondidoPorId(respondidoPorId);
            pqrs.setEstado("Resuelto");
            pqrsDao.update(pqrs);
        }
    }

    /**
     * Actualiza un PQRS
     */
    public void actualizar(PQRS pqrs) {
        pqrsDao.update(pqrs);
    }

    /**
     * Elimina un PQRS
     */
    public void eliminar(Integer id) {
        pqrsDao.deleteById(id);
    }
}
