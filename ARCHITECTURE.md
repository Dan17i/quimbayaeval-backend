# 🏗️ Arquitectura Técnica - QuimbayaEVAL Backend

Documentación técnica de la arquitectura, patrones de diseño y decisiones técnicas del sistema.

## 📊 Vista General

QuimbayaEVAL Backend es una aplicación REST API construida con Spring Boot que sigue una arquitectura en capas (Layered Architecture) con separación clara de responsabilidades.

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE (Frontend React)                  │
│                   http://localhost:5173                      │
└──────────────────────┬──────────────────────────────────────┘
                       │
                   HTTP/JSON
                   JWT Token
                       │
┌──────────────────────┴──────────────────────────────────────┐
│                  SPRING BOOT APPLICATION                     │
│                   http://localhost:8080                      │
├─────────────────────────────────────────────────────────────┤
│  Security Layer                                             │
│  ├─ JwtAuthenticationFilter                                 │
│  ├─ JwtTokenProvider                                        │
│  └─ SecurityConfig (CORS, Auth)                             │
├─────────────────────────────────────────────────────────────┤
│  Controller Layer (REST Endpoints)                          │
│  ├─ AuthController                                          │
│  ├─ CursoController                                         │
│  ├─ EvaluacionController                                    │
│  ├─ PQRSController                                          │
│  └─ CalificacionController                                  │
├─────────────────────────────────────────────────────────────┤
│  Service Layer (Business Logic)                             │
│  ├─ AuthService                                             │
│  ├─ CursoService                                            │
│  ├─ EvaluacionService                                       │
│  ├─ PQRSService                                             │
│  └─ CalificacionService                                     │
├─────────────────────────────────────────────────────────────┤
│  Repository Layer (Data Access)                             │
│  ├─ UserRepository (JPA)                                    │
│  ├─ CursoRepository (JPA)                                   │
│  ├─ EvaluacionRepository (JPA)                              │
│  └─ PQRSRepository (JPA)                                    │
├─────────────────────────────────────────────────────────────┤
│  Entity Layer (Domain Models)                               │
│  ├─ UserEntity                                              │
│  ├─ CursoEntity                                             │
│  ├─ EvaluacionEntity                                        │
│  └─ PQRSEntity                                              │
└──────────────────────┬──────────────────────────────────────┘
                       │
                   JDBC/JPA
                       │
┌──────────────────────┴──────────────────────────────────────┐
│                  PostgreSQL Database                         │
│                   localhost:5432                             │
└─────────────────────────────────────────────────────────────┘
```

## 🎯 Patrones de Diseño

### 1. Layered Architecture (Arquitectura en Capas)

Separación clara de responsabilidades en capas:

- **Controller**: Manejo de HTTP requests/responses
- **Service**: Lógica de negocio
- **Repository**: Acceso a datos
- **Entity**: Modelos de dominio

### 2. Dependency Injection

Spring Boot gestiona todas las dependencias mediante inyección:

```java
@Service
public class EvaluacionService {
    private final EvaluacionRepository evaluacionRepository;
    private final CustomMetrics customMetrics;
    
    @Autowired
    public EvaluacionService(
        EvaluacionRepository evaluacionRepository,
        CustomMetrics customMetrics
    ) {
        this.evaluacionRepository = evaluacionRepository;
        this.customMetrics = customMetrics;
    }
}
```

### 3. DTO Pattern (Data Transfer Object)

Separación entre entidades de base de datos y objetos de transferencia:

```java
// Entity (Base de datos)
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;  // Hash BCrypt
    private String role;
}

// DTO (API Response)
public class LoginResponse {
    private String token;
    private Long id;
    private String name;
    private String email;
    private String role;
    // Sin password
}
```

### 4. Repository Pattern

Abstracción del acceso a datos mediante Spring Data JPA:

```java
@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(String role);
    boolean existsByEmail(String email);
}
```

### 5. Exception Handling Pattern

Manejo centralizado de excepciones:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(
        ResourceNotFoundException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()));
    }
}
```

