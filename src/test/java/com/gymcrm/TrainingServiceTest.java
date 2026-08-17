package com.gymcrm;

import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.Training;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.TrainingRepository;
import com.gymcrm.service.TrainerWorkloadSenderService;
import com.gymcrm.service.TrainingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private GymMetricsService gymMetricsService;

    @Mock
    private TrainerWorkloadSenderService trainerWorkloadSenderService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void addTraining_shouldSaveAndConnectEntities() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");
        trainee.setTrainers(new HashSet<>());

        TrainingType type = new TrainingType();
        type.setId(5L);
        type.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");
        trainer.setActive(true);
        trainer.setSpecialization(type);

        Training training = new Training();
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(LocalDateTime.now());
        training.setTrainingDuration(60);

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Training created = trainingService.addTraining(
                "Bearer test-token",
                "tx-1",
                "Alan.Walker",
                "Max.Verstappen",
                training
        );

        assertNotNull(created);
        assertEquals("Morning Yoga", created.getTrainingName());
        assertEquals(type, created.getTrainingType());
        assertEquals(trainee, created.getTrainee());
        assertEquals(trainer, created.getTrainer());
        assertTrue(trainee.getTrainers().contains(trainer));

        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        assertEquals("Morning Yoga", captor.getValue().getTrainingName());

        verify(gymMetricsService).incrementTrainingCreated();
        verify(trainerWorkloadSenderService).send(eq("Bearer test-token"), eq("tx-1"), any());
    }

    @Test
    void addTraining_shouldThrowWhenTrainingNameMissing() {
        Training training = new Training();
        training.setTrainingDate(LocalDateTime.now());
        training.setTrainingDuration(60);
        training.setTrainingName(" ");

        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");
        trainee.setTrainers(new HashSet<>());

        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainingService.addTraining("Bearer test-token", "tx-1", "Alan.Walker", "Max.Verstappen", training));
    }

    @Test
    void getTraineeTrainings_shouldQuery() {
        when(trainingRepository.findByTraineeCriteria("Alan.Walker", null, null, null, null))
                .thenReturn(java.util.List.of());

        var result = trainingService.getTraineeTrainings("Alan.Walker", null, null, null, null);

        assertNotNull(result);
        verify(trainingRepository).findByTraineeCriteria("Alan.Walker", null, null, null, null);
    }

    @Test
    void getTrainerTrainings_shouldQuery() {
        when(trainingRepository.findByTrainerCriteria("Max.Verstappen", null, null, null))
                .thenReturn(java.util.List.of());

        var result = trainingService.getTrainerTrainings("Max.Verstappen", null, null, null);

        assertNotNull(result);
        verify(trainingRepository).findByTrainerCriteria("Max.Verstappen", null, null, null);
    }
}