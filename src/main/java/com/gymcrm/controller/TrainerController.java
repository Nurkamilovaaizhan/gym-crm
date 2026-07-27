package com.gymcrm.controller;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.dto.TrainerDto;
import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainerService;
import com.gymcrm.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;

    public TrainerController(TrainerService trainerService,
                             TrainingService trainingService) {
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @PostMapping
    public CredentialsDto create(@RequestBody TrainerDto request) {
        var saved = trainerService.createTrainer(RestMapper.toEntity(request));

        CredentialsDto response = new CredentialsDto();
        response.setUsername(saved.getUsername());
        response.setPassword(saved.getPassword());
        return response;
    }

    @GetMapping("/{username}")
    public TrainerDto get(@PathVariable(value = "username") String username,
                          @RequestHeader(value = "password") String password) {
        return RestMapper.toDto(trainerService.getTrainerByUsername(username, password));
    }

    @PutMapping("/{username}")
    public TrainerDto update(@PathVariable(value = "username") String username,
                             @RequestHeader(value = "password") String password,
                             @RequestBody TrainerDto request) {
        return RestMapper.toDto(trainerService.updateTrainer(username, password, RestMapper.toEntity(request)));
    }

    @PatchMapping("/{username}/active")
    public void setActive(@PathVariable(value = "username") String username,
                          @RequestHeader(value = "password") String password,
                          @RequestParam(value = "active") boolean active) {
        trainerService.setActive(username, password, active);
    }

    @GetMapping("/{username}/trainings")
    public List<TrainingDto> getTrainings(@PathVariable(value = "username") String username,
                                          @RequestHeader(value = "password") String password,
                                          @RequestParam(value = "from", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(value = "to", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(value = "traineeName", required = false) String traineeName) {
        return trainingService.getTrainerTrainings(username, password, from, to, traineeName)
                .stream()
                .map(RestMapper::toDto)
                .collect(Collectors.toList());
    }
}