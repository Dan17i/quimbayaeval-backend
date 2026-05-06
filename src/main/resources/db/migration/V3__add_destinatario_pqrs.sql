-- Agrega enrutamiento de PQRS por destinatario:
--   'maestro'      → asunto académico, visible para el maestro del curso y el coordinador
--   'coordinador'  → queja sobre el docente, visible solo para el coordinador

ALTER TABLE pqrs
    ADD COLUMN IF NOT EXISTS destinatario VARCHAR(20)
        NOT NULL DEFAULT 'maestro'
        CHECK (destinatario IN ('maestro', 'coordinador'));

CREATE INDEX IF NOT EXISTS idx_pqrs_destinatario ON pqrs(destinatario);
