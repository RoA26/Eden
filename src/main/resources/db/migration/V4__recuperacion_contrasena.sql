-- =====================================================================
-- V4: recuperacion de contrasena con PIN temporal.
-- El PIN se muestra en la consola del servidor (no hay correo todavia) y
-- aqui solo se guarda su hash BCrypt, con vencimiento e intentos limitados.
-- =====================================================================

CREATE TABLE recuperacion_contrasena (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario       BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    pin_hash         VARCHAR(100) NOT NULL,
    fecha_creacion   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    expira_en        TIMESTAMPTZ  NOT NULL,
    intentos         SMALLINT     NOT NULL DEFAULT 0 CHECK (intentos >= 0),
    usada            BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX ix_recuperacion_pendiente ON recuperacion_contrasena (id_usuario, fecha_creacion DESC) WHERE NOT usada;
