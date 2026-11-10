# Requisitos del Proyecto — Plataforma de Trading y Alertas de Inversión

> Extraídos del enunciado del Ejercicio 7. No se inventan requisitos adicionales.
> Formato de verificación: [ ] pendiente | [x] completado

---

## Requisitos Funcionales

### RF-01 Generación de precios simulados
- [ ] Generar ticks de precio cada segundo para 5–10 activos (BTC, ETH, EUR/USD, GOOGL, etc.)
- [ ] Los precios son simulados (no conectar a APIs financieras reales)
- [ ] Cada tick encapsulado en un Value Object inmutable `TickPriceRecord`

### RF-02 Distribución en tiempo real (Hot-Path)
- [ ] Publicar ticks en RabbitMQ, Topic Exchange `market_ticks`
- [ ] Routing keys con formato `ticks.crypto.btc`, `ticks.stock.googl`, etc.
- [ ] `live_dashboard_backend` consume de `ticks.#` y retransmite por WebSocket (puerto 8081)
- [ ] Dashboard `live_ticker.html` muestra precios en vivo

### RF-03 Puente RabbitMQ → Kafka
- [ ] `ingestion_bridge` consume de RabbitMQ (`ticks.#`)
- [ ] Publica cada tick en el topic de Kafka `historical_ticks`
- [ ] Usa el nombre del activo (ej. "BTC") como partition key

### RF-04 Motor de análisis (Cold-Path)
- [ ] `analytics_engine` consume de Kafka (`historical_ticks`)
- [ ] Mantiene estado en memoria por activo con `AssetTracker`
- [ ] Calcula SMA (Simple Moving Average) de los últimos 5 ticks
- [ ] Genera señal de compra cuando precio actual > SMA * 1.05 (umbral 5%)
- [ ] Persiste la señal en PostgreSQL (auditoría)
- [ ] Publica la señal en Kafka topic `trading_signals`: `{ "asset": "BTC", "signal": "BUY", "reason": "Momentum Crossover" }`

### RF-05 Dashboard de inversión
- [ ] `investment_dashboard_backend` consume de Kafka (`trading_signals`)
- [ ] Mantiene estado `asset → signal` en memoria
- [ ] Retransmite recomendaciones por WebSocket (puerto 8082) a `investment.html`

### RF-06 Enrutamiento de notificaciones
- [ ] `notification_router` consume de Kafka (`trading_signals`)
- [ ] Publica en Work Queue RabbitMQ `email_queue`
- [ ] Publica en Fanout Exchange RabbitMQ `console_alerts`

### RF-07 Workers de notificación
- [ ] `email_worker` consume de `email_queue` y simula envío de email
  - Log esperado: `"ENVIANDO EMAIL: ¡Momento de Invertir! Señal de COMPRA para BTC"`
- [ ] `console_alert_worker` consume del Fanout `console_alerts` e imprime alerta
  - Log esperado: `"*** ALERTA DE SISTEMA: SEÑAL DE COMPRA EN BTC ***"`

---

## Requisitos Técnicos de Programación

### RT-01 POO y Modelo Rico
- [ ] Prohibido modelo anémico (sin getters/setters indiscriminados)
- [ ] `AssetTracker` y `TradingSignal` agrupan estado + lógica de negocio
- [ ] Métodos semánticos: `registrarPrecio()`, `evaluarCruceMedia()`, `marcarNotificada()`

### RT-02 Value Objects con `record`
- [ ] `TickPriceRecord` — inmutable, con validación Fail-Fast
- [ ] `SignalPayloadRecord` — inmutable
- [ ] `AssetCode` — inmutable
- [ ] Validaciones en constructor compacto: precio > 0, código no nulo/vacío

### RT-03 Patrón Strategy
- [ ] Interfaz polimórfica para indicadores técnicos (ej. `IndicatorStrategy`)
- [ ] Implementación `SimpleMovingAverageStrategy` (SMA últimos 5 ticks)
- [ ] Posible implementación `EMAStrategy` y `MomentumThresholdStrategy` (opcional)
- [ ] El motor analítico no depende de implementaciones concretas

### RT-04 Patrón State
- [ ] Estados del activo: `NORMAL`, `MOMENTUM_ALTO`, `ALERTA_DISPARADA`
- [ ] Transiciones encapsuladas en objetos de estado
- [ ] Sin `if-else` ni `switch` para manejar transiciones

