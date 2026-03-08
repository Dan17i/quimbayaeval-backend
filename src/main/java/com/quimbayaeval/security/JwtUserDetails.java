package com.quimbayaeval.security;

/**
 * Clase para almacenar detalles del usuario autenticado
 */
public class JwtUserDetails {
    private Integer userId;
    private String email;
    private String role;

    public JwtUserDetails(Integer userId, String email, String role) {
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
