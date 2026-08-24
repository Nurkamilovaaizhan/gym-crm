package com.gymcrm.service;

import com.gymcrm.constants.enums.ActionType;
import com.gymcrm.dto.TrainerWorkloadRequest;
import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.Training;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.TrainingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final GymMetricsService gymMetricsService;
    private final TrainerWorkloadSenderService trainerWorkloadSenderService;

    public TrainingService(TrainingRepository trainingRepository,
                           TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository,
                           GymMetricsService gymMetricsService,
                           TrainerWorkloadSenderService trainerWorkloadSenderService) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.gymMetricsService = gymMetricsService;
        this.trainerWorkloadSenderService = trainerWorkloadSenderService;
    }

    @Transactional
    public Training addTraining(
                                String transactionId,
                                String traineeUsername,
                                String trainerUsername,
                                Training training) {
        Trainee trainee = traineeRepository.findByUsernameWithTrainers(traineeUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        validateTraining(training);

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(trainer.getSpecialization());

        trainee.getTrainers().add(trainer);

        Training saved = trainingRepository.save(training);
        gymMetricsService.incrementTrainingCreated();

        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(trainer.getUsername());
        request.setTrainerFirstName(trainer.getFirstName());
        request.setTrainerLastName(trainer.getLastName());
        request.setActive(trainer.isActive());
        request.setTrainingDate(saved.getTrainingDate());
        request.setTrainingDuration(saved.getTrainingDuration());
        request.setActionType(ActionType.ADD);

        trainerWorkloadSenderService.send(transactionId, request);

        log.info("Training '{}' added for trainee {} and trainer {}",
                saved.getTrainingName(), trainee.getUsername(), trainer.getUsername());
        return saved;
    }

    @Transactional
    public void deleteTraining(String transactionId,
                               Long trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new IllegalArgumentException("Training not found"));

        Trainer trainer = training.getTrainer();

        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        request.setTrainerUsername(trainer.getUsername());
        request.setTrainerFirstName(trainer.getFirstName());
        request.setTrainerLastName(trainer.getLastName());
        request.setActive(trainer.isActive());
        request.setTrainingDate(training.getTrainingDate());
        request.setTrainingDuration(training.getTrainingDuration());
        request.setActionType(ActionType.DELETE);

        trainerWorkloadSenderService.send(transactionId, request);

        trainingRepository.delete(training);

        log.info("Training '{}' deleted for trainer {}",
                training.getTrainingName(), trainer.getUsername());
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              String trainerName,
                                              String trainingType) {
        return trainingRepository.findByTraineeCriteria(username, from, to, trainerName, trainingType);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              String traineeName) {
        return trainingRepository.findByTrainerCriteria(username, from, to, traineeName);
    }

    private void validateTraining(Training training) {
        if (training.getTrainingName() == null || training.getTrainingName().isBlank()) {
            throw new ValidationException("Training name cannot be empty");
        }
        if (training.getTrainingDate() == null) {
            throw new ValidationException("Training date is required");
        }
        if (training.getTrainingDuration() <= 0) {
            throw new ValidationException("Training duration must be greater than zero");
        }
    }
}