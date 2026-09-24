#!/usr/bin/env bash
set -euo pipefail

# Construye y sube las 6 imagenes Docker a Amazon ECR.
# Uso: ./push-images.sh [region]

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
REGION="${1:-$(aws configure get region 2>/dev/null || echo us-east-1)}"
ACCOUNT="$(aws sts get-caller-identity --query Account --output text)"

REGISTRY="$ACCOUNT.dkr.ecr.$REGION.amazonaws.com"

echo "==> Region: $REGION | Registry: $REGISTRY"

echo "==> Login a ECR"
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$REGISTRY"

build_and_push() {
  local name="$1" repo="pedidos360-$1" ctx="$ROOT/$2" dockerfile="${3:-}"
  echo "==> Creando repositorio $repo (si no existe)"
  aws ecr create-repository --repository-name "$repo" --region "$REGION" >/dev/null 2>&1 || true
  echo "==> Build $repo desde $ctx"
  if [[ -n "$dockerfile" ]]; then
    docker build -f "$dockerfile" -t "$repo:latest" "$ctx"
  else
    docker build -t "$repo:latest" "$ctx"
  fi
  docker tag "$repo:latest" "$REGISTRY/$repo:latest"
  docker push "$REGISTRY/$repo:latest"
}

build_and_push "db"       "docker/postgres"
build_and_push "users"    "ms-pedidos360-users"
build_and_push "catalog"  "ms-pedidos360-catalog"
build_and_push "orders"   "ms-pedidos360-orders"
build_and_push "bff"      "ms-pedidos360-bff"
build_and_push "frontend" "FRONTEND" "$ROOT/FRONTEND/Dockerfile.prod"

echo "==> Imagenes publicadas en $REGISTRY/pedidos360-*:latest"