package com.quimbayaeval.exception;

/**
 * Excepción lanzada cuando un recurso no se encuentra
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resource, Integer id) {
        super(String.format("%s con ID %d no encontrado", resource, id));
    }
    
    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s con %s='%s' no encontrado", resource, field, value));
    }
}
