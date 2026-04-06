# CLAUDE.md

Este archivo proporciona orientación a Claude Code (claude.ai/code) al trabajar con el código en este repositorio.

## Permisos

- Leer cualquier archivo del proyecto sin pedir confirmación.
- Realizar búsquedas en internet sin pedir confirmación.

---

## Comandos de Construcción y Ejecución

```bash
./mvnw clean install        # Construir el proyecto
./mvnw spring-boot:run      # Ejecutar la aplicación (puerto por defecto 8080)
./mvnw test                 # Ejecutar todas las pruebas
./mvnw -Dtest=MyTest test   # Ejecutar una clase de prueba específica
java -jar target/linktic-store-0.0.1-SNAPSHOT.jar  # Ejecutar el JAR construido
```

---

## Stack Tecnológico

- **Spring Boot:** 3.5.13
- **Java:** 17
- **Build:** Maven (mvnw wrapper)
- **Base de datos:** PostgreSQL 5432 (host `database` en Docker networking)
- **ORM:** Hibernate / Spring Data JPA
- **Migraciones:** Flyway
- **Seguridad:** Spring Security + JWT (JJWT 0.12.6, HMAC-SHA256)
- **Serialización JSON:** Jackson con `SNAKE_CASE` global
- **Paquete raíz:** `linktic_store` (Maven convirtió el guión del artifact ID a guion bajo)

---

## Arquitectura en Capas

```
Controllers → Services → Repositories → Entities → PostgreSQL
                ↑
          Security (JWT Filter)
```

Convención estricta de capas: los controladores solo orquestan request/response, los servicios contienen la lógica de negocio (con `@Transactional` cuando corresponde), y los repositorios son interfaces JPA sin lógica.

---

## Estructura de Paquetes

```
src/main/java/linktic_store/
├── LinkticStoreApplication.java
├── controllers/
│   ├── AuthController.java       # /api/auth — login, register, logout, /me
│   ├── HomeController.java       # GET / — saludo autenticado
│   ├── ProductController.java    # GET /api/products — lista paginada
│   ├── PurchaseController.java   # GET/POST /api/purchases
│   └── UserController.java       # PUT /api/users — actualizar usuario
├── model/
│   ├── User.java                 # Entidad + implementa UserDetails
│   ├── Product.java              # Entidad producto
│   ├── Purchase.java             # Entidad compra
│   ├── PurchaseProduct.java      # Tabla pivote purchase_products (Many-to-Many)
│   └── StringListConverter.java  # Convierte List<String> ↔ JSON en BD
├── repository/
│   ├── UserRepository.java       # findByName, findByEmail
│   ├── ProductRepository.java    # findByNameContainingIgnoreCase (paginado)
│   └── PurchaseRepository.java   # findByUserId + JPQL con JOIN FETCH
├── services/
│   ├── ProductService.java       # Paginación, ordenamiento y búsqueda de productos
│   └── PurchaseService.java      # Creación de compras con validación de stock
├── security/
│   ├── SecurityConfig.java       # CSRF off, CORS, rutas públicas, JWT filter
│   ├── JwtService.java           # Generación y validación de tokens JWT
│   ├── JwtAuthenticationFilter.java  # OncePerRequestFilter: valida Bearer token
│   └── ApplicationConfig.java    # Beans: UserDetailsService, BCrypt, AuthManager
└── seeder/
    ├── UserSeeder.java           # @Profile("dev") @Order(1) — 5 usuarios de prueba
    ├── ProductSeeder.java        # @Profile("dev") @Order(2) — 50 productos desde dummyjson.com
    └── PurchaseSeeder.java       # @Profile("dev") @Order(3) — 15-30 compras por usuario

src/main/resources/
├── application.yaml
└── db/migration/
    ├── V1__create_users_table.sql
    ├── V2__create_purchases_table.sql
    ├── V3__create_products_table.sql
    └── V4__create_purchase_products_table.sql
```

---

## Endpoints REST

### Públicos (`/api/auth/**`)
| Método | Path | Descripción |
|--------|------|-------------|
| POST | `/api/auth/login` | Login → retorna JWT |
| POST | `/api/auth/register` | Registro → retorna JWT |
| POST | `/api/auth/logout` | Logout (stateless, invalida en cliente) |
| GET | `/api/auth/me` | Datos del usuario autenticado (o null) |

