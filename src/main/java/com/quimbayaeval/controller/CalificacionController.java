package com.quimbayaeval.controller;

import com.quimbayaeval.model.Calificacion;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.security.JwtUserDetails;
import com.quimbayaeval.service.CalificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calificaciones")
@CrossOrigin(origins = "*")
public class CalificacionController {

    @Autowired
    private CalificacionService calificacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Calificacion>>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        List<Calificacion> resultado;
        if (page != null && size != null) {
            Map<String, Object> filters = new HashMap<>();
            resultado = calificacionService.obtenerTodos(filters, page, size, sort, direction);
        } else {
            resultado = calificacionService.obtenerTodos();
        }
        return ResponseEntity.ok(ApiResponse.success("Listado de calificaciones", resultado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Calificacion>> getById(@PathVariable Integer id) {
        Optional<Calificacion> opt = calificacionService.obtenerPorId(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("Calificación encontrada", opt.get()));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Calificación no encontrada"));
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<ApiResponse<List<Calificacion>>> getBySubmission(@PathVariable Integer submissionId) {
        return ResponseEntity.ok(ApiResponse.success("Calificaciones por submission", calificacionService.obtenerPorSubmission(submissionId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Calificacion>> create(
            @RequestBody Calificacion c,
            Authentication authentication) {
        // calificadoPorId siempre viene del JWT, ignorar lo que envíe el frontend
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        c.setCalificadoPorId(userDetails.getUserId());
        Calificacion saved = calificacionService.crear(c);
        return ResponseEntity.ok(ApiResponse.success("Calificación creada", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Calificacion>> update(@PathVariable Integer id, @RequestBody Calificacion c) {
        c.setId(id);
        calificacionService.actualizar(c);
        return ResponseEntity.ok(ApiResponse.success("Calificación actualizada", c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Integer id) {
        calificacionService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.success("Calificación eliminada", null));
    }
}
