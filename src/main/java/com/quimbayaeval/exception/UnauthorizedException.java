package com.quimbayaeval.exception;

/**
 * Excepción para errores de autenticación/autorización
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
}
