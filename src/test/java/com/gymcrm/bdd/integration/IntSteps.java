package com.gymcrm.bdd.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class IntSteps {

    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private String token;
    private String trainerUsername;
    private LocalDateTime trainingDate;
    private int duration;
    private int mainStatus;
    private int workloadDuration;

    @Before
    public void setUp() {
        trainerUsername = null;
        trainingDate = null;
        duration = 0;
        mainStatus = 0;
        workloadDuration = 0;
    }

    @Given("both microservices are available")
    public void servicesAvailable() throws Exception {
        String username = required("TEST_USER");
        String password = required("TEST_PASSWORD");

        String query = "?username="
                + encode(username)
                + "&password="
                + encode(password);

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/login" + query))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        assertThat(response.statusCode()).isEqualTo(200);

        JsonNode json = mapper.readTree(response.body());
        token = json.get("token").asText();
    }

    @When("I create a {int} minutes training for trainer {string}")
    public void createTraining(int duration, String trainer) throws Exception {
        this.trainerUsername = trainer;
        this.duration = duration;
        this.trainingDate = LocalDateTime.now().withNano(0);

        String trainee = required("TEST_TRAINEE");

        String body = """
                {
                  "traineeUsername": "%s",
                  "trainerUsername": "%s",
                  "trainingName": "Cucumber training",
                  "trainingDate": "%s",
                  "trainingDuration": %d
                }
                """.formatted(
                trainee,
                trainer,
                trainingDate,
                duration
        );

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/trainings"))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .header("X-Transaction-Id", UUID.randomUUID().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        mainStatus = response.statusCode();
        assertThat(mainStatus).isBetween(200, 299);
    }

    @When("I create an invalid training")
    public void createInvalidTraining() throws Exception {
        String body = """
                {
                  "traineeUsername": "%s",
                  "trainerUsername": "%s",
                  "trainingName": "",
                  "trainingDate": null,
                  "trainingDuration": -1
                }
                """.formatted(
                required("TEST_TRAINEE"),
                required("TEST_TRAINER")
        );

        HttpResponse<String> response = client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/trainings"))
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        mainStatus = response.statusCode();
    }

    @Then("workload for trainer {string} contains {int} minutes")
    public void workloadContains(String trainer, int expected) {
        await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    HttpResponse<String> response = getWorkload(trainer);

                    assertThat(response.statusCode()).isEqualTo(200);

                    JsonNode json = mapper.readTree(response.body());
                    JsonNode years = json.get("years");

                    int total = 0;
                    for (JsonNode year : years) {
                        for (JsonNode month : year.get("months")) {
                            total += month.get("trainingSummaryDuration").asInt();
                        }
                    }

                    workloadDuration = total;
                    assertThat(workloadDuration).isGreaterThanOrEqualTo(expected);
                });
    }

    @Then("main service returns a client error")
    public void clientError() {
        assertThat(mainStatus).isBetween(400, 499);
    }

    private HttpResponse<String> getWorkload(String trainer) throws Exception {
        return client.send(
                HttpRequest.newBuilder()
                        .uri(URI.create(
                                "http://localhost:8081/api/workload/"
                                        + encode(trainer)))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
    }

    private String required(String name) {
        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable is required: " + name);
        }

        return value;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}