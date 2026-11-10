# Progreso del Proyecto — Plataforma de Trading

> Última actualización: 9 de noviembre de 2026
> Estado general: Planificación completada — sin código implementado aún

---

## Estado de las Etapas

| Etapa | Estado | Fecha inicio | Fecha fin | Notas |
|---|---|---|---|---|
| 0 — Estructura Maven | ✅ Completado | 9 Nov 2026 | 9 Nov 2026 | 8 pom.xml, 32/32 checks OK |
| 1 — Infraestructura Docker | ⬜ Pendiente | — | — | — |
| 2 — Modelo de dominio + tests | ⬜ Pendiente | — | — | — |
| 3 — market_data_feed | ⬜ Pendiente | — | — | — |
| 4 — live_dashboard_backend | ⬜ Pendiente | — | — | — |
| 5 — ingestion_bridge | ⬜ Pendiente | — | — | — |
| 6 — analytics_engine + PostgreSQL | ⬜ Pendiente | — | — | — |
| 7 — investment_dashboard_backend | ⬜ Pendiente | — | — | — |
| 8 — notification_router + workers | ⬜ Pendiente | — | — | — |
| 9 — Dockerfiles + Compose final | ⬜ Pendiente | — | — | — |
| 10 — Prueba integral | ⬜ Pendiente | — | — | — |

**Leyenda:** ⬜ Pendiente | 🔄 En progreso | ✅ Completado | ❌ Bloqueado

---

## Pruebas ejecutadas

_Ninguna todavía._

---

## Errores conocidos

_Ninguno todavía._

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

### Próxima tarea exacta:
**Etapa 1:** Crear `docker-compose.yml` con solo los 4 servicios de infraestructura (Kafka, Zookeeper, RabbitMQ, PostgreSQL) + `infra/postgres/init.sql` con el esquema de las 3 tablas.

---

## Notas de sesiones anteriores

_(Se irán registrando aquí al avanzar)_
