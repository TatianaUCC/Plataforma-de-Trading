-- =============================================================
-- init.sql — Esquema inicial de la base de datos trading_db
-- Ejecutado automáticamente por PostgreSQL al crear el contenedor.
-- =============================================================

-- ---------------------------------------------------------------
-- Tabla 1: tick_records
-- Registra cada tick de precio procesado por analytics_engine.
-- Escrita por: analytics_engine
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tick_records (
    id           BIGSERIAL    PRIMARY KEY,
    asset_code   VARCHAR(20)  NOT NULL,
    price        DECIMAL(18,8) NOT NULL,
    processed_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- Tabla 2: trading_signals
-- Registra cada señal de inversión generada por analytics_engine.
-- Escrita por: analytics_engine
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS trading_signals (
    id            BIGSERIAL     PRIMARY KEY,
    asset_code    VARCHAR(20)   NOT NULL,
    signal_type   VARCHAR(10)   NOT NULL,          -- 'BUY' | 'SELL'
    reason        VARCHAR(255)  NOT NULL,
    current_price DECIMAL(18,8) NOT NULL,
    sma_value     DECIMAL(18,8) NOT NULL,
    generated_at  TIMESTAMP     NOT NULL DEFAULT NOW(),
    notified      BOOLEAN       NOT NULL DEFAULT FALSE
);

-- ---------------------------------------------------------------
-- Tabla 3: notification_log
-- Registra cada despacho de notificación (EMAIL / CONSOLE).
-- Escrita por: notification_router
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notification_log (
    id           BIGSERIAL   PRIMARY KEY,
    signal_id    BIGINT      NOT NULL REFERENCES trading_signals(id),
    channel      VARCHAR(50) NOT NULL,             -- 'EMAIL' | 'CONSOLE'
    dispatched_at TIMESTAMP  NOT NULL DEFAULT NOW()
);

-- ---------------------------------------------------------------
-- Índices para consultas frecuentes por activo y fecha
-- ---------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_tick_records_asset
    ON tick_records (asset_code, processed_at DESC);

CREATE INDEX IF NOT EXISTS idx_trading_signals_asset
    ON trading_signals (asset_code, generated_at DESC);

CREATE INDEX IF NOT EXISTS idx_notification_log_signal
    ON notification_log (signal_id);
