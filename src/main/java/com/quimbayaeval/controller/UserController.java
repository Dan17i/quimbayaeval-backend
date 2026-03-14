package com.quimbayaeval.controller;

import com.quimbayaeval.dao.CursoDao;
import com.quimbayaeval.dao.InscripcionDao;
import com.quimbayaeval.dao.UserDao;
import com.quimbayaeval.model.Curso;
import com.quimbayaeval.model.dto.ApiResponse;
import com.quimbayaeval.model.dto.request.EditarPerfilRequestDTO;
import com.quimbayaeval.model.entity.UserEntity;
import com.quimbayaeval.repository.UserRepository;
import com.quimbayaeval.security.JwtUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private UserDao userDao;
    @Autowired private CursoDao cursoDao;
    @Autowired private InscripcionDao inscripcionDao;
    @Autowired private PasswordEncoder passwordEncoder;

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
                    m.put("id",      u.getId());
                    m.put("name",    u.getName());
                    m.put("email",   u.getEmail());
                    m.put("role",    u.getRole());
                    m.put("active",  u.getActive());
                    m.put("fotoUrl", u.getFotoUrl());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", result));
    }

    /**
     * GET /api/users/me — perfil del usuario autenticado.
     * Estudiante: incluye sus cursos inscritos.
     * Maestro: incluye sus cursos asignados.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMe(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        return userDao.findById(userDetails.getUserId()).map(user -> {
            Map<String, Object> perfil = new HashMap<>();
            perfil.put("id",      user.getId());
            perfil.put("name",    user.getName());
            perfil.put("email",   user.getEmail());
            perfil.put("role",    user.getRole());
            perfil.put("fotoUrl", user.getFotoUrl());

            if ("estudiante".equals(user.getRole())) {
                // cursos en los que está inscrito
                List<Curso> cursos = inscripcionDao.findCursosByEstudiante(user.getId());
                perfil.put("cursos", cursos);
            } else if ("maestro".equals(user.getRole())) {
                // cursos que tiene asignados
                List<Curso> cursos = cursoDao.findByProfesor(user.getId());
                perfil.put("cursos", cursos);
            }

            return ResponseEntity.ok(ApiResponse.success("Perfil obtenido", perfil));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    /**
     * PUT /api/users/me — editar nombre y foto de perfil
     * Body: { "name": "...", "fotoUrl": "https://..." }
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateMe(
            Authentication authentication,
            @RequestBody EditarPerfilRequestDTO dto) {

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        return userDao.findById(userDetails.getUserId()).map(user -> {
            if (dto.getName() != null && !dto.getName().isBlank()) {
                user.setName(dto.getName());
            }
            if (dto.getFotoUrl() != null) {
                user.setFotoUrl(dto.getFotoUrl());
            }
            userDao.updatePerfil(user);
            return ResponseEntity.ok(ApiResponse.success("Perfil actualizado exitosamente"));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
    }

    /**
     * PUT /api/users/me/password — cambiar contraseña
     * Body: { "passwordActual": "...", "passwordNueva": "..." }
     */
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> body) {

        String passwordActual = body.get("passwordActual");
        String passwordNueva  = body.get("passwordNueva");

        if (passwordActual == null || passwordNueva == null) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("passwordActual y passwordNueva son requeridos"));
        }
        if (passwordNueva.length() < 6) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("La nueva contraseña debe tener al menos 6 caracteres"));
        }

        JwtUserDetails userDetails = (JwtUserDetails) authentication.getDetails();
        return userDao.findById(userDetails.getUserId()).map(user -> {
            if (!passwordEncoder.matches(passwordActual, user.getPassword())) {
                return ResponseEntity.status(400)
                    .<ApiResponse<String>>body(ApiResponse.error("La contraseña actual es incorrecta"));
            }
            userDao.updatePassword(user.getId(), passwordEncoder.encode(passwordNueva));
            return ResponseEntity.ok(ApiResponse.<String>success("Contraseña actualizada exitosamente"));
        }).orElse(ResponseEntity.status(404).body(ApiResponse.error("Usuario no encontrado")));
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
