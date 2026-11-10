# Decisiones Técnicas — Plataforma de Trading

> Este archivo registra cada decisión de diseño con su justificación. Se actualiza durante el desarrollo.

---

## D-01 — Monorepo con proyectos Maven independientes

**Decisión:** Un solo repositorio con 8 carpetas bajo `services/`, cada una con su propio `pom.xml` independiente (no un multi-módulo Maven estricto).

**Justificación:**
- Desarrollo individual: facilita trabajar en un servicio sin que los errores de compilación de otro bloqueen el trabajo.
- Cada microservicio tiene su propio Dockerfile, lo que encaja mejor con proyectos Maven independientes.
- Evita la complejidad del parent POM multi-módulo (herencia, versiones compartidas) que puede causar problemas difíciles de depurar.
- Si se desea compartir el modelo de dominio en el futuro, se puede extraer a un módulo compartido, pero no es necesario ahora.

**Alternativa descartada:** Parent POM multi-módulo → mayor complejidad para un proyecto individual de estudiante.

---

## D-02 — Spring Boot 3.x con Java 17

**Decisión:** Spring Boot 3.2.x con Java 17 LTS.

**Justificación:**
- El enunciado exige Java 17+ (uso de `record`).
- Spring Boot 3.x requiere Java 17 mínimo y tiene soporte nativo para records.
- LTS estable y bien soportada en las imágenes Docker base del enunciado.

---

## D-03 — WebSocket con Spring WebSocket + STOMP

**Decisión:** Usar Spring WebSocket con el protocolo STOMP para los dashboards.

**Justificación:**
- El enunciado no especifica la implementación WebSocket, pero STOMP sobre WebSocket es el estándar en el ecosistema Spring.
- `SimpMessagingTemplate` simplifica el broadcast a todos los clientes conectados.
- Fácil integración con JavaScript del lado del cliente (librería `stomp.js` o `@stomp/stompjs`).

---

## D-04 — Patrón Ports & Adapters (Hexagonal) simplificado

**Decisión:** Definir interfaces de puerto (`SignalPublisherPort`, `SignalRepositoryPort`) en el dominio, con implementaciones en la capa de infraestructura.

**Justificación:**
- Cumple el requisito DIP del enunciado: la lógica de dominio no importa clases de Kafka ni de RabbitMQ.
- Permite pruebas unitarias con mocks de las interfaces sin levantar brokers.
- No se implementa arquitectura hexagonal completa (demasiado para el tiempo disponible), solo lo necesario para cumplir el enunciado.

---

## D-05 — Estrategia SMA: ventana de 5 ticks

**Decisión:** `SimpleMovingAverageStrategy` calcula el promedio de los últimos 5 precios.

**Justificación:** El enunciado especifica explícitamente "SMA de los últimos 5 ticks". No se modifica.

**Nota:** Si la ventana tiene menos de 5 precios (inicio del sistema), no se genera señal. Se necesitan al menos 5 ticks acumulados.

---

## D-06 — Umbral de momentum: 5%

**Decisión:** Se genera señal de compra cuando `precio_actual > SMA * 1.05`.

**Justificación:** El enunciado establece explícitamente "5% sobre la SMA". No se modifica.

---

## D-07 — 5 activos simulados

**Decisión:** BTC, ETH, EURUSD, GOOGL, AMZN.

**Justificación:**
- El enunciado dice "5–10 activos". Se elige el mínimo (5) para reducir complejidad y consumo de memoria.
- Los routing keys serán: `ticks.crypto.btc`, `ticks.crypto.eth`, `ticks.forex.eurusd`, `ticks.stock.googl`, `ticks.stock.amzn`.

---

## D-08 — Generación de precios: Random Walk

**Decisión:** Cada tick = precio_anterior * (1 + δ) donde δ es aleatorio entre -0.02 y +0.02 (±2%).

