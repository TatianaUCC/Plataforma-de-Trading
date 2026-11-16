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

**Etapa 9 — `investment_dashboard_backend`** — 16 de noviembre de 2026 ✅

Los 8 microservicios del proyecto están implementados y tienen pruebas unitarias pasando.

### Resumen de microservicios completados

| Servicio | Pruebas | Integración |
|---|---|---|
| `market_data_feed` | 21/21 ✅ | Real ✅ |
| `live_dashboard_backend` | 15/15 ✅ | Real ✅ |
| `ingestion_bridge` | 13/13 ✅ | Real ✅ |
| `analytics_engine` | 35/35 ✅ | Real ✅ |
| `notification_router` | 23/23 ✅ | Real ✅ |
| `email_worker` | 14/14 ✅ | — |
| `console_alert_worker` | 14/14 ✅ | — |
| `investment_dashboard_backend` | 14/14 ✅ | — |

### Próxima tarea exacta

**Dockerfiles multietapa + ajuste final de Docker Compose**

Crear un `Dockerfile` multietapa para cada uno de los 8 microservicios y actualizar el `docker-compose.yml` con:
- Todos los microservicios Java con `deploy.resources.limits`
- `deploy.replicas: 2` para `email_worker`
- Variables de entorno para cada servicio
- `depends_on` con `condition: service_healthy` para los brokers

### Archivos implementados
```
services/notification_router/src/main/java/com/trading/notificationrouter/
├── NotificationRouterApplication.java
├── adapter/in/SignalKafkaConsumer.java          (groupId: notification-router-group)
├── adapter/out/RabbitMQNotificationPublisher.java (email_queue + console_alerts Fanout)
├── application/NotificationRouterService.java
├── config/KafkaConsumerConfig.java
├── config/RabbitMQConfig.java
├── domain/model/SignalEvent.java                (record, Fail-Fast)
├── domain/port/out/NotificationPublisherPort.java
├── domain/port/out/NotificationLogRepositoryPort.java
├── infrastructure/jpa/NotificationLogEntity.java
├── infrastructure/jpa/NotificationLogJpaRepository.java
├── infrastructure/jpa/TradingSignalLookupEntity.java  (solo lectura)
├── infrastructure/jpa/TradingSignalLookupRepository.java
└── infrastructure/adapter/out/JpaNotificationLogRepository.java
services/notification_router/src/main/resources/application.yml
```

### Pruebas: 18/18 ✅ BUILD SUCCESS

### Integración real verificada
- Consumió 6 señales históricas de Kafka `trading_signals`
- Publicó en `email_queue` y Fanout `console_alerts`
- 12 registros en `notification_log` (6 EMAIL + 6 CONSOLE)
- FK `signal_id` resuelto consultando la tabla `trading_signals`

### Estrategia de errores documentada
- Fallo RabbitMQ → propaga excepción → Kafka no hace ACK → reintento (at-least-once)
- Fallo PostgreSQL → se loguea ERROR, no detiene el despacho ya completado
- Posible duplicación si el servicio falla tras publicar en RabbitMQ pero antes del ACK Kafka

### Pendiente de esta etapa
- `email_worker` y `console_alert_worker` aún no implementados (Etapa 8)

---

## Próxima tarea exacta

**Etapa 8 — `email_worker` y `console_alert_worker`**

Workers que consumen de RabbitMQ e imprimen los mensajes de alerta:
- `email_worker`: consume `email_queue` → imprime `"ENVIANDO EMAIL: ¡Momento de Invertir! Señal de COMPRA para BTC"`
- `console_alert_worker`: consume Fanout `console_alerts` → imprime `"*** ALERTA DE SISTEMA: SEÑAL DE COMPRA EN BTC ***"`

### Nuevos archivos implementados (esta etapa)
```
infrastructure/jpa/
├── TickRecordEntity.java           (@Entity → tabla tick_records)
├── TradingSignalEntity.java        (@Entity → tabla trading_signals, método marcarNotificada())
├── TickRecordJpaRepository.java    (JpaRepository Spring Data)
└── TradingSignalJpaRepository.java (JpaRepository Spring Data)

infrastructure/adapter/out/
├── JpaTickRecordRepository.java    (implementa TickRecordRepositoryPort)
└── JpaSignalRepository.java        (implementa SignalRepositoryPort)

domain/port/out/
├── TickRecordRepositoryPort.java   (interface DIP)
└── SignalRepositoryPort.java       (interface DIP)
```

### Modificados
- `application/AnalyticsService.java` — ahora recibe `SignalRepositoryPort` y `TickRecordRepositoryPort` por constructor; persiste tick y señal tras procesamiento; errores de BD se loguean pero no detienen el flujo
- `test/.../AnalyticsServiceTest.java` — actualizado al nuevo constructor de 4 parámetros + 4 pruebas de persistencia añadidas

### Pruebas: 35/35 ✅ BUILD SUCCESS
Incluyendo 4 pruebas de persistencia y manejo de errores de BD.

### Comportamiento ante fallos de BD
Si PostgreSQL no responde, el tick sigue procesándose y la señal sigue publicándose a Kafka. El error se loguea a nivel ERROR. La auditoría es secundaria al flujo principal.

### Integración real con PostgreSQL: ✅ Verificada
- `tick_records`: 90 filas guardadas
- `trading_signals`: 6 señales guardadas (BTC, EURUSD, AMZN con precio, SMA y timestamp)
- Señales publicadas también en Kafka `trading_signals`

### Pendiente de verificar con Docker activo
~~Pendiente~~ — completado.

### notification_log
Tabla existe en BD pero su escritura corresponde a `notification_router` — **no implementada en esta etapa** (Etapa 7 del cronograma).

---

## Próxima tarea exacta

**Etapa 7 — `investment_dashboard_backend`**

Consume señales de Kafka `trading_signals` (groupId: `investment-dashboard-group`) y las retransmite por WebSocket/STOMP en el puerto 8082 a `investment.html`.

### Archivos implementados
```
services/analytics_engine/src/main/java/com/trading/analytics/
├── AnalyticsEngineApplication.java
├── adapter/in/HistoricalTickConsumer.java      (@KafkaListener historical_ticks)
├── adapter/out/KafkaSignalPublisher.java        (publica en trading_signals)
├── application/AnalyticsService.java            (Map<String, AssetTracker>)
├── config/KafkaConsumerConfig.java              (JsonDeserializer por constructor)
├── config/KafkaProducerConfig.java              (acks=all, sin __TypeId__)
├── domain/model/AssetCode.java                  (record, Fail-Fast)
├── domain/model/AssetTracker.java               (entidad rica: Strategy + State)
├── domain/model/SignalPayload.java              (record)
├── domain/model/TickRecord.java                 (record, contrato entrada)
├── domain/port/out/SignalPublisherPort.java     (interface DIP)
├── domain/state/AssetState.java                 (interface patrón State)
├── domain/state/AssetStateContext.java          (interface contexto)
├── domain/state/NormalState.java                (evalúa umbral → genera señal)
├── domain/state/MomentumAltoState.java          (→ transiciona a AlertaDisparada)
├── domain/state/AlertaDisparadaState.java        (→ vuelve a Normal)
├── domain/strategy/IndicatorStrategy.java       (interface patrón Strategy)
└── domain/strategy/SimpleMovingAverageStrategy.java  (SMA-5, umbral 5%)
services/analytics_engine/src/main/resources/application.yml
```

### Pruebas: 27/27 ✅ BUILD SUCCESS
- `AssetCodeTest` (4): validaciones Fail-Fast
- `SimpleMovingAverageStrategyTest` (7): cálculo SMA, umbral 5%, ventana mínima
- `AssetTrackerTest` (11): registro de precios, generación de señales, transiciones de estado completas
- `AnalyticsServiceTest` (5): Mockito — delegación, trackers independientes, normalización

### Integración real verificada
- Consumer group `analytics-engine-group` asignado a las 5 particiones
- Offsets en avance confirmado (partición 0: 441, partición 1: 882)
- Sin señales en tiempo de prueba (esperado con Random Walk ±2%)

### Patrones de diseño implementados
- **Strategy**: `IndicatorStrategy` / `SimpleMovingAverageStrategy` (SMA-5, umbral 1.05)
- **State**: `NormalState` → `MomentumAltoState` → `AlertaDisparadaState` → `NormalState`
- **DIP**: `SignalPublisherPort` desacopla dominio de Kafka
- **Value Objects**: `AssetCode`, `TickRecord`, `SignalPayload` (records con Fail-Fast)

### Pendiente
- Persistencia JPA de `tick_records` y `trading_signals` en PostgreSQL (configuración lista, se activa después)

---

## Próxima tarea exacta

**Etapa 6 — `investment_dashboard_backend`**

Consume señales de Kafka `trading_signals` (groupId: investment-dashboard-group) y las retransmite por WebSocket/STOMP en el puerto 8082 a `investment.html`.

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
