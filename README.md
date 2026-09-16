# Backend API - Servicio de Usuarios

API REST en Java con Spring Boot y PostgreSQL para la gestión de usuarios y autenticación.

**Tecnologías**
* **Java** (21)
* **Spring Boot**
* **PostgreSQL**
* **Maven**

---

**Variables de Entorno**

Crea un archivo `.env` en la raíz del **backend** con la siguiente configuración base para desarrollo (*hay un*`.env-example` con los datos necesarios para comprobar el funcionamiento del codigo)

```env
JWT_SECRET=...
JWT_EXPIRATION=...
DB_HOST=localhost
...
```
### Estado del Endpoint `/api/users/profile`

> **Nota sobre la respuesta 403 Forbidden:**  
> Al realizar una petición `GET` sin encabezados de autorización, la API devolverá un estado **403 Forbidden**. 
>
> Este comportamiento confirma el **correcto funcionamiento de la infraestructura base**
