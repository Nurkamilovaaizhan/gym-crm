package com.gymcrm.controller;

import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainingService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public TrainingDto create(@RequestBody TrainingDto request) {
        var saved = trainingService.addTraining(
                request.getTraineeUsername(),
                request.getTrainerUsername(),
                RestMapper.toEntity(request)
        );

        return RestMapper.toDto(saved);
    }
}