# Progreso del Proyecto — Plataforma de Trading

> Última actualización: 10 de noviembre de 2026
> Estado general: Etapa 1 completada y verificada en vivo — infraestructura operativa

---

## Estado de las Etapas

| Etapa | Estado | Fecha inicio | Fecha fin | Notas |
|---|---|---|---|---|
| 0 — Estructura Maven | ✅ Completado | 9 Nov 2026 | 9 Nov 2026 | 8 pom.xml, 32/32 checks OK |
| 1 — Infraestructura Docker | ✅ Completado | 10 Nov 2026 | 10 Nov 2026 | Todos los servicios healthy. Topics Kafka verificados con --describe. Ver detalle abajo. |
| 2 — market_data_feed | ✅ Completado | 10 Nov 2026 | 10 Nov 2026 | 21/21 pruebas verdes. BUILD SUCCESS. |
| 3 — live_dashboard_backend | ⬜ Pendiente | — | — | — |
| 4 — ingestion_bridge | ⬜ Pendiente | — | — | — |
| 5 — analytics_engine + PostgreSQL | ⬜ Pendiente | — | — | — |
| 6 — investment_dashboard_backend | ⬜ Pendiente | — | — | — |
| 7 — notification_router + workers | ⬜ Pendiente | — | — | — |
| 8 — Dockerfiles + Compose final | ⬜ Pendiente | — | — | — |
| 9 — Prueba integral | ⬜ Pendiente | — | — | — |

**Leyenda:** ⬜ Pendiente | 🔄 En progreso | ✅ Completado | ❌ Bloqueado

---

## Pruebas ejecutadas

### Etapa 1 — Infraestructura Docker (10 Nov 2026, verificación manual)

| Servicio | Estado verificado | Método |
|---|---|---|
| `zookeeper` | ✅ healthy | `docker compose ps` |
| `kafka` | ✅ healthy | `docker compose ps` |
| `rabbitmq` | ✅ healthy | `docker compose ps` + http://localhost:15672 |
| `postgres-db` | ✅ healthy | `docker compose ps` |
| `kafka-init` | ✅ exitoso (exit 0) | `docker compose logs kafka-init` |

### Etapa 2 — market_data_feed (10 Nov 2026)

**Pruebas ejecutadas con `maven:3.9.6-eclipse-temurin-17-alpine` en Docker:**

| Clase de prueba | Tests | Resultado |
|---|---|---|
| `RabbitMQTickPublisherTest` | 5 | ✅ PASS |
| `AssetCodeTest` | 6 | ✅ PASS |
| `TickPriceRecordTest` | 5 | ✅ PASS |
| `PriceGeneratorServiceTest` | 5 | ✅ PASS |
| **Total** | **21** | **✅ BUILD SUCCESS** |

| Topic | Particiones | Replication Factor | Estado |
|---|---|---|---|
| `historical_ticks` | 5 | 1 | ✅ Creado |
| `trading_signals` | 3 | 1 | ✅ Creado |

---

## Errores conocidos

### Resueltos en Etapa 1
- **Zookeeper healthcheck fallaba** con `ruok` — resuelto añadiendo `KAFKA_OPTS: "-Dzookeeper.4lw.commands.whitelist=srvr,ruok"`.
- **kafka-init entregaba el script fragmentado** a `/bin/sh -c` — resuelto usando `command` como lista YAML de un único elemento (`- |`), que preserva el script como string único.

---

## Sesión actual — 9 Nov 2026

### Lo que se hizo hoy:
- ✅ Análisis completo del enunciado
- ✅ Creación y revisión de los 7 documentos de planificación
- ✅ **Etapa 0 completada:** 8 `pom.xml` creados y verificados

### Etapa 0 — detalle
- **8 pom.xml** creados en `services/<nombre>/pom.xml`
- Spring Boot parent: **3.2.5** (todos)
- Java: **17** (todos)
- Verificación estructural: **32/32 checks OK** (XML válido, elementos obligatorios presentes)
- Verificación de dependencias: **32/32 checks OK** (cada servicio tiene exactamente lo que necesita)
- Docker no estaba corriendo → resolución de dependencias Maven diferida a Etapa 1 (dentro del contenedor)

### Dependencias por servicio (resumen)
| Servicio | AMQP | Kafka | JPA+PG | WebSocket |
|---|---|---|---|---|
| market_data_feed | ✅ | — | — | — |
| live_dashboard_backend | ✅ | — | — | ✅ |
| ingestion_bridge | ✅ | ✅ | — | — |
| analytics_engine | — | ✅ | ✅ | — |
| investment_dashboard_backend | — | ✅ | — | ✅ |
| notification_router | ✅ | ✅ | ✅ | — |
| email_worker | ✅ | — | — | — |
| console_alert_worker | ✅ | — | — | — |

### Lo que NO se hizo:
- ❌ Resolución de dependencias Maven en línea (Docker no activo)
- ❌ Clases Java (no corresponde en esta etapa)

## Sesión — 10 Nov 2026 (continuación — validación en vivo)

### Infraestructura verificada manualmente con Docker Desktop activo

Correcciones aplicadas al `docker-compose.yml` durante las pruebas:
1. **Zookeeper:** añadida variable `KAFKA_OPTS` para habilitar comandos `srvr` y `ruok` en la whitelist de four-letter commands.
2. **kafka-init:** corregida la forma de pasar el script a `/bin/sh -c` — se usa `command` como lista YAML de un único elemento (`- |`) para que el script llegue como un único argumento y no fragmentado.

Todos los servicios alcanzaron estado `healthy`. Los dos topics de Kafka fueron creados correctamente y verificados con `kafka-topics --describe`.

### Próxima tarea exacta:
**Etapa 2 — `market_data_feed`**
Primer microservicio Java. Genera ticks de precio simulados (Random Walk ±2%) para 5 activos y los publica en RabbitMQ. Ver `IMPLEMENTATION_PLAN.md` Etapa 3.

---

## Notas de sesiones anteriores

_(Se irán registrando aquí al avanzar)_
