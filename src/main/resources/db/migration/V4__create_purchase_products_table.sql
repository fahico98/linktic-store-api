-- Tabla pivote para la relación varios a varios entre compras y productos.
CREATE TABLE purchase_products (
    id          BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT  NOT NULL REFERENCES purchases(id) ON DELETE CASCADE,
    product_id  BIGINT  NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity    INTEGER NOT NULL DEFAULT 1,
    UNIQUE (purchase_id, product_id)
);
