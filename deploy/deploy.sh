#!/usr/bin/env bash
# Compila y sube los .jar a la EC2 (ejecutar desde su PC, en la raíz del repo backend).
# Uso: ./deploy/deploy.sh ruta/llave.pem ec2-user@<IP-PUBLICA-EC2>
set -euo pipefail
KEY="$1"; HOST="$2"

mvn -q clean package -DskipTests
for s in ms-productos ms-clientes ms-pedidos bff; do
  scp -i "$KEY" "$s/target/$s.jar" "$HOST:/opt/pedidos360/"
done
ssh -i "$KEY" "$HOST" 'for s in ms-productos ms-clientes ms-pedidos bff; do sudo systemctl restart pedidos360@$s; done; sleep 20; for p in 8081 8082 8083 8080; do curl -s localhost:$p/actuator/health; echo; done'
