package com.gymcrm.monitoring.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

@Service
public class GymMetricsService {

    private final Counter loginSuccessCounter;
    private final Counter loginFailedCounter;
    private final Counter traineeRegistrationCounter;
    private final Counter trainerRegistrationCounter;
    private final Counter trainingCreatedCounter;

    public GymMetricsService(MeterRegistry meterRegistry) {
        this.loginSuccessCounter = Counter.builder("gym.login.success")
                .description("Successful login attempts")
                .register(meterRegistry);

        this.loginFailedCounter = Counter.builder("gym.login.failed")
                .description("Failed login attempts")
                .register(meterRegistry);

        this.traineeRegistrationCounter = Counter.builder("gym.trainee.registrations")
                .description("Created trainee profiles")
                .register(meterRegistry);

        this.trainerRegistrationCounter = Counter.builder("gym.trainer.registrations")
                .description("Created trainer profiles")
                .register(meterRegistry);

        this.trainingCreatedCounter = Counter.builder("gym.trainings.created")
                .description("Created trainings")
                .register(meterRegistry);
    }

    public void incrementLoginSuccess() {
        loginSuccessCounter.increment();
    }

    public void incrementLoginFailed() {
        loginFailedCounter.increment();
    }

    public void incrementTraineeRegistration() {
        traineeRegistrationCounter.increment();
    }

    public void incrementTrainerRegistration() {
        trainerRegistrationCounter.increment();
    }

    public void incrementTrainingCreated() {
        trainingCreatedCounter.increment();
    }
}