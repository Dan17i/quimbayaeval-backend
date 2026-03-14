-- Agrega foto de perfil a usuarios
ALTER TABLE users ADD COLUMN IF NOT EXISTS foto_url VARCHAR(500);
