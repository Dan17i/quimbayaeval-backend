package com.quimbayaeval.service;

import com.quimbayaeval.dao.ResultadoDao;
import com.quimbayaeval.model.Resultado;
import com.quimbayaeval.model.dto.ResumenCursoDTO;
import com.quimbayaeval.model.dto.ResultadoDetalleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResultadoServiceTest {

    @Mock
    private ResultadoDao resultadoDao;

    @InjectMocks
    private ResultadoService resultadoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerPorEstudiante_delegatesDao() {
        Resultado r = new Resultado();
        r.setEstudianteId(4);
        r.setPorcentaje(new BigDecimal("85.0"));
        when(resultadoDao.findByEstudiante(4)).thenReturn(Arrays.asList(r));

        List<Resultado> result = resultadoService.obtenerPorEstudiante(4);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getEstudianteId());
        verify(resultadoDao).findByEstudiante(4);
    }

    @Test
    void obtenerPorEvaluacion_delegatesDao() {
        Resultado r = new Resultado();
        r.setEvaluacionId(2);
        when(resultadoDao.findByEvaluacion(2)).thenReturn(Arrays.asList(r));

        List<Resultado> result = resultadoService.obtenerPorEvaluacion(2);

        assertEquals(1, result.size());
        verify(resultadoDao).findByEvaluacion(2);
    }

    @Test
    void obtenerPorSubmission_returnsEmpty_whenNotFound() {
        when(resultadoDao.findBySubmission(99)).thenReturn(Optional.empty());

        Optional<Resultado> result = resultadoService.obtenerPorSubmission(99);

        assertFalse(result.isPresent());
        verify(resultadoDao).findBySubmission(99);
    }

    @Test
    void obtenerPorSubmission_returnsResultado_whenFound() {
        Resultado r = new Resultado();
        r.setSubmissionId(2);
        r.setPuntuacionTotal(new BigDecimal("4.0"));
        when(resultadoDao.findBySubmission(2)).thenReturn(Optional.of(r));

        Optional<Resultado> result = resultadoService.obtenerPorSubmission(2);

        assertTrue(result.isPresent());
        assertEquals(new BigDecimal("4.0"), result.get().getPuntuacionTotal());
    }

    @Test
    void obtenerDetallesPorCurso_delegatesDao() {
        ResultadoDetalleDTO dto = new ResultadoDetalleDTO();
        dto.setEstudianteNombre("Juan");
        dto.setEvaluacionNombre("Parcial 1");
        dto.setNotaEscala(new BigDecimal("4.20"));
        when(resultadoDao.findDetallesByCurso(10)).thenReturn(Arrays.asList(dto));

        List<ResultadoDetalleDTO> result = resultadoService.obtenerDetallesPorCurso(10);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getEstudianteNombre());
        assertEquals(new BigDecimal("4.20"), result.get(0).getNotaEscala());
        verify(resultadoDao).findDetallesByCurso(10);
    }

    @Test
    void obtenerDetallesPorCurso_returnsEmpty_whenNoCurso() {
        when(resultadoDao.findDetallesByCurso(999)).thenReturn(Collections.emptyList());

        List<ResultadoDetalleDTO> result = resultadoService.obtenerDetallesPorCurso(999);

        assertTrue(result.isEmpty());
        verify(resultadoDao).findDetallesByCurso(999);
    }

    @Test
    void obtenerResumenPorCurso_delegatesDao() {
        ResumenCursoDTO dto = new ResumenCursoDTO();
        dto.setEvaluacionNombre("Parcial 1");
        dto.setPromedioGrupo(new BigDecimal("75.00"));
        dto.setPromedioEscala(new BigDecimal("4.00"));
        dto.setTotalEstudiantes(30);
        dto.setAprobados(25);
        dto.setReprobados(5);
        when(resultadoDao.findResumenByCurso(10)).thenReturn(Arrays.asList(dto));

        List<ResumenCursoDTO> result = resultadoService.obtenerResumenPorCurso(10);

        assertEquals(1, result.size());
        assertEquals("Parcial 1", result.get(0).getEvaluacionNombre());
        assertEquals(new BigDecimal("75.00"), result.get(0).getPromedioGrupo());
        assertEquals(25, result.get(0).getAprobados());
        verify(resultadoDao).findResumenByCurso(10);
    }

    @Test
    void obtenerResumenPorCurso_returnsEmpty_whenNoCurso() {
        when(resultadoDao.findResumenByCurso(999)).thenReturn(Collections.emptyList());

        List<ResumenCursoDTO> result = resultadoService.obtenerResumenPorCurso(999);

        assertTrue(result.isEmpty());
        verify(resultadoDao).findResumenByCurso(999);
    }
}
