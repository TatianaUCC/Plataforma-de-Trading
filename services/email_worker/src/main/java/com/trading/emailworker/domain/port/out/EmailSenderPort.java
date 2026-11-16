package com.trading.emailworker.domain.port.out;

import com.trading.emailworker.domain.model.SignalNotification;

/**
 * Puerto de salida: desacopla el dominio del mecanismo concreto de envío.
 * La implementación actual simula el envío mediante logs (no hay proveedor SMTP configurado).
 */
public interface EmailSenderPort {

    void enviar(SignalNotification notification);
}
