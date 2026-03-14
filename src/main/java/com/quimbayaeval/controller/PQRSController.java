package com.quimbayaeval.controller;

import com.quimbayaeval.model.PQRS;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.service.PQRSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para PQRS
 */
@RestController
@RequestMapping("/api/pqrs")
@CrossOrigin(origins = "*")
public class PQRSController {

    @Autowired
    private PQRSService pqrsService;

    /**
     * Obtiene todos los PQRS
     * GET /api/pqrs
     * Parámetros opcionales:
     *   ?page=0&size=10&sort=id&direction=DESC
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PQRS>>> obtenerTodos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        try {
            List<PQRS> pqrsList;
            if (page != null && size != null) {
                Map<String, Object> filters = new HashMap<>();
                pqrsList = pqrsService.obtenerTodos(filters, page, size, sort, direction);
            } else {
                pqrsList = pqrsService.obtenerTodos();
            }
            return ResponseEntity.ok(ApiResponse.success(pqrsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene PQRS del usuario autenticado
     * GET /api/pqrs/mis-pqrs
     */
    @GetMapping("/mis-pqrs")
    public ResponseEntity<ApiResponse<List<PQRS>>> obtenerMisPqrs(
            org.springframework.security.core.Authentication authentication) {
        try {
            List<PQRS> pqrsList;
            if (authentication != null && authentication.getPrincipal() instanceof com.quimbayaeval.security.JwtUserDetails userDetails) {
                pqrsList = pqrsService.obtenerPorUsuario(userDetails.getUserId());
            } else {
                pqrsList = pqrsService.obtenerTodos();
            }
            return ResponseEntity.ok(ApiResponse.success(pqrsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene un PQRS por ID
     * GET /api/pqrs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PQRS>> obtenerPorId(@PathVariable Integer id) {
        try {
            Optional<PQRS> pqrs = pqrsService.obtenerPorId(id);
            if (pqrs.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success(pqrs.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error("PQRS no encontrado")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene PQRS por usuario
     * GET /api/pqrs/usuario/{usuarioId}
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<ApiResponse<List<PQRS>>> obtenerPorUsuario(@PathVariable Integer usuarioId) {
        try {
            List<PQRS> pqrsList = pqrsService.obtenerPorUsuario(usuarioId);
            return ResponseEntity.ok(ApiResponse.success(pqrsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Obtiene PQRS por estado
     * GET /api/pqrs/estado/{estado}
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<ApiResponse<List<PQRS>>> obtenerPorEstado(@PathVariable String estado) {
        try {
            List<PQRS> pqrsList = pqrsService.obtenerPorEstado(estado);
            return ResponseEntity.ok(ApiResponse.success(pqrsList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("Error obteniendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Crea un nuevo PQRS
     * POST /api/pqrs
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PQRS>> crear(@RequestBody PQRS pqrs) {
        try {
            PQRS nuevo = pqrsService.crear(pqrs);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("PQRS creado exitosamente", nuevo)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error creando PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Responde un PQRS (alias PUT para compatibilidad con frontend)
     * PUT /api/pqrs/{id}/responder
     */
    @PutMapping("/{id}/responder")
    public ResponseEntity<ApiResponse<String>> responderPut(
            @PathVariable Integer id,
            @RequestBody RespuestaRequest request) {
        try {
            pqrsService.responder(id, request.getRespuesta(), request.getRespondidoPorId());
            // Actualizar estado si viene en el request
            if (request.getEstado() != null) {
                Optional<PQRS> pqrsOpt = pqrsService.obtenerPorId(id);
                pqrsOpt.ifPresent(p -> {
                    p.setEstado(request.getEstado());
                    pqrsService.actualizar(p);
                });
            }
            return ResponseEntity.ok(ApiResponse.success("PQRS respondido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error respondiendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Responde un PQRS
     * POST /api/pqrs/{id}/respond
     */
    @PostMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<String>> responder(
            @PathVariable Integer id,
            @RequestBody RespuestaRequest request) {
        try {
            pqrsService.responder(id, request.getRespuesta(), request.getRespondidoPorId());
            return ResponseEntity.ok(ApiResponse.success("PQRS respondido exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error respondiendo PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Actualiza un PQRS
     * PUT /api/pqrs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> actualizar(@PathVariable Integer id, @RequestBody PQRS pqrs) {
        try {
            pqrs.setId(id);
            pqrsService.actualizar(pqrs);
            return ResponseEntity.ok(ApiResponse.success("PQRS actualizado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error actualizando PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * Elimina un PQRS
     * DELETE /api/pqrs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> eliminar(@PathVariable Integer id) {
        try {
            pqrsService.eliminar(id);
            return ResponseEntity.ok(ApiResponse.success("PQRS eliminado exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiResponse.error("Error eliminando PQRS: " + e.getMessage())
            );
        }
    }

    /**
     * DTO para respuesta de PQRS
     */
    public static class RespuestaRequest {
        private String respuesta;
        private Integer respondidoPorId;
        private String estado;

        public String getRespuesta() { return respuesta; }
        public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

        public Integer getRespondidoPorId() { return respondidoPorId; }
        public void setRespondidoPorId(Integer respondidoPorId) { this.respondidoPorId = respondidoPorId; }

        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
    }
}
