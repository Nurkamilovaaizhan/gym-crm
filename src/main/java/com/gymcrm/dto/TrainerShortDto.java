package com.gymcrm.dto;

import lombok.Data;

@Data
public class TrainerShortDto {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingTypeDto specialization;
}