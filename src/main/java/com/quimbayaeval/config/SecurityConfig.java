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
import java.util.stream.Collectors;

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

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
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
                    .requestMatchers(HttpMethod.GET, "/api/auth/validate").permitAll()
                    
                    // Swagger y Actuator
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    
                    // Evaluaciones - Solo maestros y coordinadores pueden crear/editar
                    .requestMatchers(HttpMethod.POST, "/api/evaluaciones").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.PUT, "/api/evaluaciones/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/evaluaciones/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.POST, "/api/evaluaciones/*/publicar").hasAnyRole("MAESTRO", "COORDINADOR")

                    // Preguntas - Solo maestros y coordinadores pueden escribir
                    .requestMatchers(HttpMethod.POST, "/api/preguntas/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.PUT, "/api/preguntas/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    .requestMatchers(HttpMethod.DELETE, "/api/preguntas/**").hasAnyRole("MAESTRO", "COORDINADOR")

                    // Calificaciones - Solo maestros
                    .requestMatchers("/api/calificaciones/**").hasRole("MAESTRO")

                    // Perfil del usuario autenticado (estudiante, maestro, coordinador)
                    .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()

                    // Usuarios - Coordinadores gestionan, maestros solo pueden listar
                    .requestMatchers(HttpMethod.GET, "/api/users").hasAnyRole("COORDINADOR", "MAESTRO")
                    .requestMatchers("/api/users/**").hasRole("COORDINADOR")
                    
                    // Reportes - Maestros y coordinadores
                    .requestMatchers("/api/reportes/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    
                    // Cursos - Lectura para todos autenticados, escritura para maestros y coordinadores
                    .requestMatchers(HttpMethod.GET, "/api/cursos/**").authenticated()
                    .requestMatchers("/api/cursos/**").hasAnyRole("MAESTRO", "COORDINADOR")
                    
                    // Submissions - estudiante puede crear y ver las suyas; el resto solo MAESTRO/COORDINADOR
                    .requestMatchers(HttpMethod.GET, "/api/submissions/mis-submissions").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/submissions").authenticated()
                    .requestMatchers("/api/submissions/**").hasAnyRole("MAESTRO", "COORDINADOR")

                    // PQRS - Todos pueden crear y ver los suyos
                    .requestMatchers("/api/pqrs/**").authenticated()
                    
                    // Resto de rutas requieren autenticación
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("Security Filter Chain configurado exitosamente");
        return http.build();
    }

    /**
     * Configuración centralizada de CORS (Cross-Origin Resource Sharing).
     * Permite peticiones desde aplicaciones cliente (Web React, móvil Android y entornos locales/emuladores),
     * garantizando compatibilidad con credenciales (cookies/tokens Authorization) y evitando fallos
     * por espacios en la lista separada por comas o patrones comodín.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        log.info("Configurando CORS con orígenes permitidos: {}", allowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        // Si contiene comodín '*', usar setAllowedOriginPatterns para permitir credenciales sin errores de navegador
        boolean hasWildcard = origins.stream().anyMatch(o -> o.contains("*"));
        if (hasWildcard) {
            configuration.setAllowedOriginPatterns(origins);
        } else {
            configuration.setAllowedOrigins(origins);
        }

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition", "X-Total-Count"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
