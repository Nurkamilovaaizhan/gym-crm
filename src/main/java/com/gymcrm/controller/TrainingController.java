package com.gymcrm.controller;

import com.gymcrm.dto.TrainingDto;
import com.gymcrm.mapper.RestMapper;
import com.gymcrm.service.TrainingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/trainings")
public class TrainingController {

    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public TrainingDto create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                              @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
                              @RequestBody TrainingDto request) {
        var saved = trainingService.addTraining(
                authorization,
                transactionId != null && !transactionId.isBlank() ? transactionId : UUID.randomUUID().toString(),
                request.getTraineeUsername(),
                request.getTrainerUsername(),
                RestMapper.toEntity(request)
        );
        return RestMapper.toDto(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{trainingId}")
    public ResponseEntity<Void> delete(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
                                       @RequestHeader(value = "X-Transaction-Id", required = false) String transactionId,
                                       @PathVariable("trainingId") Long trainingId) {
        trainingService.deleteTraining(
                authorization,
                transactionId != null && !transactionId.isBlank() ? transactionId : UUID.randomUUID().toString(),
                trainingId
        );
        return ResponseEntity.ok().build();
    }
}