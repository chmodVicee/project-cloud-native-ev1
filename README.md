# Backend API - Servicio de Usuarios y Autenticación

API REST desarrollada en Java con Spring Boot y PostgreSQL para la gestión de usuarios, registro y autenticación basada en tokens JWT.

**Tecnologías**
* **Java 21**
* **Spring Boot**
* **PostgreSQL**
* **JSON Web Token (JWT)**
* **Maven**

---

**Variables de Entorno**

Crea un archivo `.env` en la raíz del proyecto backend utilizando como referencia el archivo `.env-example`:

```env
JWT_SECRET=tu_clave_hexadecimal_de_64_bytes
JWT_EXPIRATION=86400000
DB_USERNAME=postgres
DB_PASSWORD=
...
```
> _Actualmente se encuentra en funcionamiento el **Registro** y **Login** para los usuarios_
