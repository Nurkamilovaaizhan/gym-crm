package com.gymcrm.controller;

import com.gymcrm.dto.TrainingTypeDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainingTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training-types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @GetMapping
    public List<TrainingTypeDto> getAll() {
        return trainingTypeService.getAllTrainingTypes()
                .stream()
                .map(RestMapper::toDto)
                .collect(Collectors.toList());
    }
}