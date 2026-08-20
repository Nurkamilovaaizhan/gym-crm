package com.gymcrm.controller;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.dto.TrainerDto;
import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainerService;
import com.gymcrm.service.TrainingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
        return trainerService.createTrainer(RestMapper.toEntity(request));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{username}")
    public TrainerDto get(@PathVariable("username") String username) {
        return RestMapper.toDto(trainerService.getTrainerByUsername(username));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{username}")
    public TrainerDto update(@PathVariable("username") String username,
                             @RequestBody TrainerDto request) {
        return RestMapper.toDto(trainerService.updateTrainer(username, RestMapper.toEntity(request)));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{username}/active")
    public void setActive(@PathVariable("username") String username,
                          @RequestParam("active") boolean active) {
        trainerService.setActive(username, active);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{username}/trainings")
    public List<TrainingDto> getTrainings(@PathVariable("username") String username,
                                          @RequestParam(value = "from", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                          @RequestParam(value = "to", required = false)
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                          @RequestParam(value = "traineeName", required = false) String traineeName) {
        return trainingService.getTrainerTrainings(username, from, to, traineeName)
                .stream()
                .map(RestMapper::toDto)
                .collect(Collectors.toList());
    }
}