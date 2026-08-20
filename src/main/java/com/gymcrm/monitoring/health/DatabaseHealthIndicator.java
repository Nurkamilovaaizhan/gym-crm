package com.gymcrm.monitoring.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Health health() {
        try {
            Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);

            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "Available")
                        .build();
            }

            return Health.down()
                    .withDetail("database", "Unexpected response")
                    .build();
        } catch (Exception ex) {
            log.warn("Database health check failed: {}", ex.getMessage());
            return Health.down(ex)
                    .withDetail("database", "Unavailable")
                    .build();
        }
    }
}