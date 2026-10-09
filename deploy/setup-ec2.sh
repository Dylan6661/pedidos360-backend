#!/usr/bin/env bash
# Prepara una instancia Amazon Linux 2023 para ejecutar los 4 servicios de Pedidos360.
# Uso (en la EC2):  bash setup-ec2.sh
set -euo pipefail

sudo dnf install -y java-21-amazon-corretto-headless
sudo mkdir -p /opt/pedidos360 /etc/pedidos360
sudo chown ec2-user:ec2-user /opt/pedidos360

if [ ! -f /etc/pedidos360/pedidos360.env ]; then
  sudo cp pedidos360.env.example /etc/pedidos360/pedidos360.env
  sudo chmod 600 /etc/pedidos360/pedidos360.env
  echo ">> Edite /etc/pedidos360/pedidos360.env con sus valores reales"
fi

# DB_URL distinto para cada microservicio (misma instancia RDS, distinta base de datos)
source <(sudo cat /etc/pedidos360/pedidos360.env)
for svc in productos clientes pedidos; do
  echo "DB_URL=jdbc:mysql://${DB_HOST}:3306/pedidos360_${svc}?createDatabaseIfNotExist=true&serverTimezone=UTC" \
    | sudo tee /etc/pedidos360/ms-${svc}.env > /dev/null
  sudo chmod 600 /etc/pedidos360/ms-${svc}.env
done

sudo cp pedidos360@.service /etc/systemd/system/
sudo systemctl daemon-reload
echo ">> Listo. Copie los .jar a /opt/pedidos360 y ejecute:"
echo "   for s in ms-productos ms-clientes ms-pedidos bff; do sudo systemctl enable --now pedidos360@\$s; done"