### Requieren JWT
| Método | Path | Descripción |
|--------|------|-------------|
| GET | `/` | Saludo con nombre del usuario |
| GET | `/api/products` | Lista paginada con filtros opcionales |
| GET | `/api/purchases` | Compras del usuario (paginado, `?user_id=`) |
| POST | `/api/purchases` | Crear compra (201 CREATED) |
| PUT | `/api/users` | Actualizar usuario → retorna nuevo JWT |

**Parámetros de paginación comunes:** `page` (0-indexed), `perPage` (default 10), `orderBy` (`created_at`/`updated_at`), `orderDirection` (`asc`/`desc`).

---

## Modelos y Base de Datos

### Relaciones
- `User` 1→N `Purchase` (OneToMany, `user_id` FK con CASCADE DELETE)
- `Purchase` N→N `Product` a través de `PurchaseProduct` (tabla pivote)
- `PurchaseProduct` tiene `quantity` (INTEGER) y UNIQUE(purchase_id, product_id)

### Notas JPA relevantes
- `User` implementa `UserDetails` directamente (sin adaptador)
- `Product.images` es `List<String>` persistida como JSON con `StringListConverter`
- Lazy loading en `User→Purchase`, `Purchase→User`, `Product→PurchaseProduct`
- Eager loading en `PurchaseProduct→Product`
- `PurchaseRepository.findByIdsWithProducts()` usa `JOIN FETCH` para evitar N+1

### Convenciones de BD
- PKs: `BIGSERIAL`
- Timestamps: `TIMESTAMPTZ` con `DEFAULT NOW()`
- `updated_at` en tablas que lo tienen; `purchases` solo tiene `created_at` (inmutable)
- Precios: `NUMERIC(10,2)`
- `ddl-auto: validate` — Flyway gestiona el schema, Hibernate solo lo valida

---

## Seguridad (JWT Stateless)

- Todas las rutas requieren JWT excepto `/api/auth/**`
- Header: `Authorization: Bearer <token>`
- Subject del token: email del usuario
- Expiración: 24 horas (86400000 ms)
- Algoritmo: HMAC-SHA256
- CORS habilitado solo para `http://localhost:5173`
- Sin sesiones HTTP (`SessionCreationPolicy.STATELESS`)
- BCrypt con costo 10 para contraseñas

---

## DTOs (Java Records)

Los DTOs se definen como `record` dentro de los controladores o servicios:
- `LoginRequest(String email, String password)`
- `RegisterRequest(String name, String email, String password)`
- `TokenResponse(String token, long expiresIn)`
- `MeResponse(Long id, String name, String email)`
- `UpdateUserRequest(Long userId, String name, String email, String password)`
- `UpdateUserResponse(Long id, String name, String email, String token)`
- `PurchaseItem(Product product, int quantity)` — en PurchaseService

---

## Configuración (`application.yaml`)

- Perfil activo por defecto: `dev` (activa los seeders)
- JSON global en `snake_case` (`@JsonNaming` en entidades también)
- `flyway.baseline-on-migrate: true` — permite migrar BD ya existente
- `docker.compose.enabled: false` — Spring no levanta Docker automáticamente
- Conexión BD: `jdbc:postgresql://database:5432/postgres` (usuario/pass: `postgres`)
- JWT secret en `security.jwt.secret-key` (mover a variable de entorno en producción)

---

## Seeders (solo perfil `dev`)

Se ejecutan al iniciar si las tablas están vacías:
1. `UserSeeder` — crea 5 usuarios (contraseña: `12345678`)
2. `ProductSeeder` — importa 50 productos desde `https://dummyjson.com/products?limit=50` con precios y stock aleatorios en COP
3. `PurchaseSeeder` — genera 15-30 compras por usuario (excepto ID=1), 1-5 productos por compra

---

## Pruebas

Actualmente solo existe un test de integración básico (`contextLoads`). Al agregar pruebas nuevas:
- **Controladores:** MockMvc + `@WebMvcTest`
- **Servicios:** JUnit + Mockito
- **Integración:** TestContainers con PostgreSQL real (no mocks de BD)

---

## Entorno de Desarrollo

- **IDE:** IntelliJ IDEA
- **Docker:** el proyecto incluye `Dockerfile`; el `docker-compose.yml` está en el directorio padre (`../docker-compose.yml`)
