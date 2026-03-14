package com.quimbayaeval.controller;

import com.quimbayaeval.model.Resultado;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.model.dto.ResumenCursoDTO;
import com.quimbayaeval.model.dto.ResultadoDetalleDTO;
import com.quimbayaeval.security.JwtUserDetails;
import com.quimbayaeval.service.ResultadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/resultados")
@CrossOrigin(origins = "*")
public class ResultadoController {

    @Autowired
    private ResultadoService resultadoService;

    // GET /api/resultados/mis-resultados — resultados del estudiante autenticado
    @GetMapping("/mis-resultados")
    public ResponseEntity<ApiResponse<List<Resultado>>> getMisResultados(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        List<Resultado> resultados = resultadoService.obtenerPorEstudiante(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Resultados del estudiante", resultados));
    }

    // GET /api/resultados/evaluacion/{id} — resultados de una evaluación (maestro/coordinador)
    @GetMapping("/evaluacion/{evaluacionId}")
    public ResponseEntity<ApiResponse<List<Resultado>>> getByEvaluacion(
            @PathVariable Integer evaluacionId) {
        List<Resultado> resultados = resultadoService.obtenerPorEvaluacion(evaluacionId);
        return ResponseEntity.ok(ApiResponse.success("Resultados de la evaluación", resultados));
    }

    // GET /api/resultados/submission/{id} — resultado de una submission específica
    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<ApiResponse<Resultado>> getBySubmission(
            @PathVariable Integer submissionId) {
        Optional<Resultado> resultado = resultadoService.obtenerPorSubmission(submissionId);
        return resultado
            .map(r -> ResponseEntity.ok(ApiResponse.success("Resultado encontrado", r)))
            .orElse(ResponseEntity.status(404).body(ApiResponse.error("Resultado no encontrado")));
    }

    // GET /api/resultados/curso/{cursoId} — detalle de notas por curso (docente)
    @GetMapping("/curso/{cursoId}")
    public ResponseEntity<ApiResponse<List<ResultadoDetalleDTO>>> getByCurso(
            @PathVariable Integer cursoId) {
        List<ResultadoDetalleDTO> detalles = resultadoService.obtenerDetallesPorCurso(cursoId);
        return ResponseEntity.ok(ApiResponse.success("Resultados del curso", detalles));
    }

    // GET /api/resultados/curso/{cursoId}/resumen — promedio grupal por evaluación (coordinador)
    @GetMapping("/curso/{cursoId}/resumen")
    public ResponseEntity<ApiResponse<List<ResumenCursoDTO>>> getResumenByCurso(
            @PathVariable Integer cursoId) {
        List<ResumenCursoDTO> resumen = resultadoService.obtenerResumenPorCurso(cursoId);
        return ResponseEntity.ok(ApiResponse.success("Resumen del curso", resumen));
    }
}
