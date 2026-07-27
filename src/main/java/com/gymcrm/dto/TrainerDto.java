package com.gymcrm.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrainerDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;
    private TrainingTypeDto specialization;
    private List<TraineeShortDto> trainees;
}