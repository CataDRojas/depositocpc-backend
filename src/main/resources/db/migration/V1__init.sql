-- Esquema inicial del sistema de gestión de contenedores en depósito.

CREATE TABLE usuario (
    id            UUID PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol           VARCHAR(20)  NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('OPERADOR', 'ADMIN'))
);

CREATE TABLE ubicacion (
    id     UUID PRIMARY KEY,
    bloque VARCHAR(10) NOT NULL,
    fila   VARCHAR(10) NOT NULL,
    nivel  VARCHAR(10) NOT NULL,
    CONSTRAINT uq_ubicacion UNIQUE (bloque, fila, nivel)
);

CREATE TABLE contenedor (
    id                UUID PRIMARY KEY,
    numero_contenedor VARCHAR(11)  NOT NULL UNIQUE,
    tipo              VARCHAR(20)  NOT NULL,
    reefer_conectado  BOOLEAN,
    condicion         VARCHAR(10)  NOT NULL,
    estado            VARCHAR(20)  NOT NULL DEFAULT 'EN_DEPOSITO',
    fecha_ingreso     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    ubicacion_id      UUID         NOT NULL REFERENCES ubicacion (id),
    CONSTRAINT ck_contenedor_numero CHECK (numero_contenedor ~ '^[A-Z]{4}[0-9]{7}$'),
    CONSTRAINT ck_contenedor_tipo CHECK (tipo IN ('DRY', 'REEFER', 'FLAT_RACK', 'OPEN_TOP')),
    CONSTRAINT ck_contenedor_condicion CHECK (condicion IN ('LLENO', 'VACIO')),
    CONSTRAINT ck_contenedor_estado CHECK (estado IN ('EN_DEPOSITO', 'DESPACHADO'))
);

CREATE INDEX idx_contenedor_estado_fecha ON contenedor (estado, fecha_ingreso);
CREATE INDEX idx_contenedor_ubicacion ON contenedor (ubicacion_id);

CREATE TABLE movimiento (
    id               UUID PRIMARY KEY,
    contenedor_id    UUID        NOT NULL REFERENCES contenedor (id),
    ubicacion_id     UUID        REFERENCES ubicacion (id),
    tipo_movimiento  VARCHAR(20) NOT NULL,
    fecha            TIMESTAMPTZ NOT NULL DEFAULT now(),
    registrado_por   UUID        REFERENCES usuario (id),
    CONSTRAINT ck_movimiento_tipo CHECK (tipo_movimiento IN ('INGRESO', 'REUBICACION', 'SALIDA'))
);

CREATE INDEX idx_movimiento_contenedor ON movimiento (contenedor_id);
