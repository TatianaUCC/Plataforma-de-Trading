# Arquitectura — Plataforma de Trading y Alertas de Inversión

## Visión General

Arquitectura de microservicios con dos flujos paralelos que se originan en la misma fuente de datos:

```
                    ┌─────────────────────┐
                    │  market_data_feed   │  (genera ticks simulados cada 1s)
                    └──────────┬──────────┘
                               │ publica
                               ▼
                    ┌─────────────────────┐
                    │  RabbitMQ           │
                    │  Topic Exchange:    │
                    │  market_ticks       │
                    └──┬──────────────┬───┘
                       │              │
          ┌────────────┘              └────────────┐
          │ suscrito a ticks.#         suscrito a ticks.#
          ▼                                        ▼
┌──────────────────────┐             ┌──────────────────────┐
│ live_dashboard_      │             │  ingestion_bridge    │
│ backend              │             │  (RabbitMQ→Kafka)    │
│ (HOT-PATH)           │             └──────────┬───────────┘
└──────────┬───────────┘                        │ publica
           │ WebSocket                          ▼
           ▼                        ┌─────────────────────┐
    live_ticker.html                │  Kafka              │
    (puerto 8081)                   │  Topic:             │
                                    │  historical_ticks   │
                                    └──────────┬──────────┘
                                               │ consume
                                               ▼
                                    ┌──────────────────────┐
                                    │  analytics_engine    │
                                    │  (COLD-PATH)         │
                                    │  - AssetTracker      │
                                    │  - SMA Strategy      │
                                    │  - State Machine     │
                                    └──┬───────────────┬───┘
                                       │ persiste       │ publica
                                       ▼               ▼
                                 PostgreSQL    ┌───────────────┐
                                 (señales,     │ Kafka Topic:  │
                                  ticks)       │trading_signals│
                                              └────┬──────┬───┘
                              ┌─────────────────┘        └──────────────────┐
                              │ groupId: dashboard                           │ groupId: router
                              ▼                                              ▼
                   ┌─────────────────────┐                    ┌─────────────────────┐
                   │investment_dashboard_│                    │ notification_router │
                   │backend              │                    │ (Kafka→RabbitMQ)    │
                   └──────────┬──────────┘                    └──────┬──────────┬──┘
                              │ WebSocket                            │           │
                              ▼                           Work Queue │   Fanout  │
                      investment.html              ┌────────────┐    │  Exchange │
                      (puerto 8082)                │ email_queue│◄───┘  console_│
                                                   └─────┬──────┘      alerts   │
                                                         │                      │
                                                         ▼                      ▼
                                               ┌──────────────┐    ┌────────────────────┐
                                               │ email_worker │    │console_alert_worker│
                                               │ (x2 réplicas)│    │                    │
                                               └──────────────┘    └────────────────────┘
```

---

## Componentes de Mensajería

### RabbitMQ — Configuración

| Componente | Tipo | Nombre | Descripción |
|---|---|---|---|
| Exchange | Topic | `market_ticks` | Distribución de ticks por routing key |
| Routing keys | — | `ticks.crypto.btc`, `ticks.crypto.eth`, `ticks.forex.eurusd`, `ticks.stock.googl`, `ticks.stock.amzn` | Clasificación por tipo de activo |
| Exchange | Fanout | `console_alerts` | Distribución masiva de alertas |
| Queue | Work Queue | `email_queue` | Cola de trabajo para emails (balanceo entre réplicas) |
| Queue | — | `live_ticks_queue` | Cola del `live_dashboard_backend`, binding: `ticks.#` |
| Queue | — | `bridge_ticks_queue` | Cola del `ingestion_bridge`, binding: `ticks.#` |
| Queue | — | `console_alert_queue` | Cola ligada al Fanout `console_alerts` |

### Kafka — Configuración

| Topic | Particiones | Clave | Productor | Consumidores |
|---|---|---|---|---|
| `historical_ticks` | 5 | Nombre del activo (ej. "BTC") | `ingestion_bridge` | `analytics_engine` |
| `trading_signals` | 3 | Nombre del activo | `analytics_engine` | `investment_dashboard_backend`, `notification_router` |

---

## PostgreSQL — Modelo de Datos

### Tabla `tick_records`
Registra cada tick procesado por el `analytics_engine` para auditoría histórica.
**Escrita por:** `analytics_engine` al consumir cada mensaje de Kafka.
**Fundamento:** El enunciado exige "registro inmutable de ticks procesados" como entidad JPA.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGSERIAL PK | Identificador auto-incremental |
| asset_code | VARCHAR(20) | Código del activo (BTC, ETH, etc.) |
| price | DECIMAL(18,8) | Precio del tick |
| processed_at | TIMESTAMP | Momento de procesamiento |

### Tabla `trading_signals`
Registra cada señal generada por el motor de análisis.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGSERIAL PK | Identificador auto-incremental |
| asset_code | VARCHAR(20) | Activo que disparó la señal |
| signal_type | VARCHAR(10) | BUY / SELL |
| reason | VARCHAR(255) | Justificación técnica |
| current_price | DECIMAL(18,8) | Precio al momento de la señal |
| sma_value | DECIMAL(18,8) | Valor de la SMA calculada |
| generated_at | TIMESTAMP | Momento de generación |
| notified | BOOLEAN | Si la señal ya fue despachada |

