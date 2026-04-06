-- Tabla de productos de la tienda.
CREATE TABLE products (
    id              BIGSERIAL        PRIMARY KEY,
    name            VARCHAR(150)     NOT NULL,
    price           NUMERIC(10, 2)   NOT NULL,
    available_stock INTEGER          NOT NULL DEFAULT 0,
    description     TEXT,
    images          TEXT,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);
