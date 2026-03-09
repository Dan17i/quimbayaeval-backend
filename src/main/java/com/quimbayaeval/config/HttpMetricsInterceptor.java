package com.quimbayaeval.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para capturar métricas HTTP de requests
 */
@Component
public class HttpMetricsInterceptor implements HandlerInterceptor {

    private final MeterRegistry registry;
    private static final String TIMER_ATTRIBUTE = "request.timer";

    public HttpMetricsInterceptor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Timer.Sample sample = Timer.start(registry);
        request.setAttribute(TIMER_ATTRIBUTE, sample);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        Timer.Sample sample = (Timer.Sample) request.getAttribute(TIMER_ATTRIBUTE);
        if (sample != null) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String status = String.valueOf(response.getStatus());
            
            sample.stop(Timer.builder("http.server.requests")
                    .description("Tiempo de respuesta HTTP")
                    .tag("method", method)
                    .tag("uri", simplifyUri(uri))
                    .tag("status", status)
                    .tag("outcome", getOutcome(response.getStatus()))
                    .register(registry));
        }
    }

    /**
     * Simplifica la URI reemplazando IDs numéricos con {id}
     */
    private String simplifyUri(String uri) {
        return uri.replaceAll("/\\d+", "/{id}");
    }

    /**
     * Determina el outcome basado en el status code
     */
    private String getOutcome(int status) {
        if (status >= 200 && status < 300) return "SUCCESS";
        if (status >= 300 && status < 400) return "REDIRECTION";
        if (status >= 400 && status < 500) return "CLIENT_ERROR";
        if (status >= 500) return "SERVER_ERROR";
        return "UNKNOWN";
    }
}
