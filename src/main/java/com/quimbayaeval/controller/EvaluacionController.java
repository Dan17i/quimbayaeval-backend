package com.quimbayaeval.controller;

import com.quimbayaeval.dao.JdbcQueryBuilder;
import com.quimbayaeval.model.Evaluacion;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.service.EvaluacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para Evaluaciones con soporte para filtrado avanzado, caché y rate limiting
 */
@RestController
@RequestMapping("/api/evaluaciones")
@CrossOrigin(origins = "*")
public class EvaluacionController {

    @Autowired
    private EvaluacionService evaluacionService;

    /**
     * Obtiene todas las evaluaciones con filtros avanzados
     * GET /api/evaluaciones
     * 
     * Parámetros opcionales:
     *   ?page=0&size=10&sort=nombre&direction=ASC
     *   ?estado=activa&tipo=examen&curso_id=1
     *   ?nombre=parcial   (búsqueda por LIKE)
     *   ?publicada=true
     */
    @GetMapping
    @Cacheable(value = "evaluacionesByCurso", key = "'all_' + #page + '_' + #size + '_' + #sort")
    public ResponseEntity<ApiResponse<List<Evaluacion>>> obtenerTodas(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer cursoId,
            @RequestParam(required = false) Integer profesorId,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Boolean publicada) {
        try {
            List<Evaluacion> evaluaciones;

            // Filtro por profesorId (para reportes de admin/coordinador)
            if (profesorId != null && page == null) {
                evaluaciones = evaluacionService.obtenerDelProfesor(profesorId);
                return ResponseEntity.ok(ApiResponse.success(evaluaciones));
            }
            
            if (page != null && size != null) {
                // Construir criterios de filtro avanzados
                List<JdbcQueryBuilder.FilterCriteria> filters = new ArrayList<>();
                
                if (estado != null && !estado.isEmpty()) {
                    filters.add(new JdbcQueryBuilder.FilterCriteria("estado", 
                        JdbcQueryBuilder.FilterOperator.EQUALS, estado));
                }
                if (tipo != null && !tipo.isEmpty()) {
                    filters.add(new JdbcQueryBuilder.FilterCriteria("tipo", 
                        JdbcQueryBuilder.FilterOperator.EQUALS, tipo));
                }
                if (cursoId != null) {
                    filters.add(new JdbcQueryBuilder.FilterCriteria("curso_id", 
                        JdbcQueryBuilder.FilterOperator.EQUALS, cursoId));
                }
                if (nombre != null && !nombre.isEmpty()) {
                    filters.add(new JdbcQueryBuilder.FilterCriteria("nombre", 
                        JdbcQueryBuilder.FilterOperator.ILIKE, nombre));
                }
                if (publicada != null) {
                    filters.add(new JdbcQueryBuilder.FilterCriteria("publicada", 
                        JdbcQueryBuilder.FilterOperator.EQUALS, publicada));
                }
                
                evaluaciones = evaluacionService.obtenerConFiltrosAvanzados(
                    filters, page, size, sort != null ? sort : "id", direction);
            } else {
                // Sin paginación: retornar todos
                evaluaciones = evaluacionService.obtenerTodas();
            }
            return ResponseEntity.ok(ApiResponse.success(evaluaciones));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo evaluaciones: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene una evaluación por ID
     * GET /api/evaluaciones/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Evaluacion>> obtenerPorId(@PathVariable Integer id) {
        try {
            Optional<Evaluacion> evaluacion = evaluacionService.obtenerPorId(id);
            if (evaluacion.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(evaluacion.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Evaluación no encontrada")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo evaluación: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene evaluaciones de un curso con caché
     * GET /api/evaluaciones/curso/{cursoId}
     */
    @GetMapping("/curso/{cursoId}")
    @Cacheable(value = "evaluacionesByCurso", key = "#cursoId")
    public ResponseEntity<ApiResponse<List<Evaluacion>>> obtenerPorCurso(@PathVariable Integer cursoId) {
        try {
            List<Evaluacion> evaluaciones = evaluacionService.obtenerPorCurso(cursoId);
            return ResponseEntity.ok(ApiResponse.success(evaluaciones));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo evaluaciones: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene evaluaciones activas con caché
     * GET /api/evaluaciones/estado/activas
     */
    @GetMapping("/estado/activas")
    @Cacheable(value = "evaluacionesByEstado", key = "'activas'")
    public ResponseEntity<ApiResponse<List<Evaluacion>>> obtenerActivas() {
        try {
            List<Evaluacion> evaluaciones = evaluacionService.obtenerActivas();
            return ResponseEntity.ok(ApiResponse.success(evaluaciones));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo evaluaciones: " + e.getMessage())
            );
        }
    }

    /**
     * Crea una nueva evaluación (invalida caché)
     * POST /api/evaluaciones
     */
    @PostMapping
    @CacheEvict(value = {"evaluacionesByCurso", "evaluacionesByEstado"}, allEntries = true)
    public ResponseEntity<ApiResponse<Evaluacion>> crear(@RequestBody Evaluacion evaluacion) {
        try {
            Evaluacion nueva = evaluacionService.crear(evaluacion);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Evaluación creada exitosamente", nueva)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error creando evaluación: " + e.getMessage())
            );
        }
    }

    /**
     * Actualiza una evaluación (invalida caché)
     * PUT /api/evaluaciones/{id}
     */
    @PutMapping("/{id}")
    @CacheEvict(value = {"evaluacionesByCurso", "evaluacionesByEstado"}, allEntries = true)
    public ResponseEntity<ApiResponse<String>> actualizar(@PathVariable Integer id, @RequestBody Evaluacion evaluacion) {
        try {
            evaluacion.setId(id);
            evaluacionService.actualizar(evaluacion);
            return ResponseEntity.ok(ApiResponse.success("Evaluación actualizada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error actualizando evaluación: " + e.getMessage())
            );
        }
    }

    /**
     * Publica una evaluación (invalida caché)
     * POST /api/evaluaciones/{id}/publicar
     */
    @PostMapping("/{id}/publicar")
    @CacheEvict(value = {"evaluacionesByCurso", "evaluacionesByEstado"}, allEntries = true)
    public ResponseEntity<ApiResponse<String>> publicar(@PathVariable Integer id) {
        try {
            evaluacionService.publicar(id);
            return ResponseEntity.ok(ApiResponse.success("Evaluación publicada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error publicando evaluación: " + e.getMessage())
            );
        }
    }

    /**
     * Elimina una evaluación (invalida caché)
     * DELETE /api/evaluaciones/{id}
     */
    @DeleteMapping("/{id}")
    @CacheEvict(value = {"evaluacionesByCurso", "evaluacionesByEstado"}, allEntries = true)
    public ResponseEntity<ApiResponse<String>> eliminar(@PathVariable Integer id) {
        try {
            evaluacionService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.success("Evaluación eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error eliminando evaluación: " + e.getMessage())
            );
        }
    }

    /**
     * Envía respuesta de evaluación (estudiante completa evaluación)
     * POST /api/evaluaciones/{id}/submit
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<String>> submitEvaluacion(@PathVariable Integer id, @RequestBody Object respuestas) {
        try {
            // TODO: Implementar lógica de submit de evaluación
            // Esto guardará las respuestas del estudiante en la tabla de submissions
            return ResponseEntity.ok(ApiResponse.success("Evaluación enviada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error enviando evaluación: " + e.getMessage())
            );
        }
    }
}
