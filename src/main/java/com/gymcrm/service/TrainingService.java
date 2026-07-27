package com.gymcrm.service;

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
    private final AuthenticationService authenticationService;
    private final GymMetricsService gymMetricsService;

    public TrainingService(TrainingRepository trainingRepository,
                           TraineeRepository traineeRepository,
                           TrainerRepository trainerRepository,
                           AuthenticationService authenticationService,
                           GymMetricsService gymMetricsService) {
        this.trainingRepository = trainingRepository;
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.authenticationService = authenticationService;
        this.gymMetricsService = gymMetricsService;
    }

    @Transactional
    public Training addTraining(String authUsername,
                                String authPassword,
                                String traineeUsername,
                                String trainerUsername,
                                Training training) {
        authenticationService.authenticate(authUsername, authPassword);

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

        log.info("Training '{}' added for trainee {} and trainer {}",
                saved.getTrainingName(), trainee.getUsername(), trainer.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(String username,
                                              String password,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              String trainerName,
                                              String trainingType) {
        authenticationService.authenticate(username, password);
        return trainingRepository.findByTraineeCriteria(username, from, to, trainerName, trainingType);
    }

    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(String username,
                                              String password,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              String traineeName) {
        authenticationService.authenticate(username, password);
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