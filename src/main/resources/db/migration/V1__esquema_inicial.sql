-- =====================================================================
-- V1: usuarios, categorias, fuentes de ingreso y movimientos.
-- Cajitas, recurrentes, presupuestos, metas y acceso compartido llegan
-- en migraciones posteriores (V2, V3, ...). Nunca se edita una migracion
-- ya aplicada: cualquier cambio va en un archivo nuevo.
-- =====================================================================

CREATE TABLE usuario (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre_usuario   VARCHAR(30)  NOT NULL UNIQUE,
    nombre_completo  VARCHAR(100) NOT NULL,
    contrasena_hash  VARCHAR(100) NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE categoria (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario  BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre      VARCHAR(50) NOT NULL,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('INGRESO', 'GASTO', 'COSTO_OPERATIVO')),
    activa      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_categoria_usuario_tipo_nombre UNIQUE (id_usuario, tipo, nombre)
);

CREATE TABLE fuente_ingreso (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre          VARCHAR(60) NOT NULL,
    frecuencia      VARCHAR(12) NOT NULL CHECK (frecuencia IN ('DIARIA', 'SEMANAL', 'QUINCENAL', 'MENSUAL')),
    activa          BOOLEAN     NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_fuente_usuario_nombre UNIQUE (id_usuario, nombre)
);

CREATE TABLE movimiento (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    tipo            VARCHAR(15)   NOT NULL CHECK (tipo IN ('INGRESO', 'GASTO')),
    fecha           DATE          NOT NULL,
    monto           NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    id_categoria    BIGINT        NOT NULL REFERENCES categoria (id),
    id_fuente       BIGINT        REFERENCES fuente_ingreso (id),
    descripcion     VARCHAR(200),
    fecha_creacion  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_movimiento_usuario_fecha ON movimiento (id_usuario, fecha DESC);
CREATE INDEX ix_movimiento_fuente ON movimiento (id_fuente) WHERE id_fuente IS NOT NULL;

-- RN-01: un solo total de ingreso por fuente y por dia.
CREATE UNIQUE INDEX uq_ingreso_diario_por_fuente
    ON movimiento (id_usuario, id_fuente, fecha)
    WHERE tipo = 'INGRESO' AND id_fuente IS NOT NULL;
