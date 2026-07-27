package com.gymcrm.mapper;

import com.gymcrm.dto.*;
import com.gymcrm.entity.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

public final class RestMapper {

    private RestMapper() {
    }

    public static TrainingTypeDto toDto(TrainingType type) {
        if (type == null) {
            return null;
        }

        TrainingTypeDto dto = new TrainingTypeDto();
        dto.setId(type.getId());
        dto.setTrainingTypeName(type.getTrainingTypeName());
        return dto;
    }

    public static TrainingType toEntity(TrainingTypeDto dto) {
        if (dto == null) {
            return null;
        }

        TrainingType type = new TrainingType();
        type.setId(dto.getId());
        type.setTrainingTypeName(dto.getTrainingTypeName());
        return type;
    }

    public static TrainerShortDto toShortDto(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        TrainerShortDto dto = new TrainerShortDto();
        dto.setUsername(trainer.getUsername());
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setSpecialization(toDto(trainer.getSpecialization()));
        return dto;
    }

    public static TraineeShortDto toShortDto(Trainee trainee) {
        if (trainee == null) {
            return null;
        }

        TraineeShortDto dto = new TraineeShortDto();
        dto.setUsername(trainee.getUsername());
        dto.setFirstName(trainee.getFirstName());
        dto.setLastName(trainee.getLastName());
        return dto;
    }

    public static Trainee toEntity(TraineeDto dto) {
        if (dto == null) {
            return null;
        }

        Trainee trainee = new Trainee();
        trainee.setId(dto.getId());
        trainee.setFirstName(dto.getFirstName());
        trainee.setLastName(dto.getLastName());
        trainee.setDateOfBirth(dto.getDateOfBirth());
        trainee.setAddress(dto.getAddress());

        if (dto.getIsActive() != null) {
            trainee.setActive(dto.getIsActive());
        }

        return trainee;
    }

    public static TraineeDto toDto(Trainee trainee) {
        if (trainee == null) {
            return null;
        }

        TraineeDto dto = new TraineeDto();
        dto.setId(trainee.getId());
        dto.setFirstName(trainee.getFirstName());
        dto.setLastName(trainee.getLastName());
        dto.setUsername(trainee.getUsername());
        dto.setPassword(trainee.getPassword());
        dto.setIsActive(trainee.isActive());
        dto.setDateOfBirth(trainee.getDateOfBirth());
        dto.setAddress(trainee.getAddress());

        if (trainee.getTrainers() != null) {
            dto.setTrainers(trainee.getTrainers().stream()
                    .map(RestMapper::toShortDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setTrainers(new ArrayList<>());
        }

        return dto;
    }

    public static Trainer toEntity(TrainerDto dto) {
        if (dto == null) {
            return null;
        }

        Trainer trainer = new Trainer();
        trainer.setId(dto.getId());
        trainer.setFirstName(dto.getFirstName());
        trainer.setLastName(dto.getLastName());

        if (dto.getIsActive() != null) {
            trainer.setActive(dto.getIsActive());
        }

        trainer.setSpecialization(toEntity(dto.getSpecialization()));
        return trainer;
    }

    public static TrainerDto toDto(Trainer trainer) {
        if (trainer == null) {
            return null;
        }

        TrainerDto dto = new TrainerDto();
        dto.setId(trainer.getId());
        dto.setFirstName(trainer.getFirstName());
        dto.setLastName(trainer.getLastName());
        dto.setUsername(trainer.getUsername());
        dto.setPassword(trainer.getPassword());
        dto.setIsActive(trainer.isActive());
        dto.setSpecialization(toDto(trainer.getSpecialization()));

        if (trainer.getTrainees() != null) {
            dto.setTrainees(trainer.getTrainees().stream()
                    .map(RestMapper::toShortDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setTrainees(new ArrayList<>());
        }

        return dto;
    }

    public static Training toEntity(TrainingDto dto) {
        if (dto == null) {
            return null;
        }

        Training training = new Training();
        training.setTrainingName(dto.getTrainingName());
        training.setTrainingDate(dto.getTrainingDate());
        training.setTrainingDuration(dto.getTrainingDuration());
        return training;
    }

    public static TrainingDto toDto(Training training) {
        if (training == null) {
            return null;
        }

        TrainingDto dto = new TrainingDto();
        dto.setTrainingName(training.getTrainingName());
        dto.setTrainingDate(training.getTrainingDate());
        dto.setTrainingDuration(training.getTrainingDuration());
        dto.setTrainingType(toDto(training.getTrainingType()));

        if (training.getTrainee() != null) {
            dto.setTraineeUsername(training.getTrainee().getUsername());
            dto.setTraineeName(training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName());
        }

        if (training.getTrainer() != null) {
            dto.setTrainerUsername(training.getTrainer().getUsername());
            dto.setTrainerName(training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName());
        }

        return dto;
    }
}