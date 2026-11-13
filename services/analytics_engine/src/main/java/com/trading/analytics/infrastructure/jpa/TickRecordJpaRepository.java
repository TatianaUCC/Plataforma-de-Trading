package com.trading.analytics.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickRecordJpaRepository extends JpaRepository<TickRecordEntity, Long> {
}
