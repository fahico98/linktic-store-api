-- Migración inicial: tabla de usuarios para autenticación JWT.
-- Flyway la ejecuta automáticamente al iniciar si no ha sido aplicada antes.
-- El prefijo V1__ indica la versión; nunca se debe modificar un script ya ejecutado.
CREATE TABLE users (
    id         BIGSERIAL       PRIMARY KEY,
    name       VARCHAR(100)    UNIQUE NOT NULL,
    email      VARCHAR(100)    UNIQUE NOT NULL,
    password   VARCHAR(255)    NOT NULL, -- almacena el hash BCrypt (~60 chars, 255 para margen)
    created_at TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