**Justificación:**
- Simula variación realista de precios.
- Garantiza que el precio sea siempre positivo (no llega a cero con este enfoque multiplicativo).
- Simple de implementar y defender.

**Precio inicial por activo:**
- BTC: 60,000
- ETH: 3,000
- EURUSD: 1.08
- GOOGL: 170
- AMZN: 185

---

## D-09 — Sin EMA ni Momentum Strategy adicionales

**Decisión:** No se implementan EMA ni `MomentumThresholdStrategy`.

**Justificación:** El enunciado las menciona como ejemplos ("ej."). Solo la SMA es obligatoria. La interfaz `IndicatorStrategy` permite añadirlas en el futuro si el tiempo lo permite, pero no son requeridas para la entrega.

---

## D-10 — 2 réplicas de `email_worker`

**Decisión:** `deploy.replicas: 2` para `email_worker`.

**Justificación:** El enunciado exige "evidenciar el balanceo de carga en las colas de trabajo de RabbitMQ". Con 2 réplicas consumiendo la misma Work Queue se demuestra esto claramente.

---

## D-11 — Health checks con `depends_on` en Docker Compose

**Decisión:** Cada microservicio Java espera a que sus dependencias estén `healthy` antes de iniciar.

**Justificación:** Kafka tarda en iniciar. Sin `depends_on: condition: service_healthy`, los microservicios fallan al arrancar. Esto es especialmente crítico para `analytics_engine` que necesita tanto Kafka como PostgreSQL.

---

## D-12 — Base de datos: una sola instancia PostgreSQL

**Decisión:** Un único contenedor `postgres-db` con una sola base de datos `trading_db`.

**Justificación:** El enunciado no requiere múltiples bases de datos. Una instancia con tablas separadas es suficiente para auditoría y reduce el consumo de recursos.

---

## D-13 — Serialización: JSON con Jackson

**Decisión:** Usar Jackson para serializar/deserializar mensajes en Kafka y RabbitMQ.

**Justificación:** Jackson es la dependencia estándar en Spring Boot. Configurar `JsonSerializer` y `JsonDeserializer` es directo y evita dependencias adicionales (Avro, Protobuf) que añadirían complejidad innecesaria.

---

## D-14 — Responsable de escribir `notification_log`

**Decisión:** El `notification_router` escribe en `notification_log` al momento de despachar cada señal hacia RabbitMQ (antes de publicar en `email_queue` y `console_alerts`).

**Justificación:**
- El enunciado exige persistencia de notificaciones despachadas pero no asigna esta tarea a ningún worker.
- Los workers (`email_worker`, `console_alert_worker`) solo imprimen logs según el enunciado — no tienen conexión a PostgreSQL.
- El `notification_router` es el único servicio que conoce ambos destinos (EMAIL, CONSOLE) y ya tiene contexto de la señal, por lo que es el lugar natural para registrar el despacho.

**Plan de contingencia:** Si el tiempo falla, esta tabla se omite. El resto del sistema funciona igual.

---

## D-15 — `analytics_engine` persiste `tick_records`

**Decisión:** El `analytics_engine` guarda cada tick que consume de Kafka en la tabla `tick_records`.

**Justificación:** El enunciado especifica explícitamente "registro inmutable de ticks procesados" como entidad JPA dentro de la sección de persistencia. Esta responsabilidad recae naturalmente en el `analytics_engine`, que es el único consumidor de `historical_ticks`.

**Consideración de rendimiento:** Con 5 activos y 1 tick/segundo, se generan 5 registros/segundo. No es un problema de volumen para un proyecto académico.

---

## D-16 — Prioridad de implementación de tablas PostgreSQL

**Orden de prioridad:**
1. `trading_signals` — crítica, el enunciado la exige explícitamente en el flujo del `analytics_engine`
2. `tick_records` — requerida por el enunciado en la sección JPA
3. `notification_log` — requerida pero sin servicio asignado explícito; aplazable si hay crisis de tiempo
