package com.quimbayaeval.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.CalificacionDao;
import com.quimbayaeval.dao.PreguntaDao;
import com.quimbayaeval.dao.ResultadoDao;
import com.quimbayaeval.dao.SubmissionDao;
import com.quimbayaeval.model.Calificacion;
import com.quimbayaeval.model.Pregunta;
import com.quimbayaeval.model.Submission;
import com.quimbayaeval.model.dto.request.CalificacionBatchRequestDTO;
import com.quimbayaeval.model.dto.request.CalificacionItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de calificaciones con soporte para autocalificación de preguntas objetivas
 * y calificación por lote transaccional.
 */
@Service
public class CalificacionService {

    @Autowired
    private CalificacionDao calificacionDao;

    @Autowired
    private ResultadoDao resultadoDao;

    @Autowired
    private SubmissionDao submissionDao;

    @Autowired
    private PreguntaDao preguntaDao;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public Calificacion crear(Calificacion c) {
        Calificacion saved = calificacionDao.save(c);
        resultadoDao.upsertFromSubmission(c.getSubmissionId());
        return saved;
    }

    public Optional<Calificacion> obtenerPorId(Integer id) {
        return calificacionDao.findById(id);
    }

    public List<Calificacion> obtenerTodos() {
        return calificacionDao.findAll();
    }

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

    @Transactional
    public void actualizar(Calificacion c) {
        calificacionDao.update(c);
        if (c.getSubmissionId() != null) {
            resultadoDao.upsertFromSubmission(c.getSubmissionId());
        }
    }

    public void eliminar(Integer id) {
        calificacionDao.deleteById(id);
    }

    /**
     * Autocalifica las preguntas objetivas (selección múltiple y verdadero/falso)
     * comparando con la respuesta correcta almacenada, generando las calificaciones y
     * consolidando el resultado inicial.
     */
    @Transactional
    public void autocalificarSubmission(Integer submissionId) {
        Optional<Submission> subOpt = submissionDao.findById(submissionId);
        if (subOpt.isEmpty()) return;
        Submission sub = subOpt.get();

        List<Pregunta> preguntas = preguntaDao.findByEvaluacion(sub.getEvaluacionId());
        if (preguntas.isEmpty()) return;

        JsonNode respuestasTree = null;
        if (sub.getRespuestasJson() != null && !sub.getRespuestasJson().isBlank()) {
            try {
                respuestasTree = objectMapper.readTree(sub.getRespuestasJson());
            } catch (Exception ignored) {}
        }

        boolean todasObjetivasYCalificadas = true;

        for (Pregunta p : preguntas) {
            String tipo = p.getTipo() != null ? p.getTipo().toLowerCase() : "";
            boolean esObjetiva = "seleccion_multiple".equals(tipo) || "verdadero_falso".equals(tipo);

            if (esObjetiva) {
                String respEstudiante = "";
                if (respuestasTree != null && respuestasTree.has(String.valueOf(p.getId()))) {
                    JsonNode node = respuestasTree.get(String.valueOf(p.getId()));
                    respEstudiante = node.isTextual() ? node.asText() : node.toString();
                }

                boolean esCorrecta = verificarRespuestaCorrecta(p, respEstudiante);
                double maxPuntos = p.getPuntuacion() != null ? p.getPuntuacion() : 1.0;
                double puntosObtenidos = esCorrecta ? maxPuntos : 0.0;
                String feedback = esCorrecta
                    ? "Respuesta correcta (Autocalificado)"
                    : (respEstudiante.isBlank() ? "No respondida (Autocalificado)" : "Respuesta incorrecta (Autocalificado)");

                Calificacion c = new Calificacion();
                c.setSubmissionId(submissionId);
                c.setPreguntaId(p.getId());
                c.setPuntuacionObtenida(puntosObtenidos);
                c.setPuntuacionMaxima(maxPuntos);
                c.setRetroalimentacion(feedback);
                c.setCalificadoPorId(null); // Sistema
                calificacionDao.upsert(c);
            } else {
                todasObjetivasYCalificadas = false;
            }
        }

        if (todasObjetivasYCalificadas) {
            sub.setEstado("Calificada");
            submissionDao.update(sub);
        }

        resultadoDao.upsertFromSubmission(submissionId);
    }

    /**
     * Califica todas las preguntas de una entrega de forma atómica y actualiza las observaciones generales.
     */
    @Transactional
    public void calificarBatch(CalificacionBatchRequestDTO request, Integer calificadoPorId) {
        if (request == null || request.getSubmissionId() == null) {
            throw new IllegalArgumentException("La petición de calificación por lote debe contener un submissionId válido");
        }

        Integer submissionId = request.getSubmissionId();

        if (request.getCalificaciones() != null) {
            for (CalificacionItemDTO item : request.getCalificaciones()) {
                if (item.getPreguntaId() == null) continue;
                Calificacion c = new Calificacion();
                c.setSubmissionId(submissionId);
                c.setPreguntaId(item.getPreguntaId());
                c.setPuntuacionObtenida(item.getPuntuacionObtenida() != null ? item.getPuntuacionObtenida() : 0.0);
                c.setPuntuacionMaxima(item.getPuntuacionMaxima());
                c.setRetroalimentacion(item.getRetroalimentacion());
                c.setCalificadoPorId(calificadoPorId);
                calificacionDao.upsert(c);
            }
        }

        Optional<Submission> subOpt = submissionDao.findById(submissionId);
        if (subOpt.isPresent()) {
            Submission sub = subOpt.get();
            sub.setEstado("Calificada");
            submissionDao.update(sub);
        }

        resultadoDao.upsertFromSubmission(submissionId);

        if (request.getObservacionesGenerales() != null && !request.getObservacionesGenerales().isBlank()) {
            resultadoDao.updateObservaciones(submissionId, request.getObservacionesGenerales());
        }
    }

    private boolean verificarRespuestaCorrecta(Pregunta p, String respEstudiante) {
        if (respEstudiante == null || respEstudiante.trim().isEmpty()) {
            return false;
        }
        String respNorm = normalizarTexto(respEstudiante);

        // 1. Contrastar con respuesta_correcta_json
        if (p.getRespuestaCorrectaJson() != null && !p.getRespuestaCorrectaJson().isBlank()) {
            try {
                JsonNode rcNode = objectMapper.readTree(p.getRespuestaCorrectaJson());
                if (rcNode.has("respuesta")) {
                    String correcta = rcNode.get("respuesta").asText();
                    if (normalizarTexto(correcta).equals(respNorm)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 2. Contrastar con opciones_json
        if (p.getOpcionesJson() != null && !p.getOpcionesJson().isBlank()) {
            try {
                JsonNode opciones = objectMapper.readTree(p.getOpcionesJson());
                if (opciones.isArray()) {
                    for (JsonNode op : opciones) {
                        boolean esOpcionCorrecta = op.has("correcta") && op.get("correcta").asBoolean();
                        if (esOpcionCorrecta) {
                            if (op.has("texto") && normalizarTexto(op.get("texto").asText()).equals(respNorm)) {
                                return true;
                            }
                            if (op.has("id") && String.valueOf(op.get("id").asInt()).equals(respEstudiante.trim())) {
                                return true;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        return false;
    }

    private String normalizarTexto(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }
}
