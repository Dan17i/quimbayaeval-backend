-- Elimina el UNIQUE(evaluacion_id, estudiante_id) de submissions.
-- Ese constraint impide múltiples intentos cuando intentos_permitidos > 1.
-- El control de intentos se maneja ahora en la capa de servicio.

ALTER TABLE submissions
    DROP CONSTRAINT IF EXISTS submissions_evaluacion_id_estudiante_id_key;