## 🔐 Seguridad

### Flujo de Autenticación JWT

```
┌─────────────┐                                    ┌─────────────┐
│   Cliente   │                                    │   Backend   │
└──────┬──────┘                                    └──────┬──────┘
       │                                                  │
       │  1. POST /api/auth/login                        │
       │     {email, password, role}                     │
       ├────────────────────────────────────────────────>│
       │                                                  │
       │                          2. Validar credenciales│
       │                             (BCrypt.matches)    │
       │                                                  │
       │                          3. Generar JWT         │
       │                             (JwtTokenProvider)  │
       │                                                  │
       │  4. Response {token, user}                      │
       │<────────────────────────────────────────────────┤
       │                                                  │
       │  5. Guardar token en localStorage               │
       │                                                  │
       │  6. GET /api/cursos                             │
       │     Header: Authorization: Bearer <token>       │
       ├────────────────────────────────────────────────>│
       │                                                  │
       │                          7. JwtAuthFilter       │
       │                             - Extraer token     │
       │                             - Validar firma     │
       │                             - Verificar exp     │
       │                             - Autenticar user   │
       │                                                  │
       │  8. Response {cursos}                           │
       │<────────────────────────────────────────────────┤
       │                                                  │
```

### Componentes de Seguridad

#### 1. JwtTokenProvider

Genera y valida tokens JWT:

```java
@Component
public class JwtTokenProvider {
    
    public String generateToken(Authentication authentication) {
        JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("userId", userDetails.getId())
            .claim("role", userDetails.getRole())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### 2. JwtAuthenticationFilter

Intercepta requests y valida tokens:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String token = getJwtFromRequest(request);
        
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

#### 3. SecurityConfig

Configuración de seguridad y CORS:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/evaluaciones/**")
                    .hasAnyRole("MAESTRO", "COORDINADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/**")
                    .hasRole("COORDINADOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

## 💾 Modelo de Datos

### Diagrama Entidad-Relación

```
┌─────────────────┐
│     users       │
├─────────────────┤
│ id (PK)         │
│ name            │
│ email (UNIQUE)  │
│ password        │
│ role            │
│ active          │
└────────┬────────┘
         │
         │ 1:N (profesor)
         │
┌────────┴────────┐
│     cursos      │
├─────────────────┤
│ id (PK)         │
│ codigo          │
│ nombre          │
│ descripcion     │
│ creditos        │
│ profesor_id (FK)│
│ activo          │
└────────┬────────┘
         │
         │ 1:N
         │
┌────────┴────────────┐
│   evaluaciones      │
├─────────────────────┤
│ id (PK)             │
│ nombre              │
│ descripcion         │
│ tipo                │
│ curso_id (FK)       │
│ profesor_id (FK)    │
│ estado              │
│ fecha_inicio        │
│ fecha_fin           │
│ duracion_minutos    │
│ intentos_permitidos │
│ puntaje_total       │
└─────────────────────┘

┌─────────────────┐
│      pqrs       │
├─────────────────┤
│ id (PK)         │
│ tipo            │
│ asunto          │
│ descripcion     │
│ usuario_id (FK) │
│ estado          │
│ fecha_creacion  │
│ fecha_respuesta │
│ respuesta       │
└─────────────────┘
```

### Relaciones

- **User → Curso**: Un profesor puede tener múltiples cursos (1:N)
- **Curso → Evaluacion**: Un curso puede tener múltiples evaluaciones (1:N)
- **User → PQRS**: Un usuario puede crear múltiples PQRS (1:N)
- **User → Evaluacion**: Un profesor puede crear múltiples evaluaciones (1:N)

## 🔄 Flujo de Datos

### Ejemplo: Crear Evaluación

```
1. Cliente (Frontend)
   └─> POST /api/evaluaciones
       Body: CrearEvaluacionRequestDTO
       Header: Authorization: Bearer <token>

