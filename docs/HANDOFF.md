# Handoff — Contexto para continuar el proyecto

> Este archivo permite retomar el trabajo desde cualquier cuenta o sesión de Kiro.
> Leer COMPLETO antes de hacer cualquier pregunta o pedir código.

---

## Descripción del proyecto

Plataforma simulada de trading con arquitectura Hot-Path/Cold-Path.

- **Ejercicio:** 7 — Proyecto final de Ingeniería de Software
- **Entrega:** 19 de noviembre de 2026
- **Alumna:** trabaja individualmente
- **Objetivo:** Sistema de microservicios que simula un broker de criptomonedas

Genera precios aleatorios para 5 activos (BTC, ETH, EURUSD, GOOGL, AMZN) y los distribuye por dos caminos:
1. **Hot-Path** (RabbitMQ): distribución en tiempo real a dashboards via WebSocket
2. **Cold-Path** (Kafka): análisis histórico, cálculo de SMA, generación de señales de inversión, persistencia en PostgreSQL y envío de notificaciones

---

## Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 LTS | Lenguaje obligatorio |
| Spring Boot | 3.2.5 | Framework para microservicios |
| Apache Kafka | confluentinc/cp-kafka:7.6.1 | Cold-Path: log de eventos persistente |
| RabbitMQ | rabbitmq:3.13-management-alpine | Hot-Path: distribución de ticks |
| PostgreSQL | postgres:15-alpine | Persistencia relacional (señales, ticks) |
| Spring AMQP | incluido en Spring Boot | Cliente RabbitMQ |
| Spring Kafka | incluido en Spring Boot | Cliente Kafka |
| Spring WebSocket | incluido en Spring Boot | Dashboards en tiempo real |
| Spring Data JPA | incluido en Spring Boot | ORM para PostgreSQL |
| JUnit 5 + Mockito | incluido en Spring Boot | Pruebas unitarias |
| Docker + Compose | v2.x | Contenerización y orquestación |
| Maven | 3.9.6 | Gestión de dependencias y compilación |

---

## Estructura del proyecto

```
trabajo final/
├── docs/
│   ├── REQUIREMENTS.md      ← Lista de verificación de requisitos
│   ├── ARCHITECTURE.md      ← Diagrama y flujo completo
│   ├── IMPLEMENTATION_PLAN.md ← Etapas de desarrollo con cronograma
│   ├── DECISIONS.md         ← Decisiones técnicas justificadas
│   ├── PROGRESS.md          ← Avances reales y estado actual
│   └── HANDOFF.md           ← Este archivo
├── services/
│   ├── market_data_feed/
│   ├── live_dashboard_backend/
│   ├── ingestion_bridge/
│   ├── analytics_engine/
│   ├── investment_dashboard_backend/
│   ├── notification_router/
│   ├── email_worker/
│   └── console_alert_worker/
├── infra/
│   ├── rabbitmq/
│   └── postgres/
└── docker-compose.yml
```

---

## Los 8 microservicios (resumen)

| Servicio | Puerto | Entrada | Salida | Función |
|---|---|---|---|---|
| `market_data_feed` | — | — | RabbitMQ `market_ticks` | Genera ticks aleatorios cada segundo |
| `live_dashboard_backend` | 8081 | RabbitMQ `ticks.#` | WebSocket | Muestra precios en vivo |
| `ingestion_bridge` | — | RabbitMQ `ticks.#` | Kafka `historical_ticks` | Puente RabbitMQ→Kafka |
| `analytics_engine` | — | Kafka `historical_ticks` | Kafka `trading_signals` + PostgreSQL | SMA, genera señales, persiste |
| `investment_dashboard_backend` | 8082 | Kafka `trading_signals` | WebSocket | Muestra señales de inversión |
| `notification_router` | — | Kafka `trading_signals` | RabbitMQ `email_queue` + `console_alerts` | Enruta notificaciones |
| `email_worker` | — | RabbitMQ `email_queue` | log | Simula envío de emails |
| `console_alert_worker` | — | RabbitMQ `console_alert_queue` | log | Imprime alertas en consola |

---

## Decisiones importantes (resumen)

1. **Monorepo, proyectos Maven independientes** (no multi-módulo) → más simple para trabajo individual
2. **5 activos:** BTC, ETH, EURUSD, GOOGL, AMZN
3. **Solo SMA obligatoria** (EMA y Momentum son opcionales según el enunciado)
4. **2 réplicas de `email_worker`** para demostrar balanceo de carga
5. **Serialización JSON** con Jackson (sin Avro ni Protobuf)
6. **WebSocket con STOMP** (estándar Spring)
7. **Sin modelos anémicos**: entidades con lógica de negocio, sin getters/setters masivos
8. **Interfaces de puerto** para desacoplar dominio de Kafka/RabbitMQ (DIP)
9. **Un solo PostgreSQL** con 3 tablas: `tick_records` (analytics_engine), `trading_signals` (analytics_engine), `notification_log` (notification_router)
10. **Random Walk ±2%** para simulación de precios
11. **`notification_router` escribe `notification_log`** — los workers solo imprimen logs, no tocan BD
12. **`analytics_engine` escribe `tick_records`** — cada tick de Kafka se persiste como auditoría

