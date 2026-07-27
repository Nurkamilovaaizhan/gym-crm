package com.gymcrm.service;

import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.Training;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.TrainingRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private AuthenticationService authenticationService;

    private SimpleMeterRegistry meterRegistry;
    private GymMetricsService gymMetricsService;
    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetricsService = new GymMetricsService(meterRegistry);
        trainingService = new TrainingService(
                trainingRepository,
                traineeRepository,
                trainerRepository,
                authenticationService,
                gymMetricsService
        );
    }

    @Test
    void addTrainingShouldAttachEntitiesAndSave() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("Alan.Walker");
        trainee.setTrainers(new HashSet<>());

        TrainingType type = new TrainingType();
        type.setId(10L);
        type.setTrainingTypeName("Cardio");

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("Max.Verstappen");
        trainer.setSpecialization(type);

        Training training = new Training();
        training.setTrainingName("Morning Run");
        training.setTrainingDate(LocalDateTime.now());
        training.setTrainingDuration(45);

        doNothing().when(authenticationService).authenticate("authUser", "pass");
        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training saved = trainingService.addTraining(
                "authUser",
                "pass",
                "Alan.Walker",
                "Max.Verstappen",
                training
        );

        assertSame(trainee, saved.getTrainee());
        assertSame(trainer, saved.getTrainer());
        assertSame(type, saved.getTrainingType());
        assertTrue(trainee.getTrainers().contains(trainer));
        assertEquals(1.0, meterRegistry.find("gym.trainings.created").counter().count());

        verify(trainingRepository).save(any(Training.class));
    }

    @Test
    void addTrainingShouldFailWhenDurationIsInvalid() {
        Training training = new Training();
        training.setTrainingName("Morning Run");
        training.setTrainingDate(LocalDateTime.now());
        training.setTrainingDuration(0);

        doNothing().when(authenticationService).authenticate("authUser", "pass");
        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(new Trainee()));
        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(new Trainer()));

        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(
                        "authUser",
                        "pass",
                        "Alan.Walker",
                        "Max.Verstappen",
                        training
                ));

        verify(trainingRepository, never()).save(any());
    }
}