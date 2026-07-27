package com.gymcrm.controller;

import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PostMapping
    public TrainingDto create(@RequestHeader(value = "username") String username,
                              @RequestHeader(value = "password") String password,
                              @RequestBody TrainingDto request) {
        var saved = trainingService.addTraining(
                username,
                password,
                request.getTraineeUsername(),
                request.getTrainerUsername(),
                RestMapper.toEntity(request)
        );

        return RestMapper.toDto(saved);
    }
}