package com.trading.notificationrouter.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogJpaRepository extends JpaRepository<NotificationLogEntity, Long> {
}
