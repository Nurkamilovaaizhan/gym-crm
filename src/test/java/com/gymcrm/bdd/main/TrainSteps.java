package com.gymcrm.bdd.main;

import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.Training;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.TrainingRepository;
import com.gymcrm.service.TrainerWorkloadSenderService;
import com.gymcrm.service.TrainingService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TrainSteps {

    private TrainingService service;
    private TraineeRepository traineeRepository;
    private TrainerRepository trainerRepository;
    private TrainingRepository trainingRepository;
    private Training saved;
    private Exception error;

    @Before
    public void setUp() {
        traineeRepository = mock(TraineeRepository.class);
        trainerRepository = mock(TrainerRepository.class);
        trainingRepository = mock(TrainingRepository.class);

        GymMetricsService metrics = mock(GymMetricsService.class);
        TrainerWorkloadSenderService sender = mock(TrainerWorkloadSenderService.class);

        service = new TrainingService(
                trainingRepository,
                traineeRepository,
                trainerRepository,
                metrics,
                sender
        );
    }

    @Given("training component is ready")
    public void componentReady() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Trainee.One");
        trainee.setFirstName("Trainee");
        trainee.setLastName("One");
        trainee.setTrainers(new HashSet<>());

        Trainer trainer = new Trainer();
        trainer.setUsername("Trainer.One");
        trainer.setFirstName("Trainer");
        trainer.setLastName("One");

        TrainingType type = new TrainingType();
        type.setId(1L);
        type.setTrainingTypeName("Strength");
        trainer.setSpecialization(type);

        when(traineeRepository.findByUsernameWithTrainers("Trainee.One"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.of(trainer));

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation -> {
                    saved = invocation.getArgument(0);
                    return saved;
                });
    }

    @Given("trainer does not exist")
    public void trainerDoesNotExist() {
        when(trainerRepository.findByUsername("Trainer.One"))
                .thenReturn(Optional.empty());
    }

    @When("I submit valid training")
    public void submitValidTraining() {
        try {
            Training training = new Training();
            training.setTrainingName("Morning training");
            training.setTrainingDate(LocalDateTime.of(2026, 9, 10, 10, 0));
            training.setTrainingDuration(60);

            service.addTraining(
                    "test-transaction",
                    "Trainee.One",
                    "Trainer.One",
                    training
            );
        } catch (Exception exception) {
            error = exception;
        }
    }

    @When("I submit training")
    public void submitTraining() {
        submitValidTraining();
    }

    @Then("training is saved successfully")
    public void trainingIsSaved() {
        assertThat(error).isNull();
        assertThat(saved).isNotNull();
        assertThat(saved.getTrainingName()).isEqualTo("Morning training");
    }

    @Then("training error is returned")
    public void trainingError() {
        assertThat(error).isInstanceOf(IllegalArgumentException.class);
    }
}