Ver `docs/DECISIONS.md` para justificaciones completas (D-01 a D-16).

---

## Patrones de diseño requeridos

| Patrón | Dónde | Clase/Interface |
|---|---|---|
| Strategy | `analytics_engine` | `IndicatorStrategy` / `SimpleMovingAverageStrategy` |
| State | `analytics_engine` | `AssetState` / `NormalState` / `MomentumAltoState` / `AlertaDisparadaState` |
| DIP (Ports & Adapters) | `analytics_engine` | `SignalPublisherPort` / `SignalRepositoryPort` |
| Value Object (`record`) | Todos | `TickPriceRecord` / `SignalPayloadRecord` / `AssetCode` |

---

## Última etapa completada

**Etapa 4 — `ingestion_bridge`** — 11 de noviembre de 2026 ✅

### Archivos implementados
```
services/ingestion_bridge/src/main/java/com/trading/bridge/
├── IngestionBridgeApplication.java
├── adapter/in/BridgeTickListener.java      (ACK manual RabbitMQ)
├── adapter/out/KafkaTickPublisher.java     (send bloqueante con timeout)
├── application/BridgeService.java
├── config/KafkaConfig.java                 (acks=all, retries=3)
├── config/RabbitMQConfig.java              (ACK MANUAL, cola bridge_ticks_queue)
├── domain/model/TickEvent.java             (record, Fail-Fast)
└── domain/port/out/TickKafkaPublisherPort.java (interface DIP)
services/ingestion_bridge/src/main/resources/application.yml
```

### Pruebas: 13/13 ✅ BUILD SUCCESS
- `TickEventTest` (6): validaciones Fail-Fast del record
- `BridgeServiceTest` (3): Mockito — delega al puerto, propaga excepción
- `BridgeTickListenerTest` (4): ACK tras Kafka OK, NACK+requeue ante fallo

### Integración real verificada
- Bridge conectó a RabbitMQ (localhost:5672) y Kafka (localhost:9094)
- Cola `bridge_ticks_queue` con binding `ticks.#` creada automáticamente
- 5 activos publicados en Kafka `historical_ticks` cada ~1s
- Particiones consistentes: BTC→p2, ETH→p0, EURUSD→p4, GOOGL→p1, AMZN→p1
- Orden garantizado por activo (misma clave → misma partición)

### Manejo de errores
- ACK manual RabbitMQ: el mensaje solo se confirma cuando Kafka responde
- Fallo Kafka → `basicNack(requeue=true)` → tick vuelve a `bridge_ticks_queue`
- `acks=all` en Kafka producer: confirmación de todos los ISR
- 3 reintentos con backoff de 500ms ante errores transitorios
- Timeout de 5s en `send().get()`: fallo rápido ante Kafka no disponible

### Dependencia añadida
- `jackson-datatype-jsr310` en `pom.xml` — deserialización de `Instant` ISO-8601

---

## Próxima tarea exacta

**Etapa 5 — `analytics_engine`**

Consumidor Kafka de `historical_ticks`. Mantiene `AssetTracker` en memoria por activo, calcula SMA de los últimos 5 ticks (patrón Strategy), gestiona estados NORMAL/MOMENTUM_ALTO/ALERTA_DISPARADA (patrón State), persiste señales en PostgreSQL y publica en Kafka `trading_signals`.

### Archivos implementados
```
services/live_dashboard_backend/src/main/java/com/trading/livedashboard/
├── LiveDashboardApplication.java
├── adapter/in/TickMessageListener.java
├── adapter/out/WebSocketTickBroadcaster.java
├── config/RabbitMQConfig.java
├── config/WebSocketConfig.java
├── domain/model/TickMessage.java           (record, Fail-Fast)
└── domain/port/out/TickBroadcastPort.java  (interface DIP)
services/live_dashboard_backend/src/main/resources/application.yml
services/live_dashboard_backend/src/main/resources/static/live_ticker.html
```

### Pruebas: 15/15 ✅ BUILD SUCCESS
- `TickMessageTest` (7): validaciones Fail-Fast del record
- `TickMessageListenerTest` (4): Mockito — delega al puerto de broadcast
- `WebSocketTickBroadcasterTest` (4): Mockito — topic `/topic/ticks`, payload correcto

### Integración real verificada
- Tomcat en puerto 8081, conexión a RabbitMQ establecida en 4.8s
- Cola `live_ticks_queue` con binding `ticks.#` creada automáticamente
- Ticks recibidos de los 5 activos (BTC, ETH, EURUSD, GOOGL, AMZN) cada ~1s
- `live_ticker.html` disponible en http://localhost:8081/live_ticker.html

### Dependencia añadida
- `jackson-datatype-jsr310` en `pom.xml` — misma razón que en `market_data_feed` (Instant ISO-8601)

---

## Próxima tarea exacta

**Etapa 4 — `ingestion_bridge`**

Consume ticks de RabbitMQ (`ticks.#`) y los publica en Kafka topic `historical_ticks` usando el nombre del activo como partition key.

