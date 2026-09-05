# ms-barriodigital-requests

Microservicio principal para la gestión del ciclo de vida de los trámites de la plataforma **BarrioDigital**.

## Descripción

Este microservicio administra las solicitudes o trámites creados por los vecinos y funcionarios.

Es responsable del estado transaccional principal de cada trámite y actúa como productor de eventos y comandos asíncronos relacionados con su procesamiento.

## Responsabilidades

- Crear trámites.
- Consultar trámites.
- Listar y filtrar trámites.
- Consultar trámites pertenecientes al usuario autenticado.
- Gestionar cambios de estado.
- Validar reglas de transición.
- Consultar información de tipos de trámite.
- Validar cupos.
- Coordinar asignaciones con cuadrillas.
- Persistir trámites en MySQL.
- Publicar comandos asíncronos en RabbitMQ.
- Publicar eventos de dominio en Kafka.

## Estados del trámite

```text
INGRESADO
   ↓
ADMITIDO
   ↓
EN_GESTION
   ↓
EN_TERRENO
   ↓
RESUELTO
```

Estado alternativo:

```text
RECHAZADO
```

Regla inicial:

```text
Un trámite no puede pasar a EN_TERRENO si previamente no fue ADMITIDO.
```

## Stack tecnológico

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven
- MySQL
- RabbitMQ
- Apache Kafka
- Spring Security
- Resilience4j
- Eureka Client
- Spring Boot Actuator
- Docker

## Puerto local

```text
8081
```

## Base de datos

```text
barriodigital_requests_db
```

Se utiliza el patrón:

```text
Database per Service
```

Este servicio es el único propietario de `barriodigital_requests_db`.

Otros microservicios no deben acceder directamente a sus tablas.

## API inicial

Base path:

```text
/api/v1/requests
```

Endpoints propuestos:

```http
POST /api/v1/requests
GET /api/v1/requests
GET /api/v1/requests/{requestId}
GET /api/v1/requests/me
PATCH /api/v1/requests/{requestId}/status
GET /api/v1/requests/{requestId}/history
```

Ejemplo creación:

```json
{
  "procedureTypeId": 12,
  "description": "Luminaria pública sin funcionamiento",
  "address": {
    "street": "Av. Ejemplo",
    "number": "123",
    "commune": "Recoleta"
  }
}
```

Ejemplo respuesta:

```json
{
  "id": "REQ-2026-000001",
  "status": "INGRESADO",
  "createdAt": "2026-09-05T13:30:00Z"
}
```

Ejemplo cambio de estado:

```http
PATCH /api/v1/requests/REQ-2026-000001/status
```

```json
{
  "status": "ADMITIDO",
  "reason": null
}
```

## Comunicación síncrona

Este servicio podrá consumir:

### Catalog

```text
ms-barriodigital-catalog
```

Para:

- consultar tipos de trámite;
- validar requisitos;
- consultar disponibilidad/cupos.

### Crews

```text
ms-barriodigital-crews
```

Para:

- asignar cuadrillas;
- consultar visitas;
- consultar disponibilidad operacional.

## RabbitMQ

Requests actuará como productor de comandos asíncronos.

Ejemplos:

```text
email.send
crew.ticket
certificate.gen
```

Colas inicialmente consideradas:

```text
q.cmd.email
q.cmd.crew
q.cmd.certificate
```

Cada flujo deberá considerar su correspondiente DLQ.

## Kafka

Requests actuará como productor de eventos de dominio.

Tópico principal:

```text
requests.events
```

Eventos iniciales:

```text
REQUEST_CREATED
REQUEST_ADMITTED
REQUEST_IN_PROGRESS
REQUEST_ON_SITE
REQUEST_RESOLVED
REQUEST_REJECTED
```

## Envelope de eventos

Los mensajes asíncronos deberán utilizar un formato común:

```json
{
  "eventId": "uuid",
  "type": "REQUEST_CREATED",
  "timestamp": "2026-09-05T13:30:00Z",
  "traceId": "trace-id",
  "correlationId": "REQ-2026-000001",
  "version": 1,
  "payload": {}
}
```

## Buenas prácticas de mensajería

Se considera:

- ACK / NACK explícito.
- Idempotencia.
- Dead Letter Queue.
- Dead Letter Topic.
- Reintentos controlados.
- `eventId`.
- `traceId`.
- `correlationId`.
- Versionamiento de eventos.

## Seguridad

Los endpoints estarán protegidos mediante:

- JWT.
- Spring Security.
- RBAC.

Roles iniciales:

```text
ADMIN
OPERATOR
CITIZEN
```

La identidad del usuario debe obtenerse desde el JWT y no desde identificadores arbitrarios enviados por el frontend cuando corresponda.

## Resiliencia

Las llamadas hacia Catalog y Crews podrán utilizar:

- Circuit Breaker.
- Timeout.
- Retry controlado.

Implementación prevista:

```text
Resilience4j
```

## Observabilidad

- Spring Boot Actuator.
- Health checks.
- Logs estructurados.
- Métricas.
- CloudWatch.
- Trazabilidad mediante `traceId` y `correlationId`.
- Métricas de errores de RabbitMQ/Kafka.

## Variables de entorno

```env
SERVER_PORT=8081

MYSQL_HOST=
MYSQL_PORT=3306
MYSQL_DATABASE=barriodigital_requests_db
MYSQL_USER=
MYSQL_PASSWORD=

RABBITMQ_HOST=
RABBITMQ_PORT=5672
RABBITMQ_USER=
RABBITMQ_PASSWORD=

KAFKA_BOOTSTRAP_SERVERS=

CATALOG_SERVICE_URL=
CREWS_SERVICE_URL=

EUREKA_SERVER_URL=
COGNITO_ISSUER_URI=
```

## Contratos

Los contratos oficiales se mantendrán en:

```text
barriodigital-contracts
```

Mediante:

- OpenAPI para REST.
- AsyncAPI para RabbitMQ y Kafka.

## Estructura esperada

```text
src/
├── main/
│   ├── java/
│   │   └── cl/duoc/barriodigital/requests/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── domain/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── exception/
│   │       ├── mapper/
│   │       ├── messaging/
│   │       ├── repository/
│   │       ├── security/
│   │       └── service/
│   └── resources/
│       └── application.yml
└── test/
```

## Ejecución local

```bash
./mvnw spring-boot:run
```

## Docker

```bash
docker build -t barriodigital/requests:1.0.0 .
```

## CI/CD

GitHub Actions será utilizado para:

1. Build.
2. Unit tests.
3. SonarQube.
4. Snyk.
5. Docker build.
6. Push a Amazon ECR.
7. Deploy.

## Estrategia Git

```text
main
develop
feature/*
fix/*
```

## Estado

🚧 Proyecto en etapa inicial de diseño y construcción.
