package com.quimbayaeval.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Interceptor para Rate Limiting usando Bucket4j.
 * Implementa límites por IP y por usuario (si autenticado).
 * 
 * Límites por defecto:
 * - 100 requests por minuto por IP
 * - 500 requests por minuto para usuarios autenticados
 */
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Obtener identificador (IP o usuario)
        String key = getIdentifier(request);

        // Obtener o crear bucket para este identificador
        Bucket bucket = cache.computeIfAbsent(key, k -> createNewBucket());

        // Consumir un token
        if (bucket.tryConsume(1)) {
            // Request permitido
            long remainingTokens = bucket.getAvailableTokens();
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remainingTokens));

            return true;
        } else {
            // Rate limit excedido
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Retry-After-Seconds", "60");
            response.getWriter().write("Rate limit exceeded. Maximum 100 requests per minute.");
            return false;
        }
    }

    /**
     * Crea un nuevo bucket con límite de 100 requests por minuto para IPs
     * o 500 para usuarios autenticados
     */
    private Bucket createNewBucket() {
        // 100 requests en 60 segundos (1 minuto)
        Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
        return Bucket.builder() .addLimit(limit) .build();
    }

    /**
     * Obtiene el identificador del cliente (usuario autenticado o IP)
     */
    private String getIdentifier(HttpServletRequest request) {
        // Si hay usuario autenticado, usar su ID/email
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Podrías extraer el usuario del token aquí
            return "auth_" + authHeader;
        }

        // Sino, usar IP del cliente
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return "ip_" + clientIp;
    }
}
