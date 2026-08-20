package com.gymcrm.dto;

import com.gymcrm.constants.enums.ActionType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TrainerWorkloadRequest {
    private String trainerUsername;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean isActive;
    private LocalDateTime trainingDate;
    private int trainingDuration;
    private ActionType actionType;
}