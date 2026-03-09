package com.quimbayaeval.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de interceptores y componentes Web
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitingInterceptor rateLimitingInterceptor;
    
    @Autowired
    private HttpMetricsInterceptor httpMetricsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor de métricas HTTP (primero para capturar todo)
        registry.addInterceptor(httpMetricsInterceptor)
                .addPathPatterns("/api/**");
        
        // Interceptor de rate limiting
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**");  // No aplicar rate limit a login
    }
}