### Archivos implementados
```
services/market_data_feed/src/main/java/com/trading/marketfeed/
├── MarketDataFeedApplication.java
├── adapter/out/RabbitMQTickPublisher.java
├── config/RabbitMQConfig.java
├── domain/model/AssetCode.java           (record, Fail-Fast)
├── domain/model/TickPriceRecord.java     (record, Fail-Fast)
├── domain/port/out/TickPublisherPort.java (interface DIP)
└── service/PriceGeneratorService.java
services/market_data_feed/src/main/resources/application.yml
```

### Pruebas: 21/21 ✅ BUILD SUCCESS
- `AssetCodeTest` (6): validaciones Fail-Fast del record
- `TickPriceRecordTest` (5): validaciones Fail-Fast del record
- `PriceGeneratorServiceTest` (5): Mockito — publica 5 ticks, precios positivos, rango ±2%
- `RabbitMQTickPublisherTest` (5): Mockito — routing keys correctos por tipo de activo

### Decisiones de diseño aplicadas
- Random Walk ±2% (D-08)
- 5 activos: BTC, ETH, EURUSD, GOOGL, AMZN (D-07)
- DIP: `TickPublisherPort` desacopla el dominio de RabbitMQ
- Routing keys: `ticks.crypto.*`, `ticks.forex.*`, `ticks.stock.*`
- Credenciales via variables de entorno en `application.yml`

---

## Próxima tarea exacta

**Etapa 3 — `live_dashboard_backend`**

Consume ticks de RabbitMQ (binding `ticks.#`) y los retransmite por WebSocket/STOMP en el puerto 8081 a `live_ticker.html`.

### Archivos de infraestructura
- `docker-compose.yml` — Zookeeper, Kafka, kafka-init, RabbitMQ, PostgreSQL
- `infra/postgres/init.sql` — tablas `tick_records`, `trading_signals`, `notification_log` + índices
- `infra/rabbitmq/rabbitmq.conf` — configuración mínima

### Resultados de la verificación manual
Todos los servicios alcanzaron estado `healthy`:

| Servicio | Estado |
|---|---|
| `zookeeper` | ✅ healthy |
| `kafka` | ✅ healthy |
| `rabbitmq` | ✅ healthy |
| `postgres-db` | ✅ healthy |
| `kafka-init` | ✅ exit 0 |

Topics verificados con `kafka-topics --describe`:
- `historical_ticks`: 5 particiones, replication-factor 1 ✅
- `trading_signals`: 3 particiones, replication-factor 1 ✅

### Correcciones aplicadas durante las pruebas
1. **Zookeeper:** `KAFKA_OPTS: "-Dzookeeper.4lw.commands.whitelist=srvr,ruok"` para habilitar el healthcheck con `ruok`.
2. **kafka-init:** `command` como lista YAML de un único elemento (`- |`) para pasar el script completo como un único argumento a `/bin/sh -c`.

---

## Próxima tarea exacta

**Etapa 2 — Microservicio `market_data_feed`**

Primer microservicio Java Spring Boot. Genera ticks de precio simulados con Random Walk ±2% para 5 activos y los publica en RabbitMQ.

Archivos a crear en `services/market_data_feed/src/`:
- `MarketDataFeedApplication.java`
- `config/RabbitMQConfig.java` — declara el Topic Exchange `market_ticks`
- `domain/model/TickPriceRecord.java` — record inmutable con Fail-Fast
- `domain/model/AssetCode.java` — record inmutable con Fail-Fast
- `service/PriceGeneratorService.java` — genera precios con `@Scheduled`
- `adapter/out/RabbitMQTickPublisher.java` — publica en RabbitMQ
- `resources/application.yml`

**Verificación:** mensajes llegando al exchange `market_ticks` en http://localhost:15672.

---

## Comandos para retomar el trabajo

```bash
# Levantar la infraestructura (ya probada y funcional)
docker compose up zookeeper kafka rabbitmq postgres-db -d
docker compose ps

# Verificar topics de Kafka
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic historical_ticks
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic trading_signals

# RabbitMQ Management UI
# http://localhost:15672  (guest / guest)

# Compilar un microservicio específico
cd services/market_data_feed
mvn clean package -DskipTests

# Levantar todo el sistema (cuando estén los Dockerfiles — Etapa 9)
docker compose up --build

# Parar infraestructura sin borrar datos
docker compose down

# Parar y borrar volúmenes (solo si es necesario reiniciar desde cero)
docker compose down -v
```

---

## Instrucciones para la nueva sesión de Kiro

Al comenzar una nueva sesión:

1. Leer este archivo (`docs/HANDOFF.md`) completo
2. Leer `docs/PROGRESS.md` para ver el estado actual
3. Leer `docs/REQUIREMENTS.md` para revisar qué queda pendiente
4. No re-analizar el enunciado desde cero (ya está en `docs/ARCHITECTURE.md` y `docs/REQUIREMENTS.md`)
5. Pedir la siguiente etapa del `docs/IMPLEMENTATION_PLAN.md`
6. No generar código de etapas posteriores sin completar las anteriores
