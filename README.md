# Pedidos360 — Backend (Spring Boot)

Backend del sistema **Pedidos360** (DSY1107 Desarrollo Cloud Native I). Está compuesto por cuatro servicios Java 21 + Spring Boot 3.5 y una librería de seguridad compartida.

| Módulo | Puerto | Responsabilidad | Base de datos |
|---|---|---|---|
| `ms-productos` | 8081 | Catálogo de productos (CRUD, eliminación lógica) | `pedidos360_productos` |
| `ms-clientes` | 8082 | Perfil del cliente vinculado al usuario del IDaaS (`oid`) | `pedidos360_clientes` |
| `ms-pedidos` | 8083 | Pedidos e ítems, estados y reglas de propiedad | `pedidos360_pedidos` |
| `bff` | 8080 | Backend For Frontend: orquesta los tres servicios para el frontend Angular | — |
| `security-common` | — | Validación de JWT y manejo de errores compartido por todos | — |

## Arquitectura

```
Angular (MSAL) ──JWT──▶ AWS API Gateway (JWT authorizer + CORS) ──▶ EC2
                                                                     ├─ bff :8080 ──JWT──▶ ms-productos / ms-clientes / ms-pedidos
                                                                     ├─ ms-productos :8081 ─┐
                                                                     ├─ ms-clientes  :8082 ─┼─▶ AWS RDS MySQL
                                                                     └─ ms-pedidos   :8083 ─┘
```

## Seguridad (validación del JWT en cada servicio)

Cada servicio es un *OAuth2 Resource Server* y valida el token igual que el API Gateway (defensa en profundidad). Toda la lógica está en `security-common`:

- **Firma:** se verifica con las llaves públicas (JWKS) del tenant de Entra External ID.
- **Vigencia:** se validan `exp` y `nbf`.
- **Issuer:** el `iss` debe coincidir exactamente con `IDAAS_ISSUER`.
- **Audience:** el `aud` debe ser el client ID de la API (`IDAAS_AUDIENCE`). Lo revisa `AudienceValidator`.
- **Scopes:** el claim `scp` se mapea a `SCOPE_Pedidos.Read` y `SCOPE_Pedidos.Write`.
- **Roles:** el claim `roles` se mapea a `ROLE_Admin` y `ROLE_Cliente`.
- **Errores en JSON:** sin token o con un token inválido se responde **401**; con un token válido pero sin el rol o scope necesario se responde **403**.

| Endpoint | Regla |
|---|---|
| `GET /api/productos/**` | scope `Pedidos.Read` |
| `POST/PUT/DELETE /api/productos/**` | scope `Pedidos.Write` + rol `Admin` |
| `GET /api/clientes/me` · `PUT /api/clientes/me` | `Pedidos.Read` · `Pedidos.Write` |
| `GET/POST/PUT/DELETE /api/clientes/**` | rol `Admin` |
| `GET /api/pedidos/**` | `Pedidos.Read`. Un usuario normal solo ve sus pedidos; si el pedido es de otro, recibe 403 |
| `POST /api/pedidos`, `POST /api/pedidos/{id}/cancelar` | `Pedidos.Write` |
| `PATCH /api/pedidos/{id}/estado`, `DELETE /api/pedidos/{id}` | `Pedidos.Write` + rol `Admin` |
| `GET /api/bff/resumen`, `/api/bff/catalogo`, `/api/bff/pedidos/{id}/detalle` | `Pedidos.Read` |
| `POST /api/bff/pedidos` | `Pedidos.Write` |
| `GET /actuator/health` | público (health check) |

## Compilar y probar

Requisitos: JDK 21 y Maven 3.9 o superior.

```bash
mvn clean verify                 # compila y ejecuta las pruebas (usan H2 en memoria)
mvn clean package -DskipTests    # genera <modulo>/target/<modulo>.jar
```

## Ejecutar localmente

Sin RDS, usando H2 en memoria:

```bash
export IDAAS_ISSUER="https://<TENANT_ID>.ciamlogin.com/<TENANT_ID>/v2.0"
export IDAAS_JWKS_URI="https://<SUBDOMINIO>.ciamlogin.com/<TENANT_ID>/discovery/v2.0/keys"
export IDAAS_AUDIENCE="<API_CLIENT_ID>"
export CORS_ALLOWED_ORIGINS="http://localhost:4200"   # solo en local

java -jar ms-productos/target/ms-productos.jar --spring.profiles.active=h2
java -jar ms-clientes/target/ms-clientes.jar   --spring.profiles.active=h2
java -jar ms-pedidos/target/ms-pedidos.jar     --spring.profiles.active=h2
java -jar bff/target/bff.jar
```

## Variables de entorno

| Variable | Uso |
|---|---|
| `IDAAS_ISSUER`, `IDAAS_JWKS_URI`, `IDAAS_AUDIENCE` | Validación del JWT |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión a RDS MySQL (microservicios) |
| `PRODUCTOS_URL`, `CLIENTES_URL`, `PEDIDOS_URL` | URLs internas que usa el BFF |
| `CORS_ALLOWED_ORIGINS` | Solo en desarrollo local; en AWS el CORS lo maneja el API Gateway |
| `PORT` | Cambia el puerto por defecto |

## Despliegue en EC2

Los scripts están en `deploy/`:

- `setup-ec2.sh` instala Corretto 21 y crea las unidades systemd.
- `deploy.sh` compila, sube los `.jar` a la instancia y reinicia los servicios.

La guía paso a paso completa está en `GUIA_CONFIGURACION_CLOUD.md`.
