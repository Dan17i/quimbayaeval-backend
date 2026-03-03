package com.quimbayaeval.controller;

import com.quimbayaeval.model.Submission;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/submissions")
@CrossOrigin(origins = "*")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Submission>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        List<Submission> resultado;
        if (page != null && size != null) {
            Map<String, Object> filters = new HashMap<>();
            resultado = submissionService.obtenerTodos(filters, page, size, sort, direction);
        } else {
            resultado = submissionService.obtenerTodos();
        }
        return ResponseEntity.ok(ApiResponse.success("Listado de submissions", resultado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Submission>> getById(@PathVariable Integer id) {
        Optional<Submission> opt = submissionService.obtenerPorId(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Submission encontrada", opt.get()));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Submission no encontrada"));
    }

    @GetMapping("/evaluacion/{evaluacionId}")
    public ResponseEntity<ApiResponse<List<Submission>>> getByEvaluacion(@PathVariable Integer evaluacionId) {
        return ResponseEntity.ok(ApiResponse.success("Submissions por evaluación", submissionService.obtenerPorEvaluacion(evaluacionId)));
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<ApiResponse<List<Submission>>> getByEstudiante(@PathVariable Integer estudianteId) {
        return ResponseEntity.ok(ApiResponse.success("Submissions por estudiante", submissionService.obtenerPorEstudiante(estudianteId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Submission>> create(@RequestBody Submission s) {
        Submission saved = submissionService.crear(s);
        return ResponseEntity.ok(ApiResponse.success("Submission creada", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Submission>> update(@PathVariable Integer id, @RequestBody Submission s) {
        s.setId(id);
        submissionService.actualizar(s);
        return ResponseEntity.ok(ApiResponse.success("Submission actualizada", s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        submissionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Submission eliminada", null));
    }
}
