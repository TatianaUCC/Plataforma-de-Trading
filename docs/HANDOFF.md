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
| Spring Boot | 3.2.x | Framework para microservicios |
| Apache Kafka | 3.x (imagen Bitnami) | Cold-Path: log de eventos persistente |
| RabbitMQ | 3.x (imagen management) | Hot-Path: distribución de ticks |
| PostgreSQL | 15 | Persistencia relacional (señales, ticks) |
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

**Etapa 1 — Infraestructura Docker Compose** — 10 de noviembre de 2026

Archivos: `docker-compose.yml`, `infra/postgres/init.sql`, `infra/rabbitmq/rabbitmq.conf`.
Commit `0e41f5b` en `origin/main`.
Validación en vivo pendiente: ejecutar `docker compose up zookeeper kafka rabbitmq postgres-db -d` cuando Docker Desktop esté activo y verificar `docker compose ps`.

---

## Pendiente de validación antes de Etapa 2

> **Importante:** Antes de comenzar Etapa 2, activar Docker Desktop y ejecutar:
> ```
> docker compose up zookeeper kafka rabbitmq postgres-db -d
> docker compose ps
> ```
> Verificar que los 4 servicios estén `healthy`. Si alguno falla, corregir antes de avanzar.
> Ver sección "Comandos de verificación" más abajo.

---

## Próxima tarea exacta

**Etapa 2 — Modelo de dominio + pruebas unitarias** (en `analytics_engine`)

Records, entidades ricas, Strategy, State, interfaces de puerto y tests JUnit 5 + Mockito.

---

## Comandos para retomar el trabajo

```bash
# Ver el estado actual del proyecto
ls "c:\Users\Asus TUF\Documents\Sistemas\trabajo final"

# Levantar solo la infraestructura (cuando esté creado docker-compose.yml)
docker compose up kafka zookeeper rabbitmq postgres-db -d

# Ver logs de un servicio específico
docker compose logs analytics_engine -f

# Compilar un microservicio específico
cd services/analytics_engine
mvn clean package -DskipTests

# Ejecutar pruebas unitarias de un microservicio
cd services/analytics_engine
mvn test

# Levantar todo el sistema
docker compose up --build

# Parar todo
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
