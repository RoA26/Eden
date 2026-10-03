-- =====================================================================
-- RESCATE: deja la contrasena de un usuario en  Admin123
-- Hash BCrypt (costo 12) generado y verificado; Spring Security lo acepta.
-- Cambia 'TU_USUARIO' por tu nombre de usuario (en minusculas).
-- Despues de entrar, cambia esta clave con "¿Olvidaste tu contraseña?".
-- =====================================================================

-- 1) Mira que usuarios existen:
SELECT id, nombre_usuario, nombre_completo, activo FROM usuario ORDER BY id;

-- 2) Reemplaza la contrasena (debe responder: UPDATE 1):
UPDATE usuario
SET contrasena_hash = '$2a$12$KZk84GLyoI3G4YWr4jrsfel5xoYRZvGkIV3ANQPH8pZjiWUkWUOnS',
    activo = TRUE
WHERE nombre_usuario = 'TU_USUARIO';