### Tabla `notification_log`
Registro de auditoría de cada despacho de notificación.
**Escrita por:** `notification_router` al momento de enrutar hacia `email_queue` y `console_alerts`.

| Columna | Tipo | Descripción |
|---|---|---|
| id | BIGSERIAL PK | Identificador |
| signal_id | BIGINT FK | Referencia a `trading_signals` |
| channel | VARCHAR(50) | EMAIL, CONSOLE |
| dispatched_at | TIMESTAMP | Momento del despacho |

> **Nota de implementación:** Si el tiempo es limitado, esta tabla es la de menor prioridad. Los workers cumplen su función (imprimir logs) sin ella. Se implementa al final de la Etapa 8.

---

## Microservicios — Puertos y Dependencias

| Servicio | Puerto externo | Depende de | Produce a | Consume de |
|---|---|---|---|---|
| `market_data_feed` | — | RabbitMQ | RabbitMQ `market_ticks` | — |
| `live_dashboard_backend` | 8081 | RabbitMQ | WebSocket | RabbitMQ `live_ticks_queue` |
| `ingestion_bridge` | — | RabbitMQ, Kafka | Kafka `historical_ticks` | RabbitMQ `bridge_ticks_queue` |
| `analytics_engine` | — | Kafka, PostgreSQL | Kafka `trading_signals`, PostgreSQL | Kafka `historical_ticks` |
| `investment_dashboard_backend` | 8082 | Kafka | WebSocket | Kafka `trading_signals` |
| `notification_router` | — | Kafka, RabbitMQ | RabbitMQ `email_queue`, `console_alerts` | Kafka `trading_signals` |
| `email_worker` | — | RabbitMQ | log | RabbitMQ `email_queue` |
| `console_alert_worker` | — | RabbitMQ | log | RabbitMQ `console_alert_queue` |

---

## Modelo de Dominio (clases principales)

### Value Objects (`record`)
```
TickPriceRecord(String assetCode, double price, Instant timestamp)
  → Fail-Fast: price > 0, assetCode no nulo/vacío

SignalPayloadRecord(String asset, String signal, String reason, double price, double smaValue)

AssetCode(String code)
  → Fail-Fast: código no nulo, no vacío, mayúsculas
```

### Entidad de Dominio Rica
```
AssetTracker
  - assetCode: AssetCode
  - priceHistory: Deque<Double> (últimos N precios)
  - currentState: AssetState  (patrón State)
  - strategy: IndicatorStrategy  (patrón Strategy)
  + registrarPrecio(double price): void
  + evaluarCruceMedia(): Optional<TradingSignal>
  + getEstadoActual(): String
```

### Patrón Strategy
```
IndicatorStrategy (interface)
  + calcular(List<Double> prices): double
  + superaUmbral(double currentPrice, double indicator): boolean

SimpleMovingAverageStrategy implements IndicatorStrategy
  → promedio de los últimos 5 precios, umbral 5%
```

### Patrón State
```
AssetState (interface)
  + onPriceUpdate(AssetTracker ctx, double price): void
  + getNombre(): String

NormalState implements AssetState
MomentumAltoState implements AssetState
AlertaDisparadaState implements AssetState
```

---

## Flujo de Datos — Paso a Paso

1. `market_data_feed` genera `TickPriceRecord("BTC", 60000.0, now())`, valida, publica en RabbitMQ con key `ticks.crypto.btc`.

2. RabbitMQ enruta el mensaje a **dos colas en paralelo**:
   - `live_ticks_queue` → `live_dashboard_backend`
   - `bridge_ticks_queue` → `ingestion_bridge`

3. `live_dashboard_backend` recibe el tick y lo envía **inmediatamente** por WebSocket (sin tocar BD). `live_ticker.html` muestra el precio actualizado en tiempo real.

4. `ingestion_bridge` recibe el tick y publica en Kafka `historical_ticks` con key="BTC". Kafka garantiza orden por activo (misma partición).

5. `analytics_engine` consume de Kafka:
   - Llama `assetTracker.registrarPrecio(60000.0)`
   - `NormalState.onPriceUpdate()` delega a la `SimpleMovingAverageStrategy`
   - Si precio > SMA * 1.05 → transiciona a `MomentumAltoState` → genera `TradingSignal`
   - Persiste la señal en PostgreSQL (`TradingSignalEntity`)
   - Publica `SignalPayloadRecord` en Kafka `trading_signals`

6. Kafka enruta `trading_signals` a dos consumer groups:
   - **groupId: `dashboard-group`** → `investment_dashboard_backend`
   - **groupId: `router-group`** → `notification_router`

7. `investment_dashboard_backend` actualiza su mapa `asset → signal` y lo envía por WebSocket a `investment.html` (puerto 8082).

8. `notification_router` toma la señal y publica:
   - En `email_queue` (Work Queue) → uno de los `email_worker` la toma (balanceo de carga entre réplicas)
   - En `console_alerts` (Fanout) → todos los `console_alert_worker` la reciben

9. `email_worker` imprime: `"ENVIANDO EMAIL: ¡Momento de Invertir! Señal de COMPRA para BTC"`

10. `console_alert_worker` imprime: `"*** ALERTA DE SISTEMA: SEÑAL DE COMPRA EN BTC ***"`
