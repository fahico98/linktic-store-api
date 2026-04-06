-- Tabla de compras.
CREATE TABLE purchases (
    id         BIGSERIAL       PRIMARY KEY,
    price      NUMERIC(10, 2)  NOT NULL,
    user_id    BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
