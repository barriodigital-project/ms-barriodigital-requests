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
- Publicar tareas en RabbitMQ.
- Publicar eventos en Kafka.

## Stack tecnológico

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- MySQL
- RabbitMQ
- Kafka
- Spring Security
- Eureka Client
- Spring Boot Actuator
- Docker

## Puerto

```text
8081
```

## Base de datos

```text
barriodigital_requests_db
```

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
```

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
Requests ──► Catalog
Requests ──► Crews
```

## RabbitMQ

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

## Kafka

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

- JWT.
- Spring Security.
- Amazon Cognito.
- RBAC.

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
│   │   └── cl/duoc/barriodigital/requests/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── repository/
│   │       ├── service/
│   │       ├── messaging/
│   │       ├── security/
│   │       └── exception/
│   └── resources/
│       └── application.yml
└── test/
```

## Contratos

```text
barriodigital-contracts/openapi/requests.openapi.yaml
barriodigital-contracts/asyncapi/rabbitmq.asyncapi.yaml
barriodigital-contracts/asyncapi/kafka.asyncapi.yaml
```

## Ejecución

```bash
./mvnw spring-boot:run
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
