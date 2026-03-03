package com.quimbayaeval.controller;

import com.quimbayaeval.model.Pregunta;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/preguntas")
@CrossOrigin(origins = "*")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Pregunta>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        List<Pregunta> resultado;
        if (page != null && size != null) {
            Map<String, Object> filters = new HashMap<>();
            resultado = preguntaService.obtenerTodos(filters, page, size, sort, direction);
        } else {
            resultado = preguntaService.obtenerTodos();
        }
        return ResponseEntity.ok(ApiResponse.success("Listado de preguntas", resultado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Pregunta>> getById(@PathVariable Integer id) {
        Optional<Pregunta> opt = preguntaService.obtenerPorId(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Pregunta encontrada", opt.get()));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Pregunta no encontrada"));
    }

    @GetMapping("/evaluacion/{evaluacionId}")
    public ResponseEntity<ApiResponse<List<Pregunta>>> getByEvaluacion(@PathVariable Integer evaluacionId) {
        return ResponseEntity.ok(ApiResponse.success("Preguntas por evaluación", preguntaService.obtenerPorEvaluacion(evaluacionId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Pregunta>> create(@RequestBody Pregunta p) {
        Pregunta saved = preguntaService.crear(p);
        return ResponseEntity.ok(ApiResponse.success("Pregunta creada", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Pregunta>> update(@PathVariable Integer id, @RequestBody Pregunta p) {
        p.setId(id);
        preguntaService.actualizar(p);
        return ResponseEntity.ok(ApiResponse.success("Pregunta actualizada", p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        preguntaService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Pregunta eliminada", null));
    }
}
