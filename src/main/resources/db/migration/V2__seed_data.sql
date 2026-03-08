-- QuimbayaEVAL Seed Data
-- Datos de prueba para desarrollo y testing

-- Insertar usuarios de prueba
-- Contraseña para todos: "password123" (hasheada con BCrypt)
-- Hash generado: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (name, email, password, role, active) VALUES
('Profesor Demo', 'profesor@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maestro', true),
('Ana Martínez', 'ana.martinez@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'maestro', true),
('Estudiante Demo', 'estudiante@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true),
('María García', 'maria.garcia@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true),
('Juan Pérez', 'juan.perez@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true),
('Carlos López', 'carlos.lopez@quimbaya.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'estudiante', true);

-- Insertar cursos de ejemplo
INSERT INTO cursos (codigo, nombre, descripcion, profesor_id) VALUES
('MAT101', 'Matemáticas Básicas', 'Curso introductorio de matemáticas que cubre álgebra, geometría y trigonometría básica', 1),
('FIS101', 'Física I', 'Fundamentos de física mecánica: cinemática, dinámica, trabajo y energía', 1),
('PROG101', 'Programación I', 'Introducción a la programación con Java: variables, estructuras de control, POO', 2),
('BD101', 'Bases de Datos', 'Diseño y gestión de bases de datos relacionales con SQL', 2);

-- Insertar evaluaciones de ejemplo
INSERT INTO evaluaciones (nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada) VALUES
('Parcial 1 - Álgebra', 'Primera evaluación de álgebra lineal y ecuaciones', 1, 1, 'Examen', 'Activa', CURRENT_TIMESTAMP + INTERVAL '7 days', 90, 1, true),
('Quiz 1 - Geometría', 'Quiz corto sobre geometría plana', 1, 1, 'Quiz', 'Activa', CURRENT_TIMESTAMP + INTERVAL '3 days', 30, 2, true),
('Taller Cinemática', 'Taller práctico de cinemática y movimiento', 2, 1, 'Taller', 'Programada', CURRENT_TIMESTAMP + INTERVAL '10 days', 120, 3, false),
('Proyecto Final Java', 'Desarrollo de aplicación CRUD con Java', 3, 2, 'Proyecto', 'Programada', CURRENT_TIMESTAMP + INTERVAL '30 days', 0, 1, true),
('Quiz SQL Básico', 'Evaluación de consultas SQL básicas', 4, 2, 'Quiz', 'Activa', CURRENT_TIMESTAMP + INTERVAL '2 days', 45, 2, true);

-- Insertar preguntas para Parcial 1 - Álgebra
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(1, '¿Cuál es el resultado de 2x + 3 = 11?', 'seleccion_multiple', 2.0, 1, 
 '[{"id":1,"texto":"x = 3","correcta":false},{"id":2,"texto":"x = 4","correcta":true},{"id":3,"texto":"x = 5","correcta":false},{"id":4,"texto":"x = 6","correcta":false}]',
 '{"respuesta":"x = 4"}'),
(1, '¿Es verdadero que (a + b)² = a² + b²?', 'verdadero_falso', 1.5, 2,
 '[{"id":1,"texto":"Verdadero","correcta":false},{"id":2,"texto":"Falso","correcta":true}]',
 '{"respuesta":"Falso"}'),
(1, 'Resuelve la ecuación: 3x - 7 = 2x + 5', 'respuesta_corta', 2.5, 3,
 NULL,
 '{"respuesta":"x = 12"}'),
(1, 'Explica el método de sustitución para resolver sistemas de ecuaciones', 'ensayo', 4.0, 4,
 NULL,
 NULL);

-- Insertar preguntas para Quiz 1 - Geometría
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(2, '¿Cuántos grados tiene un triángulo?', 'seleccion_multiple', 1.0, 1,
 '[{"id":1,"texto":"90°","correcta":false},{"id":2,"texto":"180°","correcta":true},{"id":3,"texto":"270°","correcta":false},{"id":4,"texto":"360°","correcta":false}]',
 '{"respuesta":"180°"}'),
(2, 'El área de un círculo se calcula con πr²', 'verdadero_falso', 1.0, 2,
 '[{"id":1,"texto":"Verdadero","correcta":true},{"id":2,"texto":"Falso","correcta":false}]',
 '{"respuesta":"Verdadero"}'),
(2, 'Calcula el perímetro de un rectángulo de 5cm x 3cm', 'respuesta_corta', 2.0, 3,
 NULL,
 '{"respuesta":"16 cm"}');

-- Insertar preguntas para Quiz SQL Básico
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(5, '¿Qué comando SQL se usa para recuperar datos?', 'seleccion_multiple', 1.0, 1,
 '[{"id":1,"texto":"GET","correcta":false},{"id":2,"texto":"SELECT","correcta":true},{"id":3,"texto":"FETCH","correcta":false},{"id":4,"texto":"RETRIEVE","correcta":false}]',
 '{"respuesta":"SELECT"}'),
(5, 'La cláusula WHERE se usa para filtrar resultados', 'verdadero_falso', 1.0, 2,
 '[{"id":1,"texto":"Verdadero","correcta":true},{"id":2,"texto":"Falso","correcta":false}]',
 '{"respuesta":"Verdadero"}'),
(5, 'Escribe una consulta para obtener todos los usuarios de la tabla users', 'respuesta_corta', 2.0, 3,
 NULL,
 '{"respuesta":"SELECT * FROM users"}');

-- Insertar submissions de ejemplo (entregas de estudiantes)
INSERT INTO submissions (evaluacion_id, estudiante_id, respuestas_json, estado, intento_numero, fecha_inicio, fecha_envio) VALUES
(1, 3, '[{"pregunta_id":1,"respuesta":"x = 4"},{"pregunta_id":2,"respuesta":"Falso"}]', 'Enviada', 1, CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 4, '[{"pregunta_id":5,"respuesta":"180°"},{"pregunta_id":6,"respuesta":"Verdadero"},{"pregunta_id":7,"respuesta":"16 cm"}]', 'Calificada', 1, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '23 hours'),
(2, 5, '[{"pregunta_id":5,"respuesta":"180°"},{"pregunta_id":6,"respuesta":"Verdadero"}]', 'Enviada', 1, CURRENT_TIMESTAMP - INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours');

-- Insertar calificaciones de ejemplo
INSERT INTO calificaciones (submission_id, puntuacion_obtenida, puntuacion_maxima, retroalimentacion, calificado_por_id, fecha_calificacion) VALUES
(2, 4.0, 4.0, 'Excelente trabajo. Todas las respuestas correctas.', 1, CURRENT_TIMESTAMP - INTERVAL '22 hours');

-- Insertar PQRS de ejemplo
INSERT INTO pqrs (tipo, asunto, descripcion, estado, usuario_id, curso_id, respuesta, respondido_por_id, fecha_creacion, fecha_respuesta) VALUES
('Petición', 'Solicitud de extensión de plazo', 'Solicito una extensión de 2 días para el Parcial 1 debido a problemas de salud', 'Resuelta', 3, 1, 'Se ha aprobado la extensión solicitada. Nuevo plazo: 5 días adicionales.', 1, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('Queja', 'Problema con acceso a evaluación', 'No puedo acceder al Quiz 1 de Geometría, aparece error 500', 'En Proceso', 4, 1, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day', NULL),
('Sugerencia', 'Mejorar interfaz de evaluaciones', 'Sería útil tener un contador de tiempo más visible durante las evaluaciones', 'Pendiente', 5, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', NULL),
('Reclamo', 'Calificación incorrecta', 'Considero que mi respuesta en la pregunta 3 del parcial es correcta y debería tener más puntos', 'Resuelta', 3, 1, 'Se ha revisado tu respuesta y se ha ajustado la calificación. Puntuación actualizada.', 1, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day');

