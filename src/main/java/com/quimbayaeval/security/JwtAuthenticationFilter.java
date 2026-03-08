package com.quimbayaeval.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro JWT - Valida token en cada request y establece roles
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String userEmail = tokenProvider.getUserEmailFromToken(jwt);
                Integer userId = tokenProvider.getUserIdFromToken(jwt);
                String role = tokenProvider.getRoleFromToken(jwt);

                // Crear authorities con el rol (Spring Security requiere prefijo ROLE_)
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role.toUpperCase());
                
                // Crear authentication token con authorities
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            userEmail, 
                            null, 
                            Collections.singletonList(authority)
                        );
                authentication.setDetails(new JwtUserDetails(userId, userEmail, role));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario autenticado: {} con rol: {}", userEmail, role);
            }
        } catch (Exception ex) {
            log.error("Error al establecer autenticación de usuario", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el JWT del header Authorization
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
