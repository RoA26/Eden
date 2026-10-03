-- =====================================================================
-- V2: cajitas de ahorro y nuevos tipos de movimiento.
--   APORTE        disponible -> cajita
--   RETIRO        cajita -> disponible (o pago de un gasto, via id_relacionado)
--   RENDIMIENTO   intereses que genera la cajita
--   SALDO_INICIAL dinero que la cajita ya tenia antes de usar Eden
-- =====================================================================

CREATE TABLE cajita (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT       NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre          VARCHAR(40)  NOT NULL,
    proposito       VARCHAR(12)  NOT NULL CHECK (proposito IN ('CAPITAL', 'COMPRAS', 'EMERGENCIA', 'LIBRE')),
    porcentaje      NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (porcentaje >= 0 AND porcentaje <= 100),
    es_resto        BOOLEAN      NOT NULL DEFAULT FALSE,
    activa          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_cajita_usuario_nombre UNIQUE (id_usuario, nombre)
);

-- RN-04: solo una cajita "resto" activa por usuario.
CREATE UNIQUE INDEX uq_cajita_resto_por_usuario ON cajita (id_usuario) WHERE es_resto AND activa;

ALTER TABLE movimiento ADD COLUMN id_cajita BIGINT REFERENCES cajita (id);
-- Un RETIRO que paga un gasto apunta a ese gasto; si el gasto se elimina, el retiro tambien.
ALTER TABLE movimiento ADD COLUMN id_relacionado BIGINT REFERENCES movimiento (id) ON DELETE CASCADE;
ALTER TABLE movimiento ALTER COLUMN id_categoria DROP NOT NULL;

ALTER TABLE movimiento DROP CONSTRAINT IF EXISTS movimiento_tipo_check;
ALTER TABLE movimiento ADD CONSTRAINT ck_movimiento_tipo
    CHECK (tipo IN ('INGRESO', 'GASTO', 'APORTE', 'RETIRO', 'RENDIMIENTO', 'SALDO_INICIAL'));

-- Ingresos y gastos llevan categoria; las operaciones de cajita llevan cajita.
ALTER TABLE movimiento ADD CONSTRAINT ck_movimiento_coherencia CHECK (
    (tipo IN ('INGRESO', 'GASTO') AND id_categoria IS NOT NULL AND id_cajita IS NULL)
    OR
    (tipo IN ('APORTE', 'RETIRO', 'RENDIMIENTO', 'SALDO_INICIAL') AND id_cajita IS NOT NULL AND id_categoria IS NULL)
);

CREATE INDEX ix_movimiento_cajita ON movimiento (id_cajita) WHERE id_cajita IS NOT NULL;
CREATE INDEX ix_movimiento_relacionado ON movimiento (id_relacionado) WHERE id_relacionado IS NOT NULL;
