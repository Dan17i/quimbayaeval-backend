package com.quimbayaeval.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quimbayaeval.dao.CalificacionDao;
import com.quimbayaeval.dao.EvaluacionDao;
import com.quimbayaeval.dao.PreguntaDao;
import com.quimbayaeval.dao.ResultadoDao;
import com.quimbayaeval.dao.SubmissionDao;
import com.quimbayaeval.model.Calificacion;
import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.model.Pregunta;
import com.quimbayaeval.model.Submission;
import com.quimbayaeval.model.dto.request.CalificacionBatchRequestDTO;
import com.quimbayaeval.model.dto.request.CalificacionItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalificacionServiceTest {

    @Mock
    private CalificacionDao calificacionDao;

    @Mock
    private SubmissionDao submissionDao;

    @Mock
    private PreguntaDao preguntaDao;

    @Mock
    private EvaluacionDao evaluacionDao;

    @Mock
    private ResultadoDao resultadoDao;

    @InjectMocks
    private CalificacionService calificacionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void autocalificarSubmission_calificaPreguntasObjetivasCorrectamente() {
        Integer submissionId = 10;
        Submission sub = new Submission();
        sub.setId(submissionId);
        sub.setEvaluacionId(1);
        sub.setEstudianteId(5);
        sub.setRespuestasJson("{\"1\":\"B\",\"2\":\"verdadero\"}");

        Pregunta p1 = new Pregunta(1, "Pregunta 1", "seleccion_multiple");
        p1.setId(1);
        p1.setPuntuacion(2.5);
        p1.setRespuestaCorrectaJson("{\"respuesta\":\"B\"}");

        Pregunta p2 = new Pregunta(1, "¿Pregunta 2?", "verdadero_falso");
        p2.setId(2);
        p2.setPuntuacion(2.5);
        p2.setRespuestaCorrectaJson("{\"respuesta\":\"verdadero\"}");

        when(submissionDao.findById(submissionId)).thenReturn(Optional.of(sub));
        when(preguntaDao.findByEvaluacion(1)).thenReturn(List.of(p1, p2));

        calificacionService.autocalificarSubmission(submissionId);

        // Se deben registrar calificaciones para ambas preguntas
        verify(calificacionDao, times(2)).upsert(any(Calificacion.class));
        verify(resultadoDao, times(1)).upsertFromSubmission(submissionId);
    }

    @Test
    void calificarBatch_procesaLoteYActualizaResultado() {
        Integer submissionId = 20;
        Submission sub = new Submission();
        sub.setId(submissionId);
        sub.setEvaluacionId(2);
        sub.setEstudianteId(8);

        CalificacionBatchRequestDTO request = new CalificacionBatchRequestDTO();
        request.setSubmissionId(submissionId);
        request.setObservacionesGenerales("Buen trabajo");

        CalificacionItemDTO item = new CalificacionItemDTO();
        item.setPreguntaId(101);
        item.setPuntuacionObtenida(4.5);
        item.setPuntuacionMaxima(5.0);
        item.setRetroalimentacion("Argumentación sólida");
        request.setCalificaciones(List.of(item));

        when(submissionDao.findById(submissionId)).thenReturn(Optional.of(sub));

        calificacionService.calificarBatch(request, 99);

        verify(calificacionDao, times(1)).upsert(any(Calificacion.class));
        verify(resultadoDao, times(1)).upsertFromSubmission(submissionId);
        verify(resultadoDao, times(1)).updateObservaciones(submissionId, "Buen trabajo");
    }
}
