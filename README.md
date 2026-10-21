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

## Alcance de la primera etapa

Este primer incremento establece la estructura por capas. La implementación de
los endpoints y reglas de negocio se realizará en incrementos posteriores.
RabbitMQ y Kafka, incluidos productores y consumidores, quedan fuera de esta
etapa. Las integraciones síncronas con Catalog y Crews se realizarán por REST
cuando se implemente el flujo de trámites.

## Contratos

```text
barriodigital-contracts/openapi/request.openapi.yaml
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
