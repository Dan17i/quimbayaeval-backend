package com.quimbayaeval.config;

import com.quimbayaeval.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad con autorización por rol
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Configurando Security Filter Chain");
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    // Rutas públicas
                    .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                    
                    // Swagger y Actuator
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    
                    // Evaluaciones - Solo maestros y coordinadores pueden crear/editar
                    .requestMatchers(HttpMethod.POST, "/api/evaluaciones").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.PUT, "/api/evaluaciones/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/evaluaciones/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.POST, "/api/evaluaciones/*/publicar").hasAnyRole("MAESTRO", "COORDINADOR")
                    
                    // Calificaciones - Solo maestros
                    .requestMatchers("/api/calificaciones/**").hasRole("MAESTRO")
                    
                    // Usuarios - Solo coordinadores
                    .requestMatchers("/api/usuarios/**").hasRole("COORDINADOR")
                    .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("COORDINADOR", "MAESTRO")
                    
                    // Reportes - Maestros y coordinadores
                    .requestMatchers("/api/reportes/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    
                    // Cursos - Lectura para todos autenticados, escritura para maestros y coordinadores
                    .requestMatchers(HttpMethod.GET, "/api/cursos/**").authenticated()
                    .requestMatchers("/api/cursos/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    
                    // PQRS - Todos pueden crear y ver los suyos
                    .requestMatchers("/api/pqrs/**").authenticated()
                    
                    // Resto de rutas requieren autenticación
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security Filter Chain configurado exitosamente");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configurando CORS con orígenes permitidos: {}", allowedOrigins);
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
