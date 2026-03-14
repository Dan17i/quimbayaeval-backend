package com.quimbayaeval.model.dto.request;

/**
 * DTO para editar el perfil del usuario autenticado.
 * El usuario puede cambiar su nombre y foto de perfil.
 */
public class EditarPerfilRequestDTO {
    private String name;
    private String fotoUrl;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
