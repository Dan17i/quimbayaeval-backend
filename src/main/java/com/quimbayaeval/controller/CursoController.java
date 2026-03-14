package com.quimbayaeval.controller;

import com.quimbayaeval.dao.InscripcionDao;
import com.quimbayaeval.model.Curso;
import com.quimbayaeval.model.User;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para Cursos
 */
@RestController
@RequestMapping("/api/cursos")
@CrossOrigin(origins = "*")
public class CursoController {

    @Autowired
    private CursoService cursoService;

    @Autowired
    private InscripcionDao inscripcionDao;

    /**
     * Obtiene todos los cursos
     * GET /api/cursos
     * Parámetros opcionales:
     *   ?page=0&size=10&sort=nombre&direction=ASC
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Curso>>> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        try {
            List<Curso> cursos;
            if (page != null && size != null) {
                Map<String, Object> filters = new HashMap<>();
                cursos = cursoService.obtenerTodos(filters, page, size, sort, direction);
            } else {
                cursos = cursoService.obtenerTodos();
            }
            return ResponseEntity.ok(ApiResponse.success(cursos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo cursos: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene un curso por ID
     * GET /api/cursos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Curso>> obtenerPorId(@PathVariable Integer id) {
        try {
            Optional<Curso> curso = cursoService.obtenerPorId(id);
            if (curso.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(curso.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("Curso no encontrado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo curso: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene cursos de un profesor
     * GET /api/cursos/profesor/{profesorId}
     */
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<ApiResponse<List<Curso>>> obtenerDelProfesor(@PathVariable Integer profesorId) {
        try {
            List<Curso> cursos = cursoService.obtenerDelProfesor(profesorId);
            return ResponseEntity.ok(ApiResponse.success(cursos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo cursos: " + e.getMessage())
            );
        }
    }

    /**
     * Crea un nuevo curso
     * POST /api/cursos
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Curso>> crear(@RequestBody Curso curso) {
        try {
            Curso nuevo = cursoService.crear(curso);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Curso creado exitosamente", nuevo)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error creando curso: " + e.getMessage())
            );
        }
    }

    /**
     * Actualiza un curso
     * PUT /api/cursos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> actualizar(@PathVariable Integer id, @RequestBody Curso curso) {
        try {
            curso.setId(id);
            cursoService.actualizar(curso);
            return ResponseEntity.ok(ApiResponse.success("Curso actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error actualizando curso: " + e.getMessage())
            );
        }
    }

    /**
     * Elimina un curso
     * DELETE /api/cursos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> eliminar(@PathVariable Integer id) {
        try {
            cursoService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.success("Curso eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error eliminando curso: " + e.getMessage())
            );
        }
    }

    // ── Gestión de inscripciones ──────────────────────────────────────────────

    /**
     * Lista los estudiantes matriculados en un curso
     * GET /api/cursos/{id}/estudiantes
     */
    @GetMapping("/{id}/estudiantes")
    public ResponseEntity<ApiResponse<List<User>>> obtenerEstudiantes(@PathVariable Integer id) {
        List<User> estudiantes = inscripcionDao.findEstudiantesByCurso(id);
        return ResponseEntity.ok(ApiResponse.success("Estudiantes del curso", estudiantes));
    }

    /**
     * Matricula un estudiante en un curso
     * POST /api/cursos/{id}/estudiantes
     * Body: { "estudianteId": 4 }
     */
    @PostMapping("/{id}/estudiantes")
    public ResponseEntity<ApiResponse<String>> inscribir(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        Integer estudianteId = body.get("estudianteId");
        if (estudianteId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("estudianteId es requerido"));
        }
        inscripcionDao.inscribir(id, estudianteId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Estudiante matriculado exitosamente"));
    }

    /**
     * Desmatricula un estudiante de un curso
     * DELETE /api/cursos/{id}/estudiantes/{estudianteId}
     */
    @DeleteMapping("/{id}/estudiantes/{estudianteId}")
    public ResponseEntity<ApiResponse<String>> desinscribir(
            @PathVariable Integer id,
            @PathVariable Integer estudianteId) {
        inscripcionDao.desinscribir(id, estudianteId);
        return ResponseEntity.ok(ApiResponse.success("Estudiante desmatriculado exitosamente"));
    }
}
