package com.gymcrm.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TraineeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Boolean isActive;
    private LocalDateTime dateOfBirth;
    private String address;
    private List<TrainerShortDto> trainers;
}