-- Schema para QuimbayaEVAL Backend
-- PostgreSQL

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('estudiante', 'maestro', 'coordinador')),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Cursos
CREATE TABLE IF NOT EXISTS cursos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    profesor_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Inscripciones
CREATE TABLE IF NOT EXISTS inscripciones (
    id SERIAL PRIMARY KEY,
    estudiante_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    curso_id INTEGER NOT NULL REFERENCES cursos(id) ON DELETE CASCADE,
    fecha_inscripcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(estudiante_id, curso_id)
);

-- Tabla de Evaluaciones
CREATE TABLE IF NOT EXISTS evaluaciones (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    curso_id INTEGER NOT NULL REFERENCES cursos(id) ON DELETE CASCADE,
    profesor_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Examen', 'Quiz', 'Taller', 'Proyecto', 'Tarea')),
    estado VARCHAR(50) NOT NULL DEFAULT 'Borrador' CHECK (estado IN ('Borrador', 'Programada', 'Activa', 'Cerrada')),
    deadline TIMESTAMP,
    duracion_minutos INTEGER DEFAULT 60,
    intentos_permitidos INTEGER DEFAULT 1,
    publicada BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Tabla de Preguntas
CREATE TABLE IF NOT EXISTS preguntas (
    id SERIAL PRIMARY KEY,
    evaluacion_id INTEGER NOT NULL REFERENCES evaluaciones(id) ON DELETE CASCADE,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('seleccion_multiple', 'verdadero_falso', 'respuesta_corta', 'ensayo')),
    puntuacion DECIMAL(5, 2) DEFAULT 1.0,
    orden INTEGER,
    opciones_json JSONB,
    respuesta_correcta_json JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Submissions
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    evaluacion_id INTEGER NOT NULL REFERENCES evaluaciones(id) ON DELETE CASCADE,
    estudiante_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_envio TIMESTAMP,
    respuestas_json JSONB,
    intento_numero INTEGER DEFAULT 1,
    estado VARCHAR(50) NOT NULL DEFAULT 'Borrador' CHECK (estado IN ('Borrador', 'Enviada', 'Calificada')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(evaluacion_id, estudiante_id)
);

-- Tabla de Respuestas de Preguntas
CREATE TABLE IF NOT EXISTS respuestas_preguntas (
    id SERIAL PRIMARY KEY,
    submission_id INTEGER NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    pregunta_id INTEGER NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
    respuesta_texto TEXT,
    respuesta_json JSONB,
    completada BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Calificaciones
CREATE TABLE IF NOT EXISTS calificaciones (
    id SERIAL PRIMARY KEY,
    submission_id INTEGER NOT NULL REFERENCES submissions(id) ON DELETE CASCADE,
    pregunta_id INTEGER NOT NULL REFERENCES preguntas(id) ON DELETE CASCADE,
    puntuacion_obtenida DECIMAL(5, 2),
    puntuacion_maxima DECIMAL(5, 2),
    retroalimentacion TEXT,
    calificado_por_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    fecha_calificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Resultados
CREATE TABLE IF NOT EXISTS resultados (
    id SERIAL PRIMARY KEY,
    submission_id INTEGER NOT NULL UNIQUE REFERENCES submissions(id) ON DELETE CASCADE,
    puntuacion_total DECIMAL(5, 2),
    puntuacion_maxima DECIMAL(5, 2),
    porcentaje DECIMAL(5, 2),
    estado_aprobacion VARCHAR(50),
    observaciones TEXT,
    fecha_resultado TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de PQRS
CREATE TABLE IF NOT EXISTS pqrs (
    id SERIAL PRIMARY KEY,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Petición', 'Queja', 'Reclamo', 'Sugerencia')),
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    curso_id INTEGER REFERENCES cursos(id) ON DELETE SET NULL,
    usuario_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente', 'En Proceso', 'Resuelta', 'Cerrada')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    respuesta TEXT,
    respondido_por_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


-- Índices
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_cursos_profesor ON cursos(profesor_id);
CREATE INDEX IF NOT EXISTS idx_inscripciones_estudiante ON inscripciones(estudiante_id);
CREATE INDEX IF NOT EXISTS idx_inscripciones_curso ON inscripciones(curso_id);
CREATE INDEX IF NOT EXISTS idx_evaluaciones_curso ON evaluaciones(curso_id);
CREATE INDEX IF NOT EXISTS idx_evaluaciones_profesor ON evaluaciones(profesor_id);
CREATE INDEX IF NOT EXISTS idx_evaluaciones_estado ON evaluaciones(estado);
CREATE INDEX IF NOT EXISTS idx_preguntas_evaluacion ON preguntas(evaluacion_id);
CREATE INDEX IF NOT EXISTS idx_submissions_evaluacion ON submissions(evaluacion_id);
CREATE INDEX IF NOT EXISTS idx_submissions_estudiante ON submissions(estudiante_id);
CREATE INDEX IF NOT EXISTS idx_submissions_estado ON submissions(estado);
CREATE INDEX IF NOT EXISTS idx_respuestas_submission ON respuestas_preguntas(submission_id);
CREATE INDEX IF NOT EXISTS idx_respuestas_pregunta ON respuestas_preguntas(pregunta_id);
CREATE INDEX IF NOT EXISTS idx_calificaciones_submission ON calificaciones(submission_id);
CREATE INDEX IF NOT EXISTS idx_pqrs_usuario ON pqrs(usuario_id);
CREATE INDEX IF NOT EXISTS idx_pqrs_curso ON pqrs(curso_id);
CREATE INDEX IF NOT EXISTS idx_pqrs_estado ON pqrs(estado);

-- =============================================
-- SEED DATA
-- =============================================

-- Usuarios (password: "password" con BCrypt)
INSERT INTO users (name, email, password, role, active) VALUES
('Admin Sistema',   'admin@quimbaya.edu.co',          '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'coordinador', true),
('María Profesora', 'profesor@quimbaya.edu.co',        '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'maestro',      true),
('Ana Martínez',    'ana.martinez@quimbaya.edu.co',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'maestro',      true),
('Juan Estudiante', 'estudiante@quimbaya.edu.co',      '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'estudiante',   true),
('María García',    'maria.garcia@quimbaya.edu.co',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'estudiante',   true),
('Pedro Pérez',     'pedro.perez@quimbaya.edu.co',     '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'estudiante',   true),
('Carlos López',    'carlos.lopez@quimbaya.edu.co',    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'estudiante',   true)
ON CONFLICT DO NOTHING;

-- Cursos
INSERT INTO cursos (codigo, nombre, descripcion, profesor_id) VALUES
('MAT101',  'Matemáticas Básicas', 'Curso introductorio de matemáticas que cubre álgebra, geometría y trigonometría básica', 2),
('FIS101',  'Física I',            'Fundamentos de física mecánica: cinemática, dinámica, trabajo y energía',                2),
('PROG101', 'Programación I',      'Introducción a la programación con Java: variables, estructuras de control, POO',         3),
('BD101',   'Bases de Datos',      'Diseño y gestión de bases de datos relacionales con SQL',                                 3)
ON CONFLICT DO NOTHING;


-- Evaluaciones
INSERT INTO evaluaciones (nombre, descripcion, curso_id, profesor_id, tipo, estado, deadline, duracion_minutos, intentos_permitidos, publicada) VALUES
('Parcial 1 - Álgebra',  'Primera evaluación de álgebra lineal y ecuaciones',  1, 2, 'Examen',   'Activa',     CURRENT_TIMESTAMP + INTERVAL '7 days',  90,  1, true),
('Quiz 1 - Geometría',   'Quiz corto sobre geometría plana',                   1, 2, 'Quiz',     'Activa',     CURRENT_TIMESTAMP + INTERVAL '3 days',  30,  2, true),
('Taller Cinemática',    'Taller práctico de cinemática y movimiento',         2, 2, 'Taller',   'Programada', CURRENT_TIMESTAMP + INTERVAL '10 days', 120, 3, false),
('Proyecto Final Java',  'Desarrollo de aplicación CRUD con Java',             3, 3, 'Proyecto', 'Programada', CURRENT_TIMESTAMP + INTERVAL '30 days', 0,   1, true),
('Quiz SQL Básico',      'Evaluación de consultas SQL básicas',                4, 3, 'Quiz',     'Activa',     CURRENT_TIMESTAMP + INTERVAL '2 days',  45,  2, true)
ON CONFLICT DO NOTHING;

-- Preguntas - Parcial 1 Álgebra
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(1, '¿Cuál es el resultado de 2x + 3 = 11?', 'seleccion_multiple', 2.0, 1,
 '[{"id":1,"texto":"x = 3","correcta":false},{"id":2,"texto":"x = 4","correcta":true},{"id":3,"texto":"x = 5","correcta":false},{"id":4,"texto":"x = 6","correcta":false}]',
 '{"respuesta":"x = 4"}'),
(1, '¿Es verdadero que (a + b)² = a² + b²?', 'verdadero_falso', 1.5, 2,
 '[{"id":1,"texto":"Verdadero","correcta":false},{"id":2,"texto":"Falso","correcta":true}]',
 '{"respuesta":"Falso"}'),
(1, 'Resuelve la ecuación: 3x - 7 = 2x + 5', 'respuesta_corta', 2.5, 3, NULL, '{"respuesta":"x = 12"}'),
(1, 'Explica el método de sustitución para resolver sistemas de ecuaciones', 'ensayo', 4.0, 4, NULL, NULL)
ON CONFLICT DO NOTHING;

-- Preguntas - Quiz Geometría
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(2, '¿Cuántos grados tiene un triángulo?', 'seleccion_multiple', 1.0, 1,
 '[{"id":1,"texto":"90°","correcta":false},{"id":2,"texto":"180°","correcta":true},{"id":3,"texto":"270°","correcta":false},{"id":4,"texto":"360°","correcta":false}]',
 '{"respuesta":"180°"}'),
(2, 'El área de un círculo se calcula con πr²', 'verdadero_falso', 1.0, 2,
 '[{"id":1,"texto":"Verdadero","correcta":true},{"id":2,"texto":"Falso","correcta":false}]',
 '{"respuesta":"Verdadero"}'),
(2, 'Calcula el perímetro de un rectángulo de 5cm x 3cm', 'respuesta_corta', 2.0, 3, NULL, '{"respuesta":"16 cm"}')
ON CONFLICT DO NOTHING;

-- Preguntas - Quiz SQL
INSERT INTO preguntas (evaluacion_id, enunciado, tipo, puntuacion, orden, opciones_json, respuesta_correcta_json) VALUES
(5, '¿Qué comando SQL se usa para recuperar datos?', 'seleccion_multiple', 1.0, 1,
 '[{"id":1,"texto":"GET","correcta":false},{"id":2,"texto":"SELECT","correcta":true},{"id":3,"texto":"FETCH","correcta":false},{"id":4,"texto":"RETRIEVE","correcta":false}]',
 '{"respuesta":"SELECT"}'),
(5, 'La cláusula WHERE se usa para filtrar resultados', 'verdadero_falso', 1.0, 2,
 '[{"id":1,"texto":"Verdadero","correcta":true},{"id":2,"texto":"Falso","correcta":false}]',
 '{"respuesta":"Verdadero"}'),
(5, 'Escribe una consulta para obtener todos los usuarios de la tabla users', 'respuesta_corta', 2.0, 3, NULL, '{"respuesta":"SELECT * FROM users"}')
ON CONFLICT DO NOTHING;

-- Inscripciones
INSERT INTO inscripciones (estudiante_id, curso_id) VALUES
(4, 1), (5, 1), (6, 1), (7, 2), (4, 3), (5, 4)
ON CONFLICT DO NOTHING;

-- Submissions (con los estados y columnas correctas)
INSERT INTO submissions (evaluacion_id, estudiante_id, fecha_inicio, fecha_envio, estado) VALUES
(1, 4, CURRENT_TIMESTAMP - INTERVAL '2 hours',  CURRENT_TIMESTAMP - INTERVAL '1 hour',   'Enviada'),
(2, 5, CURRENT_TIMESTAMP - INTERVAL '1 day',    CURRENT_TIMESTAMP - INTERVAL '23 hours', 'Calificada'),
(2, 6, CURRENT_TIMESTAMP - INTERVAL '3 hours',  CURRENT_TIMESTAMP - INTERVAL '2 hours',  'Enviada'),
(5, 4, CURRENT_TIMESTAMP - INTERVAL '2 days',   CURRENT_TIMESTAMP - INTERVAL '47 hours', 'Calificada'),
(5, 5, CURRENT_TIMESTAMP - INTERVAL '2 days',   CURRENT_TIMESTAMP - INTERVAL '46 hours', 'Calificada'),
(1, 5, CURRENT_TIMESTAMP - INTERVAL '1 day',    CURRENT_TIMESTAMP - INTERVAL '20 hours', 'Enviada'),
(2, 4, CURRENT_TIMESTAMP - INTERVAL '4 days',   CURRENT_TIMESTAMP - INTERVAL '3 days',   'Calificada')
ON CONFLICT DO NOTHING;

-- Respuestas
INSERT INTO respuestas_preguntas (submission_id, pregunta_id, respuesta_texto, completada) VALUES
(1, 1, 'x = 4', true), (1, 2, 'Falso', true),
(2, 5, '180°', true),  (2, 6, 'Verdadero', true), (2, 7, '16 cm', true),
(3, 5, '180°', true),  (3, 6, 'Verdadero', true)
ON CONFLICT DO NOTHING;

-- Calificaciones (con los nombres de columna correctos)
INSERT INTO calificaciones (submission_id, pregunta_id, puntuacion_obtenida, retroalimentacion, calificado_por_id) VALUES
(2, 5, 1.0, 'Correcto',   2),
(2, 6, 1.0, 'Correcto',   2),
(2, 7, 2.0, 'Excelente',  2)
ON CONFLICT DO NOTHING;

-- Resultados
INSERT INTO resultados (submission_id, puntuacion_total, puntuacion_maxima, porcentaje, estado_aprobacion) VALUES
(2, 4.0, 4.0, 100.0, 'Aprobado'),
(4, 3.5, 4.0, 87.5,  'Aprobado'),
(5, 2.5, 4.0, 62.5,  'Aprobado'),
(7, 3.0, 4.0, 75.0,  'Aprobado')
ON CONFLICT DO NOTHING;

-- PQRS (con los estados y tipos correctos)
INSERT INTO pqrs (tipo, asunto, descripcion, estado, usuario_id, curso_id, respuesta, respondido_por_id, fecha_creacion, fecha_respuesta) VALUES
('Petición',   'Solicitud de extensión de plazo',    'Solicito una extensión de 2 días para el Parcial 1 debido a problemas de salud',                          'Resuelta',   4, 1, 'Se ha aprobado la extensión solicitada. Nuevo plazo: 5 días adicionales.', 2, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP - INTERVAL '4 days'),
('Queja',      'Problema con acceso a evaluación',   'No puedo acceder al Quiz 1 de Geometría, aparece error 500',                                               'En Proceso', 5, 1, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '1 day',  NULL),
('Sugerencia', 'Mejorar interfaz de evaluaciones',   'Sería útil tener un contador de tiempo más visible durante las evaluaciones',                              'Pendiente',  6, NULL, NULL, NULL, CURRENT_TIMESTAMP - INTERVAL '3 days', NULL),
('Reclamo',    'Calificación incorrecta',             'Considero que mi respuesta en la pregunta 3 del parcial es correcta y debería tener más puntos',           'Resuelta',   4, 1, 'Se ha revisado tu respuesta y se ha ajustado la calificación. Puntuación actualizada.', 2, CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT DO NOTHING;
