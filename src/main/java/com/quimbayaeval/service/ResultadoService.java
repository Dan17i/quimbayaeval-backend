package com.quimbayaeval.service;

import com.quimbayaeval.dao.ResultadoDao;
import com.quimbayaeval.model.Resultado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResultadoService {

    @Autowired
    private ResultadoDao resultadoDao;

    public List<Resultado> obtenerPorEstudiante(Integer estudianteId) {
        return resultadoDao.findByEstudiante(estudianteId);
    }

    public List<Resultado> obtenerPorEvaluacion(Integer evaluacionId) {
        return resultadoDao.findByEvaluacion(evaluacionId);
    }

    public Optional<Resultado> obtenerPorSubmission(Integer submissionId) {
        return resultadoDao.findBySubmission(submissionId);
    }
}
