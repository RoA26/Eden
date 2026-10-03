-- =====================================================================
-- V3: movimientos recurrentes, presupuestos mensuales, metas de ahorro
--     y acceso compartido de solo lectura.
-- =====================================================================

-- Gastos o ingresos que se repiten (arriendo, SOAT, salario...). Eden los
-- recuerda y el usuario los confirma: nunca se registran solos.
CREATE TABLE recurrente (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario        BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre            VARCHAR(60)   NOT NULL,
    tipo              VARCHAR(10)   NOT NULL CHECK (tipo IN ('INGRESO', 'GASTO')),
    monto             NUMERIC(14,2) NOT NULL CHECK (monto > 0),
    id_categoria      BIGINT        NOT NULL REFERENCES categoria (id),
    id_fuente         BIGINT        REFERENCES fuente_ingreso (id),
    id_cajita_origen  BIGINT        REFERENCES cajita (id),
    periodicidad      VARCHAR(10)   NOT NULL CHECK (periodicidad IN ('SEMANAL', 'QUINCENAL', 'MENSUAL', 'ANUAL')),
    dia_ancla         SMALLINT      NOT NULL CHECK (dia_ancla BETWEEN 1 AND 31),
    proxima_fecha     DATE          NOT NULL,
    dias_aviso        SMALLINT      NOT NULL DEFAULT 3 CHECK (dias_aviso BETWEEN 0 AND 30),
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_recurrente_usuario_nombre UNIQUE (id_usuario, nombre)
);
CREATE INDEX ix_recurrente_pendientes ON recurrente (id_usuario, proxima_fecha) WHERE activo;

-- Tope mensual por categoria de gasto o costo.
CREATE TABLE presupuesto (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    id_categoria    BIGINT        NOT NULL REFERENCES categoria (id) ON DELETE CASCADE,
    monto_mensual   NUMERIC(14,2) NOT NULL CHECK (monto_mensual > 0),
    CONSTRAINT uq_presupuesto_usuario_categoria UNIQUE (id_usuario, id_categoria)
);

-- Meta de ahorro vinculada a una cajita: el progreso es el saldo de la cajita.
CREATE TABLE meta (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario       BIGINT        NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    nombre           VARCHAR(60)   NOT NULL,
    monto_objetivo   NUMERIC(14,2) NOT NULL CHECK (monto_objetivo > 0),
    fecha_objetivo   DATE,
    id_cajita        BIGINT        NOT NULL REFERENCES cajita (id),
    activa           BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion   TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CONSTRAINT uq_meta_usuario_nombre UNIQUE (id_usuario, nombre)
);

-- El titular concede a otro usuario permiso de SOLO LECTURA sobre su cuenta.
CREATE TABLE acceso_compartido (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_titular        BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    id_invitado       BIGINT      NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    fecha_concesion   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_acceso_titular_invitado UNIQUE (id_titular, id_invitado),
    CONSTRAINT ck_acceso_no_a_si_mismo CHECK (id_titular <> id_invitado)
);
CREATE INDEX ix_acceso_invitado ON acceso_compartido (id_invitado);
