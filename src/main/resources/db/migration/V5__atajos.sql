-- =====================================================================
-- V5: botones rapidos ("atajos") del tablero. Cada uno es una plantilla
-- de registro (ej. "Moto $5.000", "Venta postre") que se registra con un
-- solo toque, sin formulario.
-- =====================================================================

CREATE TABLE atajo (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre          VARCHAR(30)   NOT NULL,
    tipo            VARCHAR(10)   NOT NULL CHECK (tipo IN ('INGRESO', 'GASTO')),
    monto           NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    id_categoria    BIGINT        NOT NULL REFERENCES categoria (id),
    id_fuente       BIGINT        REFERENCES fuente_ingreso (id),
    fecha_creacion  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_atajo_usuario_nombre UNIQUE (id_usuario, nombre)
);
CREATE INDEX ix_atajo_usuario ON atajo (id_usuario, id);
