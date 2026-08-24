package com.gymcrm;

import com.gymcrm.constants.enums.ActionType;
import com.gymcrm.dto.TrainerWorkloadRequest;
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
    void addTraining_shouldSaveAndSendWorkloadMessage() {
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

        LocalDateTime trainingDate = LocalDateTime.of(2026, 8, 25, 10, 0);

        Training training = new Training();
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(60);

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Max.Verstappen"))
                .thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Training created = trainingService.addTraining(
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

        verify(trainingRepository).save(any(Training.class));
        verify(gymMetricsService).incrementTrainingCreated();

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);

        verify(trainerWorkloadSenderService).send(eq("tx-1"), requestCaptor.capture());

        TrainerWorkloadRequest workloadRequest = requestCaptor.getValue();
        assertEquals("Max.Verstappen", workloadRequest.getTrainerUsername());
        assertEquals("Max", workloadRequest.getTrainerFirstName());
        assertEquals("Verstappen", workloadRequest.getTrainerLastName());
        assertTrue(workloadRequest.isActive());
        assertEquals(trainingDate, workloadRequest.getTrainingDate());
        assertEquals(60, workloadRequest.getTrainingDuration());
        assertEquals(ActionType.ADD, workloadRequest.getActionType());
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

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker"))
                .thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsername("Max.Verstappen"))
                .thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainingService.addTraining(
                        "tx-1",
                        "Alan.Walker",
                        "Max.Verstappen",
                        training
                ));

        verify(trainingRepository, never()).save(any());
        verify(trainerWorkloadSenderService, never()).send(any(), any());
    }

    @Test
    void deleteTraining_shouldDeleteAndSendWorkloadMessage() {
        TrainingType type = new TrainingType();
        type.setId(5L);
        type.setTrainingTypeName("Yoga");

        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");
        trainer.setActive(true);
        trainer.setSpecialization(type);

        LocalDateTime trainingDate = LocalDateTime.of(2026, 8, 25, 10, 0);

        Training training = new Training();
        training.setId(10L);
        training.setTrainingName("Morning Yoga");
        training.setTrainingDate(trainingDate);
        training.setTrainingDuration(60);
        training.setTrainer(trainer);

        when(trainingRepository.findById(10L)).thenReturn(Optional.of(training));

        trainingService.deleteTraining("tx-2", 10L);

        verify(trainingRepository).delete(training);

        ArgumentCaptor<TrainerWorkloadRequest> requestCaptor =
                ArgumentCaptor.forClass(TrainerWorkloadRequest.class);

        verify(trainerWorkloadSenderService).send(eq("tx-2"), requestCaptor.capture());

        TrainerWorkloadRequest workloadRequest = requestCaptor.getValue();
        assertEquals("Max.Verstappen", workloadRequest.getTrainerUsername());
        assertEquals("Max", workloadRequest.getTrainerFirstName());
        assertEquals("Verstappen", workloadRequest.getTrainerLastName());
        assertTrue(workloadRequest.isActive());
        assertEquals(trainingDate, workloadRequest.getTrainingDate());
        assertEquals(60, workloadRequest.getTrainingDuration());
        assertEquals(ActionType.DELETE, workloadRequest.getActionType());
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