#!/usr/bin/env bash
set -euo pipefail

# Despliega Pedidos360 en AWS: sube imagenes y crea la infraestructura
# (ECS Fargate con SPA+BFF+microservicios, ALB, API Gateway HTTP).
# Requisitos previos: AWS CLI configurado, .env con los valores de Azure/JWT, Docker.
#
# Uso: ./deploy.sh [region]

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
STACK="${STACK_NAME:-pedidos360-stack}"
REGION="${1:-$(aws configure get region 2>/dev/null || echo us-east-1)}"

if [[ ! -f "$ROOT/.env" ]]; then
  echo "ERROR: no existe $ROOT/.env (copia .env.example y completa los valores)" >&2
  exit 1
fi
set -a; source "$ROOT/.env"; set +a

echo "==> Paso 1: construir y subir imagenes a ECR"
"$ROOT/deploy/aws/push-images.sh" "$REGION"

echo "==> Paso 2: desplegar plantilla CloudFormation ($STACK en $REGION)"
aws cloudformation deploy \
  --stack-name "$STACK" \
  --template-file "$ROOT/deploy/aws/cloudformation.yaml" \
  --region "$REGION" \
  --parameter-overrides \
    EnvironmentName=pedidos360 \
    DbUser="${DB_USER:-postgres}" \
    DbPassword="$DB_PASSWORD" \
    JwtSecret="$JWT_SECRET" \
    JwtExpiration="${JWT_EXPIRATION:-86400000}" \
    AzureClientId="$AZURE_CLIENT_ID" \
    AzureClientSecret="$AZURE_CLIENT_SECRET" \
    AzureTenantId="$AZURE_TENANT_ID"

echo "==> Paso 3: esperar a que el stack este completo"
aws cloudformation wait stack-create-complete --stack-name "$STACK" --region "$REGION" || \
  aws cloudformation wait stack-update-complete --stack-name "$STACK" --region "$REGION"

OUT=$(aws cloudformation describe-stacks --stack-name "$STACK" --region "$REGION" --query 'Stacks[0].Outputs' --output json)

echo ""
echo "================================================================"
echo " Listo."
echo " SPA + API (API Gateway): $(echo "$OUT" | python3 -c "import json,sys; print([o['OutputValue'] for o in json.load(sys.stdin) if o['OutputKey']=='ApiGatewayUrl'][0])")"
echo " Redirect URI:            $(echo "$OUT" | python3 -c "import json,sys; print([o['OutputValue'] for o in json.load(sys.stdin) if o['OutputKey']=='AzureRedirectUri'][0])")"
echo " Aplica el Redirect URI en el App registration de Azure antes de probar el login."
echo "================================================================"