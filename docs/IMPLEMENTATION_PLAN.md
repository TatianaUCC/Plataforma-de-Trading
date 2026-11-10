# Plan de Implementación — Plataforma de Trading

> Fecha de entrega: 19 de noviembre de 2026
> Fecha de inicio estimada: 9 de noviembre de 2026
> Días disponibles: ~10 días

---

## Resumen de Etapas

| Etapa | Nombre | Días estimados | Estado |
|---|---|---|---|
| 0 | Estructura base del proyecto | 0.5 día | ⬜ Pendiente |
| 1 | Infraestructura Docker (brokers + BD) | 1 día | ⬜ Pendiente |
| 2 | Modelo de dominio + pruebas unitarias | 1.5 días | ⬜ Pendiente |
| 3 | `market_data_feed` | 0.5 día | ⬜ Pendiente |
| 4 | `live_dashboard_backend` + live_ticker.html | 1 día | ⬜ Pendiente |
| 5 | `ingestion_bridge` | 0.5 día | ⬜ Pendiente |
| 6 | `analytics_engine` + PostgreSQL | 1.5 días | ⬜ Pendiente |
| 7 | `investment_dashboard_backend` + investment.html | 0.5 día | ⬜ Pendiente |
| 8 | `notification_router` + workers | 1 día | ⬜ Pendiente |
| 9 | Dockerfiles multietapa + ajuste Compose | 1 día | ⬜ Pendiente |
| 10 | Prueba integral + ajustes finales | 1 día | ⬜ Pendiente |

---

## Etapa 0 — Estructura base del proyecto

**Objetivo:** Crear el esqueleto de los 8 proyectos Maven (uno por microservicio) con sus `pom.xml`.

**Archivos creados:**
- `services/market_data_feed/pom.xml`
- `services/live_dashboard_backend/pom.xml`
- `services/ingestion_bridge/pom.xml`
- `services/analytics_engine/pom.xml`
- `services/investment_dashboard_backend/pom.xml`
- `services/notification_router/pom.xml`
- `services/email_worker/pom.xml`
- `services/console_alert_worker/pom.xml`
- `pom.xml` raíz (parent POM opcional para gestionar versiones)

**Componentes involucrados:** Maven, Spring Boot 3.x, Java 17

**Dependencias de otras etapas:** Ninguna (es la base)

**Verificación:** `mvn validate` pasa sin errores en cada módulo

**Requisitos que satisface:** RI-03, RT-05

---

## Etapa 1 — Infraestructura Docker

**Objetivo:** Levantar Kafka, Zookeeper, RabbitMQ y PostgreSQL con Docker Compose. Verificar conectividad antes de programar los microservicios.

**Archivos creados:**
- `docker-compose.yml` (infraestructura base, sin microservicios Java todavía)
- `infra/rabbitmq/rabbitmq.conf`
- `infra/postgres/init.sql` (creación de tablas)

**Componentes involucrados:** Docker, Kafka, Zookeeper, RabbitMQ, PostgreSQL

**Dependencias de otras etapas:** Ninguna

**Verificación:**
- `docker compose up kafka zookeeper rabbitmq postgres-db`
- Acceder a RabbitMQ Management: http://localhost:15672 (guest/guest)
- Verificar que PostgreSQL acepta conexiones
- Crear topics de Kafka manualmente y confirmar

**Requisitos que satisface:** RI-01, RI-02, RI-04

---

## Etapa 2 — Modelo de dominio + pruebas unitarias

**Objetivo:** Implementar el modelo de dominio rico en el módulo `analytics_engine` y validarlo con pruebas unitarias aisladas (sin Spring, sin BD).

**Archivos creados (en `analytics_engine/src/`):**
- `domain/model/AssetCode.java` (record)
- `domain/model/TickPriceRecord.java` (record)
- `domain/model/SignalPayloadRecord.java` (record)
- `domain/model/TradingSignal.java` (entidad rica)
- `domain/model/AssetTracker.java` (agregado)
- `domain/strategy/IndicatorStrategy.java` (interface)
- `domain/strategy/SimpleMovingAverageStrategy.java`
- `domain/state/AssetState.java` (interface)
- `domain/state/NormalState.java`
- `domain/state/MomentumAltoState.java`
- `domain/state/AlertaDisparadaState.java`
- `domain/port/out/SignalPublisherPort.java` (interface DIP)
- `domain/port/out/SignalRepositoryPort.java` (interface DIP)

**Pruebas unitarias:**
- `AssetCodeTest.java` — Fail-Fast (código vacío, nulo)
- `TickPriceRecordTest.java` — Fail-Fast (precio negativo o cero)
- `SimpleMovingAverageStrategyTest.java` — cálculo correcto de SMA, detección de umbral
- `AssetTrackerTest.java` — registrar precios, generar señal, transiciones de estado
- `NormalStateTest.java`, `MomentumAltoStateTest.java` — transiciones correctas

**Dependencias de otras etapas:** Etapa 0

**Verificación:** `mvn test -pl services/analytics_engine` → todos los tests verdes en < 2 segundos

**Requisitos que satisface:** RT-01, RT-02, RT-03, RT-04, RT-06

---

## Etapa 3 — `market_data_feed`

**Objetivo:** Productor que genera ticks aleatorios y los publica en RabbitMQ.

**Archivos creados:**
- `market_data_feed/src/main/java/.../MarketDataFeedApplication.java`
- `.../config/RabbitMQConfig.java` (declaración del Topic Exchange)
- `.../service/PriceGeneratorService.java` (lógica de generación aleatoria)
- `.../adapter/out/RabbitMQTickPublisher.java`
- `market_data_feed/src/main/resources/application.yml`

**Componentes involucrados:** Spring Boot, Spring AMQP, RabbitMQ

**Dependencias de otras etapas:** Etapa 1 (RabbitMQ levantado)

**Verificación:** Ver en RabbitMQ Management que llegan mensajes al exchange `market_ticks` cada segundo.

**Requisitos que satisface:** RF-01, RF-02

---

## Etapa 4 — `live_dashboard_backend` + live_ticker.html

**Objetivo:** Consumir ticks de RabbitMQ y retransmitir por WebSocket.

**Archivos creados:**
- `live_dashboard_backend/src/main/java/.../LiveDashboardApplication.java`
- `.../config/RabbitMQConfig.java` (binding a `ticks.#`)
- `.../config/WebSocketConfig.java`
- `.../adapter/in/TickMessageListener.java`
- `.../service/TickBroadcastService.java`
- `live_dashboard_backend/src/main/resources/static/live_ticker.html`
- `live_dashboard_backend/src/main/resources/application.yml`

**Componentes involucrados:** Spring Boot, Spring AMQP, Spring WebSocket, STOMP

**Dependencias de otras etapas:** Etapa 3

**Verificación:** Abrir http://localhost:8081/live_ticker.html y observar precios actualizando en tiempo real.

**Requisitos que satisface:** RF-02

---

## Etapa 5 — `ingestion_bridge`

**Objetivo:** Puente RabbitMQ → Kafka.

**Archivos creados:**
- `ingestion_bridge/src/main/java/.../IngestionBridgeApplication.java`
- `.../config/RabbitMQConfig.java`
- `.../config/KafkaConfig.java`
- `.../adapter/in/BridgeTickListener.java`
- `.../adapter/out/KafkaTickPublisher.java`
- `ingestion_bridge/src/main/resources/application.yml`

**Componentes involucrados:** Spring Boot, Spring AMQP, Spring Kafka

**Dependencias de otras etapas:** Etapas 1, 3

**Verificación:** Consumir manualmente el topic `historical_ticks` con `kafka-console-consumer.sh` y verificar llegada de mensajes.

**Requisitos que satisface:** RF-03

---

## Etapa 6 — `analytics_engine` + PostgreSQL

**Objetivo:** Motor stateful que calcula SMA, genera señales y persiste en PostgreSQL.

**Archivos creados:**
- `analytics_engine/src/main/java/.../AnalyticsEngineApplication.java`
- `.../config/KafkaConsumerConfig.java`
- `.../config/KafkaProducerConfig.java`
- `.../infrastructure/jpa/TickRecordEntity.java` (entidad JPA)
- `.../infrastructure/jpa/TradingSignalEntity.java`
- `.../infrastructure/jpa/TickRecordRepository.java`
- `.../infrastructure/jpa/TradingSignalRepository.java`
- `.../infrastructure/adapter/out/JpaSignalRepository.java` (implementa `SignalRepositoryPort`)
- `.../infrastructure/adapter/out/KafkaSignalPublisher.java` (implementa `SignalPublisherPort`)
- `.../application/AnalyticsService.java`
- `.../adapter/in/HistoricalTickConsumer.java`
- `analytics_engine/src/main/resources/application.yml`

**Dependencias de otras etapas:** Etapas 1, 2, 5

**Verificación:**
- Ver señales generadas en logs
- Consultar tabla `trading_signals` en PostgreSQL
- Ver mensajes en Kafka topic `trading_signals`

**Requisitos que satisface:** RF-04, RT-01 a RT-05, RI-02

**Pruebas adicionales de integración con mocks:**
- `AnalyticsServiceTest.java` — verifica que al superar el umbral se llama al publisher y al repositorio

---

## Etapa 7 — `investment_dashboard_backend` + investment.html

**Objetivo:** Consumir señales de Kafka y enviarlas por WebSocket.

**Archivos creados:**
- `investment_dashboard_backend/src/main/java/.../InvestmentDashboardApplication.java`
- `.../config/KafkaConsumerConfig.java`
- `.../config/WebSocketConfig.java`
- `.../adapter/in/SignalConsumer.java`
- `.../service/SignalBroadcastService.java`
- `investment_dashboard_backend/src/main/resources/static/investment.html`
- `investment_dashboard_backend/src/main/resources/application.yml`

**Componentes involucrados:** Spring Boot, Spring Kafka, Spring WebSocket

**Dependencias de otras etapas:** Etapas 1, 6

**Verificación:** Abrir http://localhost:8082/investment.html y ver señales de compra/venta.

**Requisitos que satisface:** RF-05

---

## Etapa 8 — `notification_router` + workers

**Objetivo:** Enrutar señales desde Kafka a RabbitMQ y procesar con workers.

**Archivos creados:**
- `notification_router/src/main/java/...` (consumer Kafka + publisher RabbitMQ + escritura a `notification_log` en PostgreSQL)
- `email_worker/src/main/java/...` (consumer Work Queue — solo imprime log, sin BD)
- `console_alert_worker/src/main/java/...` (consumer Fanout — solo imprime log, sin BD)
- Archivos `application.yml` para cada servicio
- Configuraciones RabbitMQ (Fanout Exchange, colas)

**Dependencias de otras etapas:** Etapas 1, 6

**Verificación:**
- Logs del `email_worker`: `"ENVIANDO EMAIL: ..."`
- Logs del `console_alert_worker`: `"*** ALERTA DE SISTEMA: ..."`
- Con 2 réplicas de `email_worker`, verificar balanceo de carga en RabbitMQ Management
- Consultar tabla `notification_log` en PostgreSQL: debe tener registros con canal EMAIL y CONSOLE

**Requisitos que satisface:** RF-06, RF-07, RI-02 (notification_log)

---

## Etapa 9 — Dockerfiles multietapa + ajuste final de Compose

**Objetivo:** Contenerizar todos los microservicios y agregar gobernanza de recursos.

**Archivos creados/modificados:**
- `services/*/Dockerfile` (multietapa para cada uno de los 8 servicios)
- `docker-compose.yml` (completo: infraestructura + 8 microservicios)
  - `deploy.resources.limits` para cada servicio
  - `deploy.replicas: 2` para `email_worker`
  - Health checks
  - Redes bridge separadas

**Verificación:** `docker compose up --build` → todos los contenedores en estado `healthy`.

**Requisitos que satisface:** RI-03, RI-04, RI-05

---

## Etapa 10 — Prueba integral + ajustes

**Objetivo:** Validar el flujo completo end-to-end y preparar la presentación.

**Actividades:**
- Levantar todo el sistema con `docker compose up`
- Verificar flujo completo: tick generado → dashboard en vivo → señal → notificaciones
- Revisar logs de todos los servicios
- Ajustar tiempos de espera si Kafka o RabbitMQ tardan en iniciar
- Preparar comandos de demostración para la presentación (15 min)

**Criterios de éxito:**
- `live_ticker.html` muestra precios actualizando
- `investment.html` muestra señales de compra
- Logs de `email_worker` y `console_alert_worker` muestran las alertas
- PostgreSQL tiene registros en `trading_signals`
- Todos los contenedores estables sin reiniciar

---

## Cronograma Ajustado (10 días)

> **Nota realista:** Las horas indicadas son de trabajo efectivo con Kiro. Se asume que Kiro genera el código y tú lo revisas, ajustas y pruebas. No son horas de programación manual. Suma 30–60 minutos por sesión para leer errores, corregir configuraciones y hacer pruebas manuales.

| Fecha | Etapa | Horas de trabajo efectivo | Riesgo |
|---|---|---|---|
| 9 Nov (Lun) | Etapa 0: estructura Maven | 1-2 h | Bajo |
| 10 Nov (Mar) | Etapa 1: infra Docker (Kafka, RabbitMQ, PostgreSQL) | 2-3 h | **Alto** — Kafka/ZooKeeper tardan en configurar |
| 11 Nov (Mié) | Etapa 2: modelo de dominio + pruebas unitarias | 3-4 h | Medio — el modelo es conceptualmente denso |
| 12 Nov (Jue) | Etapa 3: market_data_feed + Etapa 4: live_dashboard | 3-4 h | Medio — WebSocket puede dar problemas de CORS |
| 13 Nov (Vie) | Etapa 5: ingestion_bridge + inicio Etapa 6 | 3-4 h | Bajo |
| 14 Nov (Sáb) | Etapa 6: analytics_engine completo + PostgreSQL | 4-5 h | **Alto** — el servicio más complejo del proyecto |
| 15 Nov (Dom) | Buffer obligatorio: completar lo que quedó pendiente | flexible | — |
| 16 Nov (Lun) | Etapa 7: investment_dashboard + Etapa 8: notifications | 3-4 h | Bajo |
| 17 Nov (Mar) | Etapa 9: Dockerfiles + Compose final con recursos | 3-4 h | Medio — límites de RAM pueden causar OOM |
| 18 Nov (Mié) | Etapa 10: prueba integral end-to-end + correcciones | 3-4 h | **Alto** — siempre hay ajustes de última hora |
| 19 Nov (Jue) | **Entrega** — repaso arquitectura para presentación | 1-2 h | — |

### Prioridades si el tiempo falla

Si en el día 15 (buffer) hay más de 2 etapas pendientes, aplicar estas prioridades:

1. **Crítico (no se puede omitir):** analytics_engine funcionando + señales en Kafka + PostgreSQL con `trading_signals`
2. **Importante:** market_data_feed, ingestion_bridge, ambos workers
3. **Presentable pero simplificable:** los dashboards HTML pueden ser mínimos (solo reciben y muestran texto)
4. **Aplazable si hay crisis:** `notification_log` en PostgreSQL, réplicas de email_worker (aunque el enunciado las pide)

> **Regla práctica:** no gastes más de 1 hora depurando un error de configuración de Kafka/Docker. Si no resuelves, consulta la siguiente sesión de Kiro con el log exacto del error.