2. JwtAuthenticationFilter
   └─> Validar token
   └─> Autenticar usuario
   └─> Verificar rol (maestro/coordinador)

3. EvaluacionController
   └─> @PostMapping("/api/evaluaciones")
   └─> Validar DTO (@Valid)
   └─> Llamar a evaluacionService.crear()

4. EvaluacionService
   └─> Validar lógica de negocio
   └─> Convertir DTO → Entity
   └─> Llamar a evaluacionRepository.save()
   └─> Registrar métrica (customMetrics)
   └─> Convertir Entity → DTO
   └─> Retornar resultado

5. EvaluacionRepository (JPA)
   └─> INSERT INTO evaluaciones (...)
   └─> Retornar entity con ID generado

6. EvaluacionController
   └─> Envolver en ApiResponse
   └─> Retornar ResponseEntity (201 Created)

7. Cliente (Frontend)
   └─> Recibir response
   └─> Actualizar UI
```

## 📊 Métricas y Monitoreo

### Arquitectura de Métricas

```
┌─────────────────────────────────────────────────────────┐
│              Spring Boot Application                    │
├─────────────────────────────────────────────────────────┤
│  Controllers/Services                                   │
│  └─> @Timed annotations                                 │
│  └─> CustomMetrics.increment()                          │
│  └─> CustomMetrics.recordTimer()                        │
├─────────────────────────────────────────────────────────┤
│  Micrometer (Metrics Facade)                            │
│  └─> Counter, Timer, Gauge                              │
│  └─> MeterRegistry                                      │
├─────────────────────────────────────────────────────────┤
│  Spring Boot Actuator                                   │
│  └─> /actuator/metrics                                  │
│  └─> /actuator/prometheus                               │
│  └─> /actuator/health                                   │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ HTTP Scraping
                   │
┌──────────────────┴──────────────────────────────────────┐
│              Prometheus Server                          │
│  └─> Scrape metrics every 15s                           │
│  └─> Store time-series data                             │
│  └─> Query with PromQL                                  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   │ Data Source
                   │
┌──────────────────┴──────────────────────────────────────┐
│              Grafana Dashboard                          │
│  └─> Visualize metrics                                  │
│  └─> Create alerts                                      │
│  └─> Monitor performance                                │
└─────────────────────────────────────────────────────────┘
```

### Métricas Personalizadas

```java
@Component
public class CustomMetrics {
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Counter evaluacionCreatedCounter;
    private final Timer evaluacionCreationTimer;
    
    public CustomMetrics(MeterRegistry registry) {
        this.loginSuccessCounter = Counter.builder("auth.login.success")
            .description("Número de logins exitosos")
            .tag("type", "authentication")
            .register(registry);
        
        this.evaluacionCreationTimer = Timer.builder("evaluacion.creation.time")
            .description("Tiempo de creación de evaluaciones")
            .register(registry);
    }
    
    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }
    
    public Timer.Sample startEvaluacionCreationTimer() {
        return Timer.start();
    }
    
    public void stopEvaluacionCreationTimer(Timer.Sample sample) {
        sample.stop(evaluacionCreationTimer);
    }
}
```

## 🚀 Optimizaciones

### 1. Caché

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
            new ConcurrentMapCache("cursos"),
            new ConcurrentMapCache("evaluaciones")
        ));
        return cacheManager;
    }
}

@Service
public class CursoService {
    
    @Cacheable(value = "cursos", key = "#id")
    public Curso findById(Long id) {
        return cursoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado"));
    }
    
    @CacheEvict(value = "cursos", key = "#id")
    public void delete(Long id) {
        cursoRepository.deleteById(id);
    }
}
```

### 2. Rate Limiting

```java
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler
    ) throws Exception {
        
        String key = getClientKey(request);
        Bucket bucket = buckets.computeIfAbsent(key, k -> createBucket());
        
        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429); // Too Many Requests
            return false;
        }
    }
    
    private Bucket createBucket() {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
            .build();
    }
}
```

