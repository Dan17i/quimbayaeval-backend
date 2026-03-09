package com.quimbayaeval.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Métricas personalizadas de negocio para QuimbayaEVAL
 */
@Component
public class CustomMetrics {

    private final MeterRegistry registry;
    
    // Contadores
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter evaluacionCreatedCounter;
    private final Counter evaluacionSubmittedCounter;
    private final Counter pqrsCreatedCounter;
    
    // Timers
    private final Timer evaluacionCreationTimer;
    private final Timer submissionProcessingTimer;

    public CustomMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        // Inicializar contadores
        this.loginSuccessCounter = Counter.builder("auth.login.success")
                .description("Número de logins exitosos")
                .tag("type", "authentication")
                .register(registry);
                
        this.loginFailureCounter = Counter.builder("auth.login.failure")
                .description("Número de logins fallidos")
                .tag("type", "authentication")
                .register(registry);
                
        this.evaluacionCreatedCounter = Counter.builder("evaluacion.created")
                .description("Número de evaluaciones creadas")
                .tag("type", "business")
                .register(registry);
                
        this.evaluacionSubmittedCounter = Counter.builder("evaluacion.submitted")
                .description("Número de evaluaciones enviadas por estudiantes")
                .tag("type", "business")
                .register(registry);
                
        this.pqrsCreatedCounter = Counter.builder("pqrs.created")
                .description("Número de PQRS creadas")
                .tag("type", "business")
                .register(registry);
        
        // Inicializar timers
        this.evaluacionCreationTimer = Timer.builder("evaluacion.creation.time")
                .description("Tiempo de creación de evaluaciones")
                .tag("type", "performance")
                .register(registry);
                
        this.submissionProcessingTimer = Timer.builder("submission.processing.time")
                .description("Tiempo de procesamiento de envíos")
                .tag("type", "performance")
                .register(registry);
    }

    // Métodos para incrementar contadores
    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailure() {
        loginFailureCounter.increment();
    }

    public void incrementEvaluacionCreated() {
        evaluacionCreatedCounter.increment();
    }

    public void incrementEvaluacionSubmitted() {
        evaluacionSubmittedCounter.increment();
    }

    public void incrementPqrsCreated() {
        pqrsCreatedCounter.increment();
    }

    // Métodos para timers
    public Timer.Sample startEvaluacionCreationTimer() {
        return Timer.start(registry);
    }

    public void stopEvaluacionCreationTimer(Timer.Sample sample) {
        sample.stop(evaluacionCreationTimer);
    }

    public Timer.Sample startSubmissionProcessingTimer() {
        return Timer.start(registry);
    }

    public void stopSubmissionProcessingTimer(Timer.Sample sample) {
        sample.stop(submissionProcessingTimer);
    }

    // Método para registrar gauge (valor actual)
    public <T> void registerActiveEvaluations(T obj, java.util.function.ToDoubleFunction<T> valueFunction) {
        registry.gauge("evaluacion.active.count", obj, valueFunction);
    }

    public <T> void registerActivePqrs(T obj, java.util.function.ToDoubleFunction<T> valueFunction) {
        registry.gauge("pqrs.active.count", obj, valueFunction);
    }
}
