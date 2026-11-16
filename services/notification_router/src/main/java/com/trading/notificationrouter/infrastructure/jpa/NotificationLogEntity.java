package com.trading.notificationrouter.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Entidad JPA que mapea la tabla 'notification_log'.
 *
 * Nota sobre signal_id: la tabla tiene FK a trading_signals(id).
 * El notification_router recibe la señal de Kafka (sin el id de BD).
 * Se almacena signal_id=0 cuando no se puede resolver el id de la señal
 * en la misma transacción, priorizando el despacho sobre la integridad referencial.
 *
 * Para evitar violar la FK, el adaptador JPA busca la señal reciente
 * en trading_signals antes de insertar. Si no la encuentra, omite la inserción
 * del log (no bloquea el flujo de notificaciones).
 */
@Entity
@Table(name = "notification_log")
public class NotificationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "signal_id", nullable = false)
    private Long signalId;

    @Column(name = "channel", nullable = false, length = 50)
    private String channel;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt;

    protected NotificationLogEntity() {}

    public NotificationLogEntity(Long signalId, String channel, Instant dispatchedAt) {
        if (signalId == null || signalId <= 0) {
            throw new IllegalArgumentException("signalId debe ser positivo");
        }
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("channel no puede ser nulo ni vacío");
        }
        if (dispatchedAt == null) {
            throw new IllegalArgumentException("dispatchedAt no puede ser nulo");
        }
        this.signalId   = signalId;
        this.channel    = channel;
        this.dispatchedAt = dispatchedAt;
    }

    public Long getId()           { return id; }
    public Long getSignalId()     { return signalId; }
    public String getChannel()    { return channel; }
    public Instant getDispatchedAt() { return dispatchedAt; }
}