### 3. Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

## 🧪 Testing

### Pirámide de Tests

```
        ┌─────────────┐
        │   E2E Tests │  ← Pocos, lentos, costosos
        │   (5%)      │
        ├─────────────┤
        │ Integration │  ← Algunos, moderados
        │   Tests     │
        │   (15%)     │
        ├─────────────┤
        │   Unit      │  ← Muchos, rápidos, baratos
        │   Tests     │
        │   (80%)     │
        └─────────────┘
```

### Estrategia de Testing

```java
// Unit Test (Service)
@ExtendWith(MockitoExtension.class)
class EvaluacionServiceTest {
    
    @Mock
    private EvaluacionRepository evaluacionRepository;
    
    @InjectMocks
    private EvaluacionService evaluacionService;
    
    @Test
    void crear_DebeCrearEvaluacion() {
        // Arrange
        EvaluacionEntity entity = new EvaluacionEntity();
        when(evaluacionRepository.save(any())).thenReturn(entity);
        
        // Act
        Evaluacion result = evaluacionService.crear(evaluacion);
        
        // Assert
        assertNotNull(result);
        verify(evaluacionRepository).save(any());
    }
}

// Integration Test (Controller)
@SpringBootTest
@AutoConfigureMockMvc
class EvaluacionControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "MAESTRO")
    void crear_DebeRetornar201() throws Exception {
        mockMvc.perform(post("/api/evaluaciones")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }
}
```

## 📦 Estructura de Paquetes

```
src/main/java/com/quimbayaeval/
├── config/                      # Configuración
│   ├── CacheConfig.java
│   ├── SecurityConfig.java
│   ├── WebMvcConfig.java
│   ├── MetricsConfig.java
│   └── CustomMetrics.java
├── controller/                  # REST Controllers
│   ├── AuthController.java
│   ├── CursoController.java
│   ├── EvaluacionController.java
│   └── PQRSController.java
├── service/                     # Business Logic
│   ├── AuthService.java
│   ├── CursoService.java
│   ├── EvaluacionService.java
│   └── PQRSService.java
├── repository/                  # Data Access (JPA)
│   ├── UserRepository.java
│   ├── CursoRepository.java
│   ├── EvaluacionRepository.java
│   └── PQRSRepository.java
├── model/
│   ├── entity/                 # JPA Entities
│   │   ├── UserEntity.java
│   │   ├── CursoEntity.java
│   │   └── EvaluacionEntity.java
│   └── dto/                    # Data Transfer Objects
│       ├── request/
│       │   ├── LoginRequestDTO.java
│       │   └── CrearEvaluacionRequestDTO.java
│       └── ApiResponse.java
├── security/                    # Security Components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── JwtUserDetails.java
├── exception/                   # Exception Handling
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── BusinessValidationException.java
└── QuimbayaEvalBackendApplication.java
```

## 🔧 Decisiones Técnicas

### ¿Por qué Spring Boot?

- Ecosistema maduro y bien documentado
- Configuración por convención
- Amplia comunidad y soporte
- Integración fácil con herramientas de monitoreo

### ¿Por qué JPA/Hibernate?

- Abstracción del acceso a datos
- Portabilidad entre bases de datos
- Caché de segundo nivel
- Lazy loading automático

### ¿Por qué JWT?

- Stateless (no requiere sesiones en servidor)
- Escalable horizontalmente
- Contiene información del usuario (claims)
- Estándar de la industria

### ¿Por qué PostgreSQL?

- Open source y gratuito
- ACID compliant
- Soporte para JSON
- Excelente rendimiento

## 📚 Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security](https://docs.spring.io/spring-security/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [JWT.io](https://jwt.io/)
- [Micrometer](https://micrometer.io/docs)

---

**Última actualización**: Marzo 2026  
**Versión**: 1.0.0