### RT-05 Principios SOLID + DIP
- [ ] Inversión de dependencias: lógica de dominio no invoca SDKs de Kafka/RabbitMQ directamente
- [ ] Interfaces de puertos para publishers y consumers
- [ ] Inyección estricta por constructor en Spring Boot
- [ ] Sin campo `@Autowired` directo en clases de dominio

### RT-06 Pruebas unitarias (JUnit 5 + Mockito)
- [ ] Pruebas de dominio puro (sin Spring, sin BD, sin brokers)
- [ ] Patrón AAA (Arrange, Act, Assert)
- [ ] Pruebas de cálculo SMA y detección de momentum
- [ ] Pruebas de transiciones de estado (`State`)
- [ ] Pruebas Fail-Fast en records (precio negativo, código vacío)
- [ ] Pruebas de servicios con `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, `verify()`

---

## Requisitos de Infraestructura

### RI-01 Servicios centrales
- [ ] `kafka` — broker central
- [ ] `zookeeper` — coordinador de Kafka
- [ ] `rabbitmq` — broker AMQP
- [ ] `postgres-db` — base de datos relacional

### RI-02 PostgreSQL
- [ ] Volumen nombrado para persistencia ante reinicios
- [ ] Entidades JPA: ticks procesados, señales generadas, registro de notificaciones
- [ ] Repositorios basados en interfaces (Spring Data JPA)

### RI-03 Dockerfile multietapa (cada microservicio)
- [ ] Etapa 1 (Build): `maven:3.9.6-eclipse-temurin-17-alpine`
  - Copia `pom.xml` primero → `mvn dependency:go-offline -B`
  - Luego copia fuente → `mvn clean package -DskipTests`
- [ ] Etapa 2 (Runtime): `eclipse-temurin:17-jre-alpine`
  - Crea `appuser:appgroup` (no root)
  - Copia solo el `.jar`
  - `ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"`
  - Ejecuta como `appuser`

### RI-04 Docker Compose
- [ ] `deploy.resources.limits` en cada microservicio Java (CPU y RAM)
- [ ] `deploy.replicas` para `email_worker` (mínimo 2 réplicas)
- [ ] Redes bridge separadas (interna y externa)
- [ ] Health checks para brokers y BD antes de iniciar microservicios

### RI-05 Gobernanza de recursos
- [ ] CPU por microservicio: `0.50` a `0.75`
- [ ] RAM por microservicio: `384M` a `512M`
- [ ] JVM flags ergonómicos sincronizados con límites de contenedor

---

## Puntos Ambiguos del Enunciado y Resoluciones Adoptadas

1. **EMA y Momentum Strategy**: Se mencionan como ejemplos ("ej."). Solo la SMA es obligatoria. Se define la interfaz `IndicatorStrategy` para cumplir el requisito de polimorfismo sin implementar EMA.

2. **Número exacto de activos**: Dice "5–10 activos". Se implementarán exactamente 5: BTC, ETH, EUR/USD, GOOGL, AMZN. Mínimo del rango para reducir complejidad sin incumplir el enunciado.

3. **Tabla de auditoría de ticks**: El enunciado dice "registro inmutable de ticks procesados" como parte del esquema JPA. Sin embargo, el flujo detallado del `analytics_engine` solo menciona persitir señales explícitamente. **Decisión adoptada**: se incluye la tabla `tick_records` porque el enunciado la menciona en la sección de persistencia JPA. Si el profesor solo espera señales, la tabla adicional no resta puntos.

4. **Tabla `notification_log`**: El enunciado menciona "registro de notificaciones despachadas" en la capa PostgreSQL. El flujo de datos no asigna esta persistencia a ningún microservicio concreto (los workers solo imprimen logs). **Decisión adoptada**: la tabla se crea en el esquema pero la responsabilidad de escribirla queda en el `notification_router` al momento de despachar, no en los workers. Esto es una interpretación razonable. Si el tiempo escasea, esta tabla se puede omitir ya que los workers cumplen su función sin ella.

5. **`groupId` de Kafka**: El enunciado no menciona los `groupId` explícitamente, pero son necesarios para que `investment_dashboard_backend` y `notification_router` consuman el mismo topic `trading_signals` de forma independiente. Se asignan: `investment-dashboard-group` y `notification-router-group`.

6. **WebSocket**: El enunciado no especifica la implementación. Se usa Spring WebSocket con STOMP, el estándar en el ecosistema Spring Boot.

7. **Tabla `notification_log` y `tick_records`**: Son requisitos JPA del enunciado pero de bajo riesgo. Se implementan al final de la Etapa 6. Si el tiempo falla, se omiten sin romper la funcionalidad principal.
