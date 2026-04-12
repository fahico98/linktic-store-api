# Linktic Store — Backend

API REST para una tienda en línea. Gestiona autenticación de usuarios, catálogo de productos y registro de compras.

**Stack:** Java 17 · Spring Boot 3.5 · PostgreSQL · Spring Security + JWT · Flyway

---

## Requisitos

- Docker y Docker Compose (recomendado)
- O bien: Java 17+ y una instancia de PostgreSQL corriendo localmente

---

## Levantar con Docker

El primer paso para levantar los contenedores docker que servirán toda la aplicación (backend, frontend y base de datos) es organizar el sistema de carpetas 
de todo el proyecto: Este repositorio debe ubicarse en la misma carpeta en la cual se aloja la aplicación frontend (que puedes clonar desde el repositorio 
https://github.com/fahico98/linktic-store-client) para luego mover el archivo `docker-compose.yml` a la carpeta donde están los dos proyectos:

```bash
directorio-del-proyecto/
├── docker-compose.yml
├── backend/   # este repositorio (backend)
└── frontend/  # repositorio del frontend
```

Los nombres de los directorios `frontend` y `backend` son obligatorios, si se les ponen nombres diferentes deberán hacerse las correcciones pertinentes en 
el archivo `docker-compose.yml`.

Desde la ruta de la carpeta `directorio-del-proyecto` se debe ejecutar el comando: 

```bash
docker compose up --build
```

| Servicio  | URL                        |
|-----------|----------------------------|
| Backend   | http://localhost:8080      |
| Frontend  | http://localhost:5173      |
| PostgreSQL| localhost:**5433**         |

Al iniciar en perfil `dev`, la app puebla automáticamente la base de datos con usuarios, productos y compras de prueba.

---

## Endpoints Principales

Todas las rutas excepto `/api/auth/**` requieren el header:
```
Authorization: Bearer <token>
```

### Autenticación
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/auth/register` | Registrar usuario → retorna JWT |
| POST | `/api/auth/login` | Login → retorna JWT |
| GET | `/api/auth/me` | Datos del usuario autenticado |
| POST | `/api/auth/logout` | Logout (invalida token en el cliente) |

### Productos
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/products` | Lista paginada de productos |

Query params opcionales: `page`, `perPage`, `orderBy` (`created_at`/`updated_at`), `orderDirection` (`asc`/`desc`), `searchText`.

### Compras
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/purchases?user_id=1` | Compras del usuario (paginado) |
| POST | `/api/purchases` | Crear nueva compra |

### Usuarios
| Método | Ruta | Descripción |
|--------|------|-------------|
| PUT | `/api/users` | Actualizar nombre, email o contraseña |

---

## Estructura del Proyecto

```
src/main/java/linktic_store/
├── controllers/    # Endpoints REST
├── model/          # Entidades JPA
├── repository/     # Acceso a datos (Spring Data JPA)
├── services/       # Lógica de negocio
├── security/       # JWT + Spring Security
└── seeder/         # Datos iniciales (solo perfil dev)

src/main/resources/
├── application.yaml
└── db/migration/   # Scripts SQL versionados con Flyway
```

---

## Base de Datos

El schema se gestiona con **Flyway** (migraciones versionadas en `db/migration/`). Hibernate solo valida que las entidades coincidan con el schema — no lo modifica.

Tablas: `users`, `products`, `purchases`, `purchase_products`.

---

## Datos de Prueba (perfil `dev`)

| Usuario | Email | Contraseña |
|---------|-------|------------|
| Carlos Mendez | carlos.mendez@email.com | 12345678 |
| Laura Torres | laura.torres@email.com | 12345678 |
| Andres Rojas | andres.rojas@email.com | 12345678 |
| Sofia Vargas | sofia.vargas@email.com | 12345678 |
| Miguel Castillo | miguel.castillo@email.com | 12345678 |

Los 50 productos se importan desde [dummyjson.com](https://dummyjson.com) con precios y stock aleatorios.

Autor: **Fahibram Cárcamo C.**
