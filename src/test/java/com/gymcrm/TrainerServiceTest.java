package com.gymcrm.service;

import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    private SimpleMeterRegistry meterRegistry;
    private GymMetricsService gymMetricsService;
    private TrainerService trainerService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetricsService = new GymMetricsService(meterRegistry);
        trainerService = new TrainerService(
                trainerRepository,
                userRepository,
                authenticationService,
                gymMetricsService
        );
    }

    @Test
    void createTrainerShouldSetCredentialsAndSave() {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setTrainingTypeName("Strength");

        Trainer trainer = new Trainer();
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");
        trainer.setSpecialization(trainingType);

        when(userRepository.findAllUsernames()).thenReturn(Set.of());
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer saved = trainerService.createTrainer(trainer);

        assertEquals("Max.Verstappen", saved.getUsername());
        assertNotNull(saved.getPassword());
        assertEquals(10, saved.getPassword().length());
        assertTrue(saved.isActive());
        assertEquals(1.0, meterRegistry.find("gym.trainer.registrations").counter().count());

        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void createTrainerShouldThrowValidationExceptionWhenSpecializationIsMissing() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");

        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void setActiveShouldBeNonIdempotent() {
        Trainer trainer = new Trainer();
        trainer.setId(1L);
        trainer.setUsername("Max.Verstappen");
        trainer.setActive(false);

        doNothing().when(authenticationService).authenticate("Max.Verstappen", "pass");
        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainerService.setActive("Max.Verstappen", "pass", false));

        verify(trainerRepository, never()).save(any());
    }
}