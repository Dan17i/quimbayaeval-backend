package com.quimbayaeval.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de caché
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        // Usar ConcurrentMapCacheManager como alternativa simple a EhCache
        return new ConcurrentMapCacheManager(
            "evaluacionesByCurso",
            "evaluacionesByEstado",
            "cursosByCodigo"
        );
    }
}
