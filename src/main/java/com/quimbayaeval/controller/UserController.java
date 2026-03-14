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

    /**
     * GET /api/users?role=maestro
     * Retorna usuarios activos, opcionalmente filtrados por rol. Sin password.
     */
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
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Usuarios obtenidos", result));
    }
}
