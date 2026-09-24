# Despliegue en AWS con API Gateway

Controla el **sistema completo** con un **API Gateway de AWS**: la SPA y toda la API se
sirven bajo el mismo origen del API Gateway, que es la única puerta de entrada.

```
[ SPA React + API ]  →  https://<api-id>.execute-api.<region>.amazonaws.com
        │  API Gateway HTTP ($default: reenvía el path completo)
        ▼
[ ALB (HTTP :80) ]  →  /api/*, /oauth2/*, /login/*  →  BFF (bff:8080)
                     →  cualquier otra ruta            →  frontend (nginx SPA)
        ▼
[ ECS Fargate: 1 task con 6 contenedores (misma red localhost) ]
     ├─ db        postgres:16 + init.sql (users_db, catalog_db, orders_db)
     ├─ users     8083  (OAuth2 Microsoft + JWT HS512)
     ├─ catalog   8081
     ├─ orders    8082
     ├─ bff       8080  (valida JWT, autoriza por rol y enruta)
     └─ frontend  80    (nginx sirve la SPA React build)
```

Al servir la SPA desde el mismo API Gateway, el navegador no tiene CORS ni URLs
externas: `/api/**`, `/oauth2/**`, `/login/**` y las rutas de la SPA entran todas por el
mismo origen y el ALB las reparte por path.

## Qué cambió en el código (ya está en este repo)

| Archivo | Cambio |
|---|---|
| `ms-pedidos360-bff/.../CorsFilter.java` | El origen permitido ahora se lee de `CORS_ALLOWED_ORIGIN` (antes fijo `localhost:4200`) |
| `FRONTEND/src/api/client.ts` | El cliente usa `baseURL = VITE_API_URL` si está definida (en AWS queda vacía → relativo) |
| `FRONTEND/src/pages/LoginPage.tsx` | El botón de Microsoft navega a `${VITE_API_URL}/oauth2/authorization/azure` |
| `FRONTEND/src/vite-env.d.ts` | Tipos de `import.meta.env` (nuevo) |
| `FRONTEND/Dockerfile.prod` + `FRONTEND/nginx.conf` | Imagen `pedidos360-frontend`: build + nginx `try_files` para el router SPA |
| `docker/postgres/Dockerfile` | Imagen `pedidos360-db` con el `init.sql` incluido |
| `deploy/aws/*` | Plantilla, scripts y esta guía |

## Requisitos previos

1. **Cuenta de AWS** con permisos para: `ECR`, `ECS/Fargate`, `CloudFormation`, `ALB/EC2 (VPC)`,
   `API Gateway v2`, `IAM`, `CloudWatch Logs`.
2. **AWS CLI v2** configurado (`aws configure`).
3. **Docker** (para buildear y subir las imágenes) y `python3` (para leer outputs).
4. `.env` en la raíz con `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`,
   `JWT_SECRET`, `DB_USER`, `DB_PASSWORD`.

> Costo aproximado: Fargate (0.5 vCPU + 1GB ≈ US$9/mes) + ALB (≈ US$16/mes)
> + API Gateway (gratis bajo los 300M requests/mes) ≈ **US$25/mes** aprox.
> La base de datos corre dentro de Fargate (volumen efímero): si se reinicia el task,
> se pierden los datos. Para producción usar AWS RDS.
>
> **Rol IAM**: el entorno de práctica (voclabs) prohíbe crear roles IAM. La plantilla
> reutiliza el rol ya existente **`LabRole`** como execution + task role del Fargate
> (`!Sub arn:aws:iam::${AWS::AccountId}:role/LabRole`). Si usas una cuenta con permisos
> normales, puedes reemplazarlo por un rol `ecs-tasks.amazonaws.com` con
> `AmazonECSTaskExecutionRolePolicy`.

## Pasos

### 1) Desplegar

```bash
chmod +x deploy/aws/*.sh
./deploy/aws/deploy.sh                 # usa la region de aws configure
./deploy/aws/deploy.sh us-east-1       # o fuerza una region
```

El script:
1. `push-images.sh` → crea 6 repos en ECR (`pedidos360-db/users/catalog/orders/bff/frontend`),
   los buildea (la SPA se compila dentro de la imagen nginx) y los sube.
2. `cloudformation deploy` → VPC, subredes, ALB con reglas por path (SPA vs BFF), ECS
   (task de 6 contenedores), API Gateway HTTP con ruta `$default` → ALB.
3. Espera a que el stack termine e imprime la URL del API Gateway y el Redirect URI.

### 2) Registrar el Redirect URI en Azure (Microsoft Entra)

1. Copia la salida **Redirect URI** del despliegue
   (`https://<api-id>.execute-api.<region>.amazonaws.com/login/oauth2/code/azure`).
2. En [Azure Portal → App registrations → tu app → Authentication → Add a platform → Web],
   agrega esa URI (usa el mismo `AZURE_CLIENT_ID/SECRET/TENANT_ID` de tu `.env`).

### 3) Probar

Abre la URL del API Gateway (misma para la SPA y la API):

```bash
API_URL=$(aws cloudformation describe-stacks --stack-name pedidos360-stack \
  --query "Stacks[0].Outputs[?OutputKey=='ApiGatewayUrl'].OutputValue" --output text)

curl -i "$API_URL"                                # devuelve el index.html de la SPA
curl -i "$API_URL/api/catalog/products"           # 401: exige JWT (el ruteo gateway→BFF ok)
curl -I "$API_URL/oauth2/authorization/azure"     # 302 → login de Microsoft
```

Luego abre `$API_URL` en el navegador.

## Comandos útiles

```bash
# Logs de los contenedores (CloudWatch)
aws logs tail /ecs/pedidos360 --follow

# Estado del servicio ECS
aws ecs describe-services --cluster pedidos360 --services pedidos360-svc

# Actualizar SOLO la SPA (sin tocar backend)
cd FRONTEND && docker build -f Dockerfile.prod -t pedidos360-frontend:latest . && \
docker tag pedidos360-frontend:latest \
  "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$(aws configure get region).amazonaws.com/pedidos360-frontend:latest" && \
docker push "$(aws sts get-caller-identity --query Account --output text).dkr.ecr.$(aws configure get region).amazonaws.com/pedidos360-frontend:latest" && \
aws ecs update-service --cluster pedidos360 --service pedidos360-svc --force-new-deployment

# Borrar todo (¡pierde la base de datos!)
aws cloudformation delete-stack --stack-name pedidos360-stack
# (los repos ECR quedan; bórralos con aws ecr delete-repository si quieres)
```

## Notas y limitaciones

- **El API Gateway es el punto de CONTROL**: expone todo el sistema bajo un solo origen
  HTTPS y reenvía al ALB sin reescribir paths. La **autenticación y autorización** siguen
  en el BFF (valida JWT y roles).
- Si quieres que el propio gateway valide el JWT, agrega un authorizer (Lambda que firma
  HS512). No es necesario para el flujo actual.
- La conexión ALB→contenedores es **HTTP** (red privada de la VPC). El cliente siempre
  habla HTTPS (el API Gateway lo termina).
- Si el task se cae, Fargate lo reinicia automáticamente; los datos de la BD son efímeros.
- `deploy.sh` no usa S3 ni CloudFront (permisos restringidos del entorno de práctica).