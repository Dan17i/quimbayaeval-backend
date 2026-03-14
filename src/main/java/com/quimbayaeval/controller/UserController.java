package com.quimbayaeval.controller;

import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /** GET /api/users?role=maestro — lista usuarios activos, sin password */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUsers(
            @RequestParam(required = false) String role) {

        List<UserEntity> users = (role != null)
                ? userRepository.findByRole(role)
                : userRepository.findByActiveTrue();

        List<Map<String, Object>> result = users.stream()
                .filter(UserEntity::getActive)
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id",    u.getId());
                    m.put("name",  u.getName());
                    m.put("email", u.getEmail());
                    m.put("role",  u.getRole());
                    m.put("active", u.getActive());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", result));
    }

    /** PATCH /api/users/{id}/status — body: { "status": "activo" | "bloqueado" } */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El campo 'status' es requerido"));
        }
        return userRepository.findById(id).map(user -> {
            user.setActive("activo".equalsIgnoreCase(status));
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success("Estado actualizado a: " + status));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    /** DELETE /api/users/{id} — soft delete */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Integer id) {
        return userRepository.findById(id).map(user -> {
            user.setActive(false);
            userRepository.save(user);
            return ResponseEntity.ok(ApiResponse.success("Usuario eliminado exitosamente"));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }
}
