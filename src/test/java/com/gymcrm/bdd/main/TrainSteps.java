package com.gymcrm.bdd.main;

import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.TrainingRepository;
import com.gymcrm.repository.TrainingTypeRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TrainSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TraineeRepository traineeRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private TrainingTypeRepository trainingTypeRepository;

    private ResultActions result;
    private Exception error;

    @Before
    public void cleanDatabase() {
        trainingRepository.deleteAll();
        traineeRepository.deleteAll();
        trainerRepository.deleteAll();
        trainingTypeRepository.deleteAll();

        result = null;
        error = null;
    }

    @Given("training component is ready")
    public void componentReady() {
        TrainingType type = new TrainingType();
        type.setTrainingTypeName("Strength");
        type = trainingTypeRepository.save(type);

        Trainee trainee = new Trainee();
        trainee.setFirstName("Trainee");
        trainee.setLastName("One");
        trainee.setUsername("Trainee.One");
        trainee.setPassword("test-password");
        trainee.setActive(true);

        Trainer trainer = new Trainer();
        trainer.setFirstName("Trainer");
        trainer.setLastName("One");
        trainer.setUsername("Trainer.One");
        trainer.setPassword("test-password");
        trainer.setActive(true);
        trainer.setSpecialization(type);

        traineeRepository.save(trainee);
        trainerRepository.save(trainer);
    }

    @Given("trainer does not exist")
    public void trainerDoesNotExist() {
        trainerRepository.deleteAll();
    }

    @When("I submit valid training")
    public void submitValidTraining() {
        submitTrainingRequest();
    }

    @When("I submit training")
    public void submitTraining() {
        submitTrainingRequest();
    }

    private void submitTrainingRequest() {
        String body = """
                {
                  "traineeUsername": "Trainee.One",
                  "trainerUsername": "Trainer.One",
                  "trainingName": "Morning training",
                  "trainingDate": "2026-09-10T10:00:00",
                  "trainingDuration": 60
                }
                """;

        try {
            result = mockMvc.perform(
                    post("/api/trainings")
                            .with(user("test-user"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
            );
        } catch (Exception exception) {
            error = exception;
        }
    }

    @Then("training is saved successfully")
    public void trainingIsSaved() {
        assertThat(error).isNull();

        try {
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.trainingName")
                            .value("Morning training"))
                    .andExpect(jsonPath("$.trainingDuration")
                            .value(60));
        } catch (Exception exception) {
            throw new AssertionError("Training response is invalid", exception);
        }
    }

    @Then("training error is returned")
    public void trainingError() {
        assertThat(error).isNull();

        try {
            result.andExpect(status().isBadRequest());
        } catch (Exception exception) {
            throw new AssertionError("Expected bad request", exception);
        }
    }
}