-- Previene calificaciones duplicadas para la misma pregunta dentro de la misma submission.
-- Sin este constraint un maestro podía calificar la misma pregunta dos veces,
-- corrompiendo el cálculo de puntuación total.

ALTER TABLE calificaciones
    ADD CONSTRAINT uq_cal_sub_preg UNIQUE(submission_id, pregunta_id);
