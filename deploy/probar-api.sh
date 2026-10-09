#!/usr/bin/env bash
# Evidencias para la presentación: respuestas 401 / 200 / 403 a través del API Gateway.
# Uso:
#   export API=https://<API_ID>.execute-api.us-east-1.amazonaws.com
#   export TOKEN_USER=<access token de un usuario sin rol Admin>   (botón "Copiar access token" en Mi perfil)
#   export TOKEN_ADMIN=<access token de un usuario con rol Admin>
#   bash probar-api.sh
set -uo pipefail
: "${API:?Defina API}"

call() { # descripción, curl args...
  local desc="$1"; shift
  local out code
  out=$(curl -s -w '\n%{http_code}' "$@")
  code=$(echo "$out" | tail -n1)
  printf "\n=== %s → HTTP %s\n%s\n" "$desc" "$code" "$(echo "$out" | sed '$d' | head -c 400)"
}

call "Sin token (esperado 401 del API Gateway)"            "$API/api/productos"
call "Token falso (esperado 401 del API Gateway)"          -H "Authorization: Bearer abc.def.ghi" "$API/api/productos"
if [ -n "${TOKEN_USER:-}" ]; then
  call "Usuario: listar productos (200)"                    -H "Authorization: Bearer $TOKEN_USER" "$API/api/productos"
  call "Usuario: resumen BFF (200)"                         -H "Authorization: Bearer $TOKEN_USER" "$API/api/bff/resumen"
  call "Usuario: mis pedidos (200)"                         -H "Authorization: Bearer $TOKEN_USER" "$API/api/pedidos"
  call "Usuario: crear producto sin rol Admin (403 del backend)" -X POST -H "Authorization: Bearer $TOKEN_USER" \
       -H "Content-Type: application/json" -d '{"nombre":"X","categoria":"Y","precio":100,"stock":1}' "$API/api/productos"
  call "Usuario: listar todos los clientes (403 del backend)" -H "Authorization: Bearer $TOKEN_USER" "$API/api/clientes"
fi
if [ -n "${TOKEN_ADMIN:-}" ]; then
  call "Admin: crear producto (201)" -X POST -H "Authorization: Bearer $TOKEN_ADMIN" \
       -H "Content-Type: application/json" -d '{"nombre":"Café","descripcion":"Americano","categoria":"Bebestible","precio":1990,"stock":20}' "$API/api/productos"
  call "Admin: listar clientes (200)" -H "Authorization: Bearer $TOKEN_ADMIN" "$API/api/clientes"
fi
