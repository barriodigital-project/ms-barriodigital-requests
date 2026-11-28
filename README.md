# ms-barriodigital-requests

Microservicio encargado de la gestión de trámites de **BarrioDigital**.

## Responsabilidades

- Crear trámites.
- Listar trámites.
- Consultar un trámite.
- Consultar trámites del usuario autenticado.
- Gestionar estados.
- Validar tipos y cupos mediante Catalog.
- Publicar eventos de creación y cambio de estado después de persistirlos.

## Stack tecnológico

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- MySQL
- Eureka Client
- Spring Boot Actuator
- Docker

## Puerto

```text
8081
```

## Base de datos

```text
requests_db
```

Por defecto se conecta a MySQL en `localhost:3306`. `DB_HOST`, `DB_PORT`,
`DB_NAME`, `DB_USERNAME` y `DB_PASSWORD` permiten cambiar la conexión; el
nombre por defecto de la base de datos es `requests_db`. `DB_PASSWORD` es
obligatoria y no tiene una contraseña fallback.
Al ejecutar desde esta carpeta, Spring carga `DB_PASSWORD` desde el archivo
compartido `../.env` del workspace. Ese archivo local no se versiona.

Patrón:

```text
Database per Service
```

## Estados

```text
INGRESADO
ADMITIDO
EN_GESTION
EN_TERRENO
RESUELTO
RECHAZADO
```

## API

```http
POST  /api/v1/requests
GET   /api/v1/requests
GET   /api/v1/requests/me
GET   /api/v1/requests/{id}
PATCH /api/v1/requests/{id}/status
DELETE /api/v1/requests/{id}
```

Eliminar un trámite responde HTTP 204.

## Modelo principal

```text
Request
├── id
├── trackingCode
├── procedureTypeId
├── citizenId
├── description
├── address
├── status
├── assignedCrewId
├── createdAt
└── updatedAt
```

## Integraciones REST

```text
Requests ──► Catalog (validación de tipo y cupos)
Requests ──► Outbox transaccional ──► Amazon SQS
```

Requests valida que el tipo de trámite exista y tenga disponibilidad antes de
crear la solicitud. La creación no reserva ni descuenta cupos. La creación y
cada transición de estado generan un evento en la misma transacción de base de
datos; un publicador reintenta enviar eventos pendientes a SQS.

Los eventos usan el contrato
[`request-events.asyncapi.yaml`](../barriodigital-contracts/asyncapi/request-events.asyncapi.yaml)
y tienen entrega al menos una vez. Report deduplica usando `eventId`.

La autenticación JWT y autorización por roles se realizan en el BFF. Para las
rutas de creación y consultas propias, el BFF reenvía el `sub` del token
validado mediante el encabezado interno `X-Citizen-Id`; no se debe exponer
directamente este MS fuera de la red interna.

Las transiciones de estado permitidas son:

```text
INGRESADO -> ADMITIDO | RECHAZADO
ADMITIDO -> EN_GESTION | RECHAZADO
EN_GESTION -> EN_TERRENO | RESUELTO | RECHAZADO
EN_TERRENO -> EN_GESTION | RESUELTO
RESUELTO y RECHAZADO son terminales
```

Requests se registra en Eureka y lo utiliza para descubrir Catalog. Por defecto
se conecta a `http://localhost:8761/eureka/`; la URL se puede sobrescribir
mediante `EUREKA_SERVER_URL`.

## Seguridad

JWT authentication and role authorization are enforced in the BFF. This service
is internal-only; Requests uses the trusted `X-Citizen-Id` header added by the
BFF for citizen-owned operations.

## Observabilidad

- Spring Boot Actuator.
- Logs.
- Métricas.
- Amazon CloudWatch.

## Estructura esperada

```text
src/
├── main/
│   ├── java/
│   │   └── cl/brarriodigital/request/
│   │       ├── controller/
│   │       ├── dto/
│   │       │   ├── request/
│   │       │   └── response/
│   │       ├── exception/
│   │       ├── model/
│   │       ├── repository/
│   │       ├── service/
│   │       └── RequestMsApplication.java
│   └── resources/
│       └── application.properties
└── test/
```

## Eventos

`SQS_EVENTS_ENABLED` habilita el publicador; `REQUEST_EVENTS_QUEUE_URL`,
`AWS_REGION` y las credenciales temporales de AWS configuran la cola. En local
la integración está deshabilitada por defecto y el servicio sigue funcionando
sin conexión a AWS. La tabla outbox conserva los eventos pendientes y aplica
reintentos con backoff. El DELETE interno no se expone a través del BFF y no
genera eventos.

## Contratos

```text
barriodigital-contracts/openapi/request.openapi.yaml
```

## Ejecución

```bash
./mvnw.cmd spring-boot:run
```

Para levantar el sistema completo, usar
[`compose-up.ps1`](../barriodigital-infra/scripts/compose-up.ps1) desde la raíz
del workspace. `.\mvnw.cmd clean verify` ejecuta pruebas aisladas sobre H2.

## Docker

```bash
docker build -t barriodigital/requests:1.0.0 .
```

## Estrategia Git

```text
main
develop
feature/*
fix/*
```

## Estado

🚧 Proyecto en etapa inicial de diseño e implementación.
