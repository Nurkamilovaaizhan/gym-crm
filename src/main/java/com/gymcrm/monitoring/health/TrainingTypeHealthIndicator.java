package com.gymcrm.monitoring.health;

import com.gymcrm.repository.TrainingTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TrainingTypeHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        try {
            long count = trainingTypeRepository.count();

            if (count > 0) {
                return Health.up()
                        .withDetail("trainingTypesCount", count)
                        .build();
            }

            return Health.down()
                    .withDetail("trainingTypesCount", count)
                    .withDetail("reason", "Training types are not initialized")
                    .build();
        } catch (Exception ex) {
            log.warn("Training type health check failed: {}", ex.getMessage());
            return Health.down(ex).build();
        }
    }
}