-- Password recovery: the token now expires. usuario.password_token keeps the SHA-256 of the token
-- (never the token itself) and password_token_vence says until when it is valid.
-- Run once on every existing database:
--   sudo -u postgres psql -d sysfact -f migracion_usuario_password_token_vence.sql
ALTER TABLE public.usuario ADD COLUMN IF NOT EXISTS password_token_vence timestamp with time zone NULL;

-- Tokens created before this change have no expiry: they are discarded.
UPDATE public.usuario SET password_token = NULL WHERE password_token IS NOT NULL;
