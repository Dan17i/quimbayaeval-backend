# Guía de Filtrado Avanzado, Rate Limiting y Caché - QuimbayaEVAL Backend

## 1. Filtrado Avanzado

El backend ahora soporta filtrado avanzado con operadores flexibles en lugar de solo igualdad.

### Operadores Soportados

```
= (EQUALS)              Igualdad exacta
LIKE                    Búsqueda parcial (case-sensitive)
ILIKE                   Búsqueda parcial (case-insensitive, PostgreSQL)
>  (GT)                 Mayor que
<  (LT)                 Menor que
>= (GTE)                Mayor o igual que
<= (LTE)                Menor o igual que
IN                      Valor en lista
BETWEEN                 Rango entre dos valores
IS NULL                 Campo nulo
IS NOT NULL             Campo no nulo
```

### Ejemplos de Uso

#### 1. Búsqueda de Evaluaciones Activas
```bash
GET /api/evaluaciones?estado=Activa&page=0&size=10
```

#### 2. Búsqueda por Nombre (LIKE)
```bash
GET /api/evaluaciones?nombre=Parcial&page=0&size=10
```
Retorna todas las evaluaciones cuyo nombre contiene "Parcial" (case-sensitive).

#### 3. Búsqueda Case-Insensitive
```bash
GET /api/evaluaciones?nombre=cálculo&page=0&size=10
```
Retorna todas las evaluaciones cuyo nombre contiene "cálculo" o "Cálculo" (case-insensitive).

#### 4. Filtros Múltiples
```bash
GET /api/evaluaciones?tipo=Examen&estado=Activa&cursoId=1&page=0&size=10
```
Combina múltiples filtros con AND lógico.

#### 5. Ordenamiento
```bash
GET /api/evaluaciones?page=0&size=10&sort=nombre&direction=DESC
```

#### 6. Filtro por Rango de Duración
Para buscar evaluaciones con duración entre 30 y 120 minutos, usar el operador BETWEEN en el código.

### Implementación en el Controlador

Los controladores ahora aceptan parámetros de query opcionales que se convierten automáticamente en criterios de filtro:

```java
@GetMapping
@Cacheable(value = "evaluacionesByCurso", key = "'all_' + #page + '_' + #size + '_' + #sort")
public ResponseEntity<ApiResponse<List<Evaluacion>>> obtenerTodas(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String direction,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) Integer cursoId,
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) Boolean publicada)
```

### Parámetros por Entidad

#### Evaluaciones
- `estado`: "Activa", "Cerrada", "Programada", "Borrador"
- `tipo`: "Examen", "Quiz", "Taller", "Proyecto", "Tarea"
- `cursoId`: ID del curso
- `nombre`: Búsqueda de texto
- `publicada`: true/false
- `sort`: "nombre", "id", "deadline", "estado"
- `direction`: "ASC" o "DESC"

#### Cursos
- `nombre`: Búsqueda de texto
- `profesor`: Búsqueda de texto
- `sort`: "codigo", "nombre", "id"

#### PQRS
- `tipo`: "Pregunta", "Reclamo", "Sugerencia", "Queja"
- `estado`: "Pendiente", "En Proceso", "Resuelto"
- `cursoId`: ID del curso
- `sort`: "id", "fecha"

---

## 2. Rate Limiting

El backend implementa **Rate Limiting** automático para prevenir abuso de la API.

### Límites Actuales

```
Por IP:           100 requests por minuto
Por Usuario Auth: 100 requests por minuto (puede configurarse)
```

### Excepciones

Los endpoints de autenticación (`/api/auth/**`) están excluidos de rate limiting para permitir login.

### Headers de Respuesta

```
X-Rate-Limit-Remaining: Número de requests restantes en el minuto
X-Rate-Limit-Retry-After-Seconds: Segundos hasta poder reintentar
```

### Ejemplo de Respuesta al Exceder Límite

```
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Retry-After-Seconds: 60
Content-Type: application/json

"Rate limit exceeded. Maximum 100 requests per minute."
```

### Configuración

Para personalizar los límites, editar `RateLimitingInterceptor.java`:

```java
private Bucket createNewBucket() {
    // Cambiar 100 por el límite deseado
    Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
    return Bucket4j.builder()
            .addLimit(limit)
            .build();
}
```

### Cómo Se Identifica al Cliente

1. **Usuario Autenticado**: Usa el token JWT (header `Authorization: Bearer <token>`)
2. **Usuario Anónimo**: Usa la IP del cliente (header `X-Forwarded-For` o `RemoteAddr`)

---

## 3. Caché

El backend implementa **caché en memoria (EhCache)** para optimizar endpoints frecuentes.

### Estrategia de Caché

```
Evaluaciones por curso:     TTL 10 minutos
Evaluaciones por estado:    TTL 5 minutos
Cursos:                     TTL 15 minutos
PQRS:                       TTL 10 minutos
Preguntas por evaluación:   TTL 15 minutos
Usuarios:                   TTL 20 minutos
```

