package com.quimbayaeval.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuración de métricas personalizadas con Micrometer
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    /**
     * Habilita el aspecto @Timed para medir tiempos de ejecución
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Registra métricas personalizadas de negocio
     */
    @Bean
    public CustomMetrics customMetrics(MeterRegistry registry) {
        return new CustomMetrics(registry);
    }
}
