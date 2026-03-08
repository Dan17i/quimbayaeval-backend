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

-- Tabla de Inscripciones (Estudiante-Curso)
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
    opciones_json JSONB,  -- Para almacenar opciones de selección múltiple
    respuesta_correcta_json JSONB,  -- Para almacenar respuestas correctas
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Submissions (Respuestas de estudiantes)
CREATE TABLE IF NOT EXISTS submissions (
    id SERIAL PRIMARY KEY,
    evaluacion_id INTEGER NOT NULL REFERENCES evaluaciones(id) ON DELETE CASCADE,
    estudiante_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion TIMESTAMP,
    estado VARCHAR(50) NOT NULL DEFAULT 'en_progreso' CHECK (estado IN ('en_progreso', 'completada', 'calificada')),
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
    calificacion DECIMAL(5, 2),
    comentario TEXT,
    calificada_por_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    fecha_calificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Resultados (Agregado por evaluación/estudiante)
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
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Pregunta', 'Reclamo', 'Sugerencia', 'Queja')),
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    curso_id INTEGER REFERENCES cursos(id) ON DELETE SET NULL,
    usuario_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    estado VARCHAR(50) NOT NULL DEFAULT 'Pendiente' CHECK (estado IN ('Pendiente', 'En Proceso', 'Resuelto')),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_respuesta TIMESTAMP,
    respuesta TEXT,
    respondido_por_id INTEGER REFERENCES users(id) ON DELETE SET NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para mejor performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_cursos_profesor ON cursos(profesor_id);
CREATE INDEX idx_inscripciones_estudiante ON inscripciones(estudiante_id);
CREATE INDEX idx_inscripciones_curso ON inscripciones(curso_id);
CREATE INDEX idx_evaluaciones_curso ON evaluaciones(curso_id);
CREATE INDEX idx_evaluaciones_profesor ON evaluaciones(profesor_id);
CREATE INDEX idx_evaluaciones_estado ON evaluaciones(estado);
CREATE INDEX idx_preguntas_evaluacion ON preguntas(evaluacion_id);
CREATE INDEX idx_submissions_evaluacion ON submissions(evaluacion_id);
CREATE INDEX idx_submissions_estudiante ON submissions(estudiante_id);
CREATE INDEX idx_submissions_estado ON submissions(estado);
CREATE INDEX idx_respuestas_submission ON respuestas_preguntas(submission_id);
CREATE INDEX idx_respuestas_pregunta ON respuestas_preguntas(pregunta_id);
CREATE INDEX idx_calificaciones_submission ON calificaciones(submission_id);
CREATE INDEX idx_pqrs_usuario ON pqrs(usuario_id);
CREATE INDEX idx_pqrs_curso ON pqrs(curso_id);
CREATE INDEX idx_pqrs_estado ON pqrs(estado);

-- Datos de prueba (opcional, comentar si no se necesita)
-- INSERT INTO users (name, email, password, role) VALUES
-- ('Juan Estudiante', 'juan@ejemplo.edu', '$2a$10$...', 'estudiante'),
-- ('María Maestra', 'maria@ejemplo.edu', '$2a$10$...', 'maestro'),
-- ('Carlos Coordinador', 'carlos@ejemplo.edu', '$2a$10$...', 'coordinador');
