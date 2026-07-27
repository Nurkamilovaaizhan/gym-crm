package com.gymcrm.controller;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.dto.TraineeDto;
import com.gymcrm.dto.TrainerShortDto;
import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TraineeService;
import com.gymcrm.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    public TraineeController(TraineeService traineeService,
                             TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainingService = trainingService;
    }

    @PostMapping
    public CredentialsDto create(@RequestBody TraineeDto request) {
        var saved = traineeService.createTrainee(RestMapper.toEntity(request));

        CredentialsDto response = new CredentialsDto();
        response.setUsername(saved.getUsername());
        response.setPassword(saved.getPassword());
        return response;
    }

    @GetMapping("/{username}")
    public TraineeDto get(@PathVariable(value = "username") String username,
                          @RequestHeader(value = "password") String password) {
        return RestMapper.toDto(traineeService.getTraineeByUsername(username, password));
    }

    @PutMapping("/{username}")
    public TraineeDto update(@PathVariable(value = "username") String username,
                             @RequestHeader(value = "password") String password,
                             @RequestBody TraineeDto request) {
        return RestMapper.toDto(traineeService.updateTrainee(username, password, RestMapper.toEntity(request)));
    }

    @DeleteMapping("/{username}")
    public void delete(@PathVariable(value = "username") String username,
                       @RequestHeader(value = "password") String password) {
        traineeService.deleteTraineeByUsername(username, password);
    }

    @PatchMapping("/{username}/active")
    public void setActive(@PathVariable(value = "username") String username,
                          @RequestHeader(value = "password") String password,
                          @RequestParam(value = "active") boolean active) {
        traineeService.setActive(username, password, active);
    }

    @GetMapping("/{username}/trainers/unassigned")
    public List<TrainerShortDto> getUnassignedTrainers(@PathVariable(value = "username") String username,
                                                       @RequestHeader(value = "password") String password) {
        return traineeService.getUnassignedTrainers(username, password)
                .stream()
                .map(RestMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @PutMapping("/{username}/trainers")
    public List<TrainerShortDto> updateTrainers(@PathVariable(value = "username") String username,
                                                @RequestHeader(value = "password") String password,
                                                @RequestBody Set<String> trainerUsernames) {
        return traineeService.updateTraineeTrainers(username, password, trainerUsernames)
                .stream()
                .map(RestMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{username}/trainings")
    public List<TrainingDto> getTrainings(@PathVariable(value = "username") String username,
                                          @RequestHeader(value = "password") String password,
                                          @RequestParam(value = "from", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(value = "to", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(value = "trainerName", required = false) String trainerName,
                                          @RequestParam(value = "trainingType", required = false) String trainingType) {
        return trainingService.getTraineeTrainings(username, password, from, to, trainerName, trainingType)
                .stream()
                .map(RestMapper::toDto)
                .collect(Collectors.toList());
    }
}