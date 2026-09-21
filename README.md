# Pedidos360 - Arquitectura de Microservicios

Sistema de gestión de pedidos con arquitectura de microservicios. La autenticación y autorización centralizada en un BFF que valida el JWT emitido por el login de Microsoft Entra ID (OAuth2) y enruta las peticiones a los microservicios internos.

```
Microsoft Entra ID (Usuarios + Roles)
   │  OAuth2 (Authorization Code + PKCE)
   ▼
 [ BFF: ms-pedidos360-bff ]  ← exposición pública (puerto 8080)
   │  Valida JWT + autorización (ADMIN/USER) + CORS
   ├──► ms-pedidos360-users    (usuarios, auth OAuth2, registro/login JWT) — puerto 8083
   ├──► ms-pedidos360-catalog  (categorías y productos)                    — puerto 8081
   └──► ms-pedidos360-orders   (órdenes, reglas de negocio, stock)         — puerto 8082
               │
               └──► ms-pedidos360-catalog (valida precio y descuenta stock)
```

## Microservicios

| Servicio             | Puerto | Base de datos | Función |
|----------------------|--------|---------------|---------|
| **ms-pedidos360-bff**      | 8080 (público) | - | Validación de JWT, roles, CORS y ruteo hacia los servicios internos |
| **ms-pedidos360-users**    | 8083  | `users_db`    | Usuarios, login OAuth2 con Microsoft Entra ID y emisión de JWT |
| **ms-pedidos360-catalog**  | 8081  | `catalog_db`  | CRUD de categorías y productos, control de stock |
| **ms-pedidos360-orders**   | 8082  | `orders_db`   | Órdenes, estados y descuento de stock contra el catálogo |

**Tecnologías:** Java 21 · Spring Boot · PostgreSQL · JWT · Maven · Docker / Docker Compose

---

## Ejecución con Docker (recomendada)

Levanta PostgreSQL + los 4 microservicios en un solo comando:

```bash
# 1) Copiar el archivo de variables y completar los valores:
cp .env.example .env

# 2) Levantar todo:
docker-compose up --build
```

Una vez arriba:

* BFF (punto de entrada único): `http://localhost:8080`
* Login con Microsoft (OAuth2): `http://localhost:8080/oauth2/authorization/azure`
* Catálogo (vía BFF): `http://localhost:8080/api/catalog/products`
* Los puertos 8081, 8082 y 8083 se exponen únicamente para depuración; la aplicación debe consumir siempre el 8080.

> Las variables requeridas en `.env`: `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`, `JWT_SECRET`.

### Parar todo
```bash
docker-compose down
```

> Si actualizas desde una versión anterior, elimina el volumen de la base para que se creen las nuevas bases `catalog_db` y `orders_db`: `docker-compose down -v`

---

## Ejecución local (sin Docker)

Cada microservicio necesita PostgreSQL con su base de datos (`users_db`, `catalog_db`, `orders_db`). Requiere un archivo `.env` en cada carpeta usando su `.env-example`:

```bash
cd ms-pedidos360-users && ./mvnw spring-boot:run      # puerto 8083
cd ms-pedidos360-catalog && ./mvnw spring-boot:run    # puerto 8081
cd ms-pedidos360-orders && ./mvnw spring-boot:run     # puerto 8082
cd ms-pedidos360-bff && ./mvnw spring-boot:run        # puerto 8080
```

---

## Reglas de autorización (BFF)

* **Público (sin JWT):** `/oauth2/**`, `/login/oauth2/**`, `/api/auth/**` (login/registro).
* **Cualquier usuario autenticado:** lectura del catálogo, sus órdenes, su perfil.
* **Solo ADMIN:** escritura en catálogo (`POST/PUT/PATCH/DELETE /api/catalog/**`), cambio de estado de órdenes (`PUT /api/orders/{id}/status`) y listado de usuarios (`GET /api/users`).

Los microservicios internos confían en la red interna; el BFF agrega el encabezado `X-User-Email` para las órdenes.

---

**Variables de Entorno**

```env
JWT_SECRET=tu_clave_hexadecimal_de_64_bytes
JWT_EXPIRATION=86400000
DB_USER=postgres
DB_PASSWORD=postgres
AZURE_CLIENT_ID=
AZURE_CLIENT_SECRET=
AZURE_TENANT_ID=
AZURE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/azure
```

> _En funcionamiento: **Login con Microsoft Entra ID (OAuth2 + PKCE)**, **emisión/validación de JWT con roles**, **arquitectura BFF + 3 microservicios con bases de datos independientes**._