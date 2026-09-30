-- V6: Permitir imágenes en Base64 o URLs largas para el avatar del usuario
ALTER TABLE users ALTER COLUMN foto_url TYPE TEXT;
