# Estructura de Carpetas del Proyecto

## Árbol completo esperado al finalizar

```
trabajo final/
│
├── docker-compose.yml                   ← Orquestación completa
│
├── infra/
│   ├── rabbitmq/
│   │   └── rabbitmq.conf                ← Configuración personalizada (opcional)
│   └── postgres/
│       └── init.sql                     ← Creación de tablas al iniciar
│
├── docs/
│   ├── REQUIREMENTS.md
│   ├── ARCHITECTURE.md
│   ├── IMPLEMENTATION_PLAN.md
│   ├── DECISIONS.md
│   ├── PROGRESS.md
│   ├── HANDOFF.md
│   └── PROJECT_STRUCTURE.md             ← Este archivo
│
└── services/
    │
    ├── market_data_feed/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/marketfeed/
    │       │   ├── MarketDataFeedApplication.java
    │       │   ├── config/
    │       │   │   └── RabbitMQConfig.java
    │       │   ├── domain/
    │       │   │   └── model/
    │       │   │       ├── TickPriceRecord.java       (record)
    │       │   │       └── AssetCode.java             (record)
    │       │   ├── service/
    │       │   │   └── PriceGeneratorService.java
    │       │   └── adapter/out/
    │       │       └── RabbitMQTickPublisher.java
    │       └── resources/
    │           └── application.yml
    │
    ├── live_dashboard_backend/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/livedashboard/
    │       │   ├── LiveDashboardApplication.java
    │       │   ├── config/
    │       │   │   ├── RabbitMQConfig.java
    │       │   │   └── WebSocketConfig.java
    │       │   ├── adapter/in/
    │       │   │   └── TickMessageListener.java
    │       │   └── service/
    │       │       └── TickBroadcastService.java
    │       └── resources/
    │           ├── application.yml
    │           └── static/
    │               └── live_ticker.html
    │
    ├── ingestion_bridge/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/bridge/
    │       │   ├── IngestionBridgeApplication.java
    │       │   ├── config/
    │       │   │   ├── RabbitMQConfig.java
    │       │   │   └── KafkaConfig.java
    │       │   ├── adapter/in/
    │       │   │   └── BridgeTickListener.java
    │       │   └── adapter/out/
    │       │       └── KafkaTickPublisher.java
    │       └── resources/
    │           └── application.yml
    │
    ├── analytics_engine/                ← EL MÁS COMPLEJO
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/
    │       ├── main/
    │       │   ├── java/com/trading/analytics/
    │       │   │   ├── AnalyticsEngineApplication.java
    │       │   │   ├── domain/
    │       │   │   │   ├── model/
    │       │   │   │   │   ├── AssetCode.java             (record, Fail-Fast)
    │       │   │   │   │   ├── TickPriceRecord.java       (record, Fail-Fast)
    │       │   │   │   │   ├── SignalPayloadRecord.java   (record)
    │       │   │   │   │   ├── AssetTracker.java          (agregado rico)
    │       │   │   │   │   └── TradingSignal.java         (entidad de dominio)
    │       │   │   │   ├── strategy/
    │       │   │   │   │   ├── IndicatorStrategy.java     (interface)
    │       │   │   │   │   └── SimpleMovingAverageStrategy.java
    │       │   │   │   ├── state/
    │       │   │   │   │   ├── AssetState.java            (interface)
    │       │   │   │   │   ├── NormalState.java
    │       │   │   │   │   ├── MomentumAltoState.java
    │       │   │   │   │   └── AlertaDisparadaState.java
    │       │   │   │   └── port/out/
    │       │   │   │       ├── SignalPublisherPort.java   (interface DIP)
    │       │   │   │       └── SignalRepositoryPort.java  (interface DIP)
    │       │   │   ├── application/
    │       │   │   │   └── AnalyticsService.java
    │       │   │   ├── infrastructure/
    │       │   │   │   ├── jpa/
    │       │   │   │   │   ├── TickRecordEntity.java
    │       │   │   │   │   ├── TradingSignalEntity.java
    │       │   │   │   │   ├── TickRecordJpaRepository.java
    │       │   │   │   │   └── TradingSignalJpaRepository.java
    │       │   │   │   └── adapter/out/
    │       │   │   │       ├── JpaSignalRepository.java  (impl SignalRepositoryPort)
    │       │   │   │       └── KafkaSignalPublisher.java (impl SignalPublisherPort)
    │       │   │   ├── config/
    │       │   │   │   ├── KafkaConsumerConfig.java
    │       │   │   │   └── KafkaProducerConfig.java
    │       │   │   └── adapter/in/
    │       │   │       └── HistoricalTickConsumer.java
    │       │   └── resources/
    │       │       └── application.yml
    │       └── test/
    │           └── java/com/trading/analytics/
    │               ├── domain/
    │               │   ├── AssetCodeTest.java
    │               │   ├── TickPriceRecordTest.java
    │               │   ├── AssetTrackerTest.java
    │               │   ├── NormalStateTest.java
    │               │   └── MomentumAltoStateTest.java
    │               ├── strategy/
    │               │   └── SimpleMovingAverageStrategyTest.java
    │               └── application/
    │                   └── AnalyticsServiceTest.java     (con Mockito)
    │
    ├── investment_dashboard_backend/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/investdashboard/
    │       │   ├── InvestmentDashboardApplication.java
    │       │   ├── config/
    │       │   │   ├── KafkaConsumerConfig.java
    │       │   │   └── WebSocketConfig.java
    │       │   ├── adapter/in/
    │       │   │   └── SignalConsumer.java
    │       │   └── service/
    │       │       └── SignalBroadcastService.java
    │       └── resources/
    │           ├── application.yml
    │           └── static/
    │               └── investment.html
    │
    ├── notification_router/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/notificationrouter/
    │       │   ├── NotificationRouterApplication.java
    │       │   ├── config/
    │       │   │   ├── KafkaConsumerConfig.java
    │       │   │   └── RabbitMQConfig.java
    │       │   ├── adapter/in/
    │       │   │   └── SignalKafkaConsumer.java
    │       │   ├── adapter/out/
    │       │   │   └── RabbitMQNotificationPublisher.java
    │       │   └── infrastructure/jpa/
    │       │       ├── NotificationLogEntity.java
    │       │       └── NotificationLogJpaRepository.java
    │       └── resources/
    │           └── application.yml
    │
    ├── email_worker/
    │   ├── Dockerfile
    │   ├── pom.xml
    │   └── src/main/
    │       ├── java/com/trading/emailworker/
    │       │   ├── EmailWorkerApplication.java
    │       │   ├── config/
    │       │   │   └── RabbitMQConfig.java
    │       │   └── adapter/in/
    │       │       └── EmailTaskListener.java
    │       └── resources/
    │           └── application.yml
    │
    └── console_alert_worker/
        ├── Dockerfile
        ├── pom.xml
        └── src/main/
            ├── java/com/trading/consoleworker/
            │   ├── ConsoleAlertWorkerApplication.java
            │   ├── config/
            │   │   └── RabbitMQConfig.java
            │   └── adapter/in/
            │       └── ConsoleAlertListener.java
            └── resources/
                └── application.yml
```

## Convención de paquetes Java

Todos los microservicios siguen el patrón:
```
com.trading.<nombre_del_servicio>
```

Ejemplos:
- `com.trading.analytics`
- `com.trading.livedashboard`
- `com.trading.bridge`
- `com.trading.emailworker`

## Notas sobre la estructura

- Cada microservicio es completamente independiente (sin dependencias entre JAR de microservicios).
- Los Value Objects (`TickPriceRecord`, `AssetCode`) se definen en cada servicio que los necesita (no se comparten vía JAR para evitar complejidad de multi-módulo).
- Las pruebas unitarias solo existen en `analytics_engine` (donde está el dominio rico) como mínimo requerido por el enunciado.
