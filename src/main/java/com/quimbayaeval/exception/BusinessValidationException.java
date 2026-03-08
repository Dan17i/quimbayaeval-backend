package com.quimbayaeval.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Excepción para errores de validación de reglas de negocio
 */
@Getter
public class BusinessValidationException extends RuntimeException {
    
    private final Map<String, String> errors;
    
    public BusinessValidationException(String message) {
        super(message);
        this.errors = new HashMap<>();
    }
    
    public BusinessValidationException(String field, String error) {
        super("Errores de validación");
        this.errors = new HashMap<>();
        this.errors.put(field, error);
    }
    
    public BusinessValidationException(Map<String, String> errors) {
        super("Errores de validación");
        this.errors = errors;
    }
}