### Endpoints Cacheados

#### Lectura (GET)
```
GET /api/evaluaciones/curso/{cursoId}         ✓ Cacheado
GET /api/evaluaciones/estado/activas          ✓ Cacheado
GET /api/cursos                               ✓ Cacheado (con paginación)
GET /api/pqrs                                 ✓ Cacheado
```

#### Escritura (POST/PUT/DELETE)
Cuando se crea, actualiza o elimina una entidad, el caché se invalida automáticamente:

```
POST   /api/evaluaciones                      ✗ Invalida caché
PUT    /api/evaluaciones/{id}                 ✗ Invalida caché
DELETE /api/evaluaciones/{id}                 ✗ Invalida caché
POST   /api/evaluaciones/{id}/publicar        ✗ Invalida caché
```

### Impacto de Rendimiento

**Antes de caché**:
```
GET /api/evaluaciones/curso/1
  └─ Query a BD: ~50ms
  └─ Mapeo ResultSet: ~10ms
  └─ Serialización JSON: ~5ms
  Total: ~65ms
```

**Después de caché**:
```
GET /api/evaluaciones/curso/1  (1er request)
  └─ Query a BD: ~50ms
  Total: ~50ms

GET /api/evaluaciones/curso/1  (2do-9no request)
  └─ Desde caché: ~1ms
  Total: ~1ms
```

### Invalidación Manual de Caché

Si necesitas forzar recarga, puedes usar la CLI o un endpoint admin (opcional):

```bash
# Sin endpoint de invalidación, esperar TTL
# O reiniciar la aplicación
```

### Configuración de EhCache

Editar `src/main/resources/ehcache.xml`:

```xml
<cache alias="evaluacionesByCurso">
    <expiry>
        <ttl unit="minutes">10</ttl>
    </expiry>
    <resources>
        <heap unit="entries">1000</heap>
    </resources>
</cache>
```

**Parámetros**:
- `ttl`: Time To Live (validez del caché)
- `heap unit="entries"`: Máximo de elementos en caché

---

## 4. Ejemplos Completos de Curl

### Búsqueda Múltiple con Paginación
```bash
curl -X GET "http://localhost:8080/api/evaluaciones?tipo=Examen&estado=Activa&page=0&size=5&sort=nombre&direction=ASC" \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### Buscar Evaluación por Nombre
```bash
curl -X GET "http://localhost:8080/api/evaluaciones?nombre=parcial&page=0&size=10" \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### Obtener Evaluaciones Activas (Cacheado)
```bash
curl -X GET "http://localhost:8080/api/evaluaciones/estado/activas" \
  -H "Authorization: Bearer <tu-token-jwt>"
```

### Crear Nueva Evaluación (Invalida Caché)
```bash
curl -X POST "http://localhost:8080/api/evaluaciones" \
  -H "Authorization: Bearer <tu-token-jwt>" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Nuevo Examen",
    "tipo": "Examen",
    "estado": "Programada",
    "cursoId": 1,
    "profesorId": 1,
    "duracionMinutos": 90
  }'
```

### Manejo de Rate Limit
```bash
# Request que excede límite
curl -X GET "http://localhost:8080/api/evaluaciones" \
  -H "Authorization: Bearer <tu-token-jwt>"

# Respuesta:
HTTP/1.1 429 Too Many Requests
X-Rate-Limit-Retry-After-Seconds: 60
"Rate limit exceeded. Maximum 100 requests per minute."

# Reintentar después de 60 segundos
```

---

## 5. Testing

### Tests Incluidos

1. **EvaluacionControllerAdvancedFilterTest.java**
   - 13 test cases covering filtrado, caché, paginación
   - ejemplos: filtro por estado, tipo, búsqueda, múltiples filtros

2. **JdbcQueryBuilderAdvancedFilterTest.java**
   - 12 test cases para operadores (LIKE, BETWEEN, IN, etc.)
   - validación de SQL injection

### Ejecutar Tests

```bash
mvn clean test -Dtest=EvaluacionControllerAdvancedFilterTest
mvn clean test -Dtest=JdbcQueryBuilderAdvancedFilterTest
```

---

## 6. Notas de Seguridad

1. **SQL Injection**: Los nombres de columna se validan con regex `^[a-zA-Z0-9_\.]+$`
2. **Parámetros PreparedStatement**: Todos los valores se parametrizan
3. **Rate Limiting**: Previene ataques de fuerza bruta y DDoS
4. **Caché**: Solo afecta lecturas, no altera integridad de datos

---

## 7. Pasos Siguientes

1. ✅ Filtrado avanzado implementado
2. ✅ Rate Limiting implementado
3. ✅ Caché implementado
4. ⏳ Refresh tokens y JWT mejorado
5. ⏳ Documentación API (Swagger)
6. ⏳ CI/CD y deployment automático

---

**Última actualización**: Febrero 26, 2026  
**Versión**: 2.0 (Con filtrado avanzado, rate limiting y caché)
