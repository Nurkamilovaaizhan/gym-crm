package com.gymcrm.controller;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.dto.TraineeDto;
import com.gymcrm.dto.TrainerShortDto;
import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TraineeService;
import com.gymcrm.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return traineeService.createTrainee(RestMapper.toEntity(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{username}")
    public TraineeDto get(@PathVariable("username") String username) {
        return RestMapper.toDto(traineeService.getTraineeByUsername(username));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{username}")
    public TraineeDto update(@PathVariable("username") String username,
                             @RequestBody TraineeDto request) {
        return RestMapper.toDto(traineeService.updateTrainee(username, RestMapper.toEntity(request)));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{username}")
    public void delete(@PathVariable("username") String username) {
        traineeService.deleteTraineeByUsername(username);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{username}/active")
    public void setActive(@PathVariable("username") String username,
                          @RequestParam("active") boolean active) {
        traineeService.setActive(username, active);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{username}/trainers/unassigned")
    public List<TrainerShortDto> getUnassignedTrainers(@PathVariable("username") String username) {
        return traineeService.getUnassignedTrainers(username)
                .stream()
                .map(RestMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{username}/trainers")
    public List<TrainerShortDto> updateTrainers(@PathVariable("username") String username,
                                                @RequestBody Set<String> trainerUsernames) {
        return traineeService.updateTraineeTrainers(username, trainerUsernames)
                .stream()
                .map(RestMapper::toShortDto)
                .collect(Collectors.toList());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{username}/trainings")
    public List<TrainingDto> getTrainings(@PathVariable("username") String username,
                                          @RequestParam(value = "from", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(value = "to", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(value = "trainerName", required = false) String trainerName,
                                          @RequestParam(value = "trainingType", required = false) String trainingType) {
        return trainingService.getTraineeTrainings(username, from, to, trainerName, trainingType)
                .stream()
                .map(RestMapper::toDto)
                .collect(Collectors.toList());
    }
}