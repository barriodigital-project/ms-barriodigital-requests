# ms-barriodigital-requests

Microservicio encargado de la gestión de trámites de **BarrioDigital**.

## Responsabilidades

- Crear trámites.
- Listar trámites.
- Consultar un trámite.
- Consultar trámites del usuario autenticado.
- Gestionar estados.
- Validar tipos y cupos mediante Catalog.
- Coordinar asignaciones mediante Crews.
- La integración de mensajería se incorporará en una etapa posterior.

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
nombre por defecto de la base de datos es `requests_db`.
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
```

Requests valida que el tipo de trámite exista y tenga disponibilidad antes de
crear la solicitud. La creación no reserva ni descuenta cupos.

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

## Integración futura con RabbitMQ

Productor de tareas asíncronas.

Colas:

```text
q.cmd.notification
q.cmd.crew
```

Flujo:

```text
Requests
   ↓
RabbitMQ
   ↓
Notify
```

## Integración futura con Kafka

Tópico:

```text
requests.events
```

Eventos:

```text
REQUEST_CREATED
REQUEST_ADMITTED
REQUEST_RESOLVED
REQUEST_REJECTED
```

Consumidores:

```text
Audit
Report
```

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

## Alcance de esta etapa

Se implementan modelo, repositorio, servicio, controlador, DTOs, validación,
manejo de errores, persistencia MySQL y validación REST de disponibilidad con
Catalog. RabbitMQ y Kafka, incluidos productores y consumidores, quedan fuera
de esta etapa. La asignación con Crews se implementará en una etapa posterior.

## Contratos

```text
barriodigital-contracts/openapi/request.openapi.yaml
barriodigital-contracts/asyncapi/rabbitmq.asyncapi.yaml
barriodigital-contracts/asyncapi/kafka.asyncapi.yaml
```

## Ejecución

```bash
./mvnw.cmd spring-boot:run
```

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
