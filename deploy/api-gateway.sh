#!/usr/bin/env bash
# Crea el AWS API Gateway (HTTP API) de Pedidos360 con:
#   - JWT authorizer contra Microsoft Entra External ID (issuer + audience)
#   - Una ruta por endpoint del backend, con su scope requerido
#   - CORS restringido al origen del frontend
# Ejecutar en AWS CloudShell o con AWS CLI v2 configurado.
#
# Uso:
#   export EC2_HOST=<Elastic IP o DNS público de la EC2>
#   export TENANT_ID=<Directory (tenant) ID de External ID>
#   export API_CLIENT_ID=<Application (client) ID de pedidos360-api>
#   export FRONTEND_ORIGIN=https://<dominio-cloudfront>
#   bash api-gateway.sh
set -euo pipefail

: "${EC2_HOST:?Defina EC2_HOST}"
: "${TENANT_ID:?Defina TENANT_ID}"
: "${API_CLIENT_ID:?Defina API_CLIENT_ID}"
: "${FRONTEND_ORIGIN:?Defina FRONTEND_ORIGIN}"
REGION="${AWS_REGION:-us-east-1}"
ISSUER="https://${TENANT_ID}.ciamlogin.com/${TENANT_ID}/v2.0"

echo ">> Creando HTTP API con CORS"
API_ID=$(aws apigatewayv2 create-api --region "$REGION" \
  --name pedidos360-api --protocol-type HTTP \
  --cors-configuration "{\"AllowOrigins\":[\"${FRONTEND_ORIGIN}\",\"http://localhost:4200\"],\"AllowMethods\":[\"GET\",\"POST\",\"PUT\",\"PATCH\",\"DELETE\",\"OPTIONS\"],\"AllowHeaders\":[\"authorization\",\"content-type\"],\"MaxAge\":3600}" \
  --query ApiId --output text)
echo "   API_ID=$API_ID"

echo ">> Creando JWT authorizer (issuer: $ISSUER)"
AUTH_ID=$(aws apigatewayv2 create-authorizer --region "$REGION" --api-id "$API_ID" \
  --name entra-external-id-jwt --authorizer-type JWT \
  --identity-source '$request.header.Authorization' \
  --jwt-configuration "{\"Audience\":[\"${API_CLIENT_ID}\"],\"Issuer\":\"${ISSUER}\"}" \
  --query AuthorizerId --output text)

# route "<METODO> <PATH>" <puerto> <scope>
route() {
  local method="${1%% *}" path="${1#* }" port="$2" scope="$3"
  local int_id
  int_id=$(aws apigatewayv2 create-integration --region "$REGION" --api-id "$API_ID" \
    --integration-type HTTP_PROXY --integration-method "$method" \
    --integration-uri "http://${EC2_HOST}:${port}${path}" \
    --payload-format-version 1.0 --query IntegrationId --output text)
  aws apigatewayv2 create-route --region "$REGION" --api-id "$API_ID" \
    --route-key "$1" --target "integrations/${int_id}" \
    --authorization-type JWT --authorizer-id "$AUTH_ID" \
    --authorization-scopes "$scope" > /dev/null
  printf "   %-38s -> :%s  [%s]\n" "$1" "$port" "$scope"
}

echo ">> Creando rutas"
# ms-productos (8081)
route "GET /api/productos"               8081 Pedidos.Read
route "GET /api/productos/{id}"          8081 Pedidos.Read
route "POST /api/productos"              8081 Pedidos.Write
route "PUT /api/productos/{id}"          8081 Pedidos.Write
route "DELETE /api/productos/{id}"       8081 Pedidos.Write
# ms-clientes (8082)
route "GET /api/clientes/me"             8082 Pedidos.Read
route "PUT /api/clientes/me"             8082 Pedidos.Write
route "GET /api/clientes"                8082 Pedidos.Read
route "GET /api/clientes/{id}"           8082 Pedidos.Read
# ms-pedidos (8083) — la creación de pedidos se expone solo a través del BFF
route "GET /api/pedidos"                 8083 Pedidos.Read
route "GET /api/pedidos/{id}"            8083 Pedidos.Read
route "POST /api/pedidos/{id}/cancelar"  8083 Pedidos.Write
route "PATCH /api/pedidos/{id}/estado"   8083 Pedidos.Write
# bff (8080)
route "GET /api/bff/resumen"             8080 Pedidos.Read
route "GET /api/bff/catalogo"            8080 Pedidos.Read
route "POST /api/bff/pedidos"            8080 Pedidos.Write
route "GET /api/bff/pedidos/{id}/detalle" 8080 Pedidos.Read

echo ">> Creando stage \$default con auto-deploy"
aws apigatewayv2 create-stage --region "$REGION" --api-id "$API_ID" \
  --stage-name '$default' --auto-deploy > /dev/null

echo
echo "Listo. URL del API Gateway (use este valor en environment.ts → api.baseUrl):"
echo "  https://${API_ID}.execute-api.${REGION}.amazonaws.com"
