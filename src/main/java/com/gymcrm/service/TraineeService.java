package com.gymcrm.service;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymMetricsService gymMetricsService;
    private final PasswordEncoder passwordEncoder;

    public TraineeService(TraineeRepository traineeRepository,
                          TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          GymMetricsService gymMetricsService,
                          PasswordEncoder passwordEncoder) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.gymMetricsService = gymMetricsService;
        this.passwordEncoder = passwordEncoder;
    }

    public CredentialsDto createTrainee(Trainee trainee) {
        validateForCreate(trainee);

        String username = UserUtils.generateUsername(
                trainee.getFirstName(),
                trainee.getLastName(),
                userRepository.findAllUsernames()
        );
        String rawPassword = UserUtils.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(passwordEncoder.encode(rawPassword));
        trainee.setActive(true);

        traineeRepository.save(trainee);

        gymMetricsService.incrementTraineeRegistration();
        log.info("Created trainee profile, username={}", username);

        CredentialsDto response = new CredentialsDto();
        response.setUsername(username);
        response.setPassword(rawPassword);
        return response;
    }

    @Transactional(readOnly = true)
    public Trainee getTraineeByUsername(String username) {
        return traineeRepository.findByUsernameWithTrainers(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));
    }

    @Transactional
    public Trainee updateTrainee(String username, Trainee updated) {
        validateForUpdate(updated);

        Trainee existing = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setAddress(updated.getAddress());
        existing.setActive(updated.isActive());

        log.info("Updating trainee profile, username={}", username);
        return traineeRepository.save(existing);
    }

    @Transactional
    public void deleteTraineeByUsername(String username) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        traineeRepository.delete(trainee);
        log.info("Deleted trainee username={}", username);
    }

    @Transactional
    public void setActive(String username, boolean active) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        if (trainee.isActive() == active) {
            throw new ValidationException("Trainee already has this active state");
        }

        trainee.setActive(active);
        traineeRepository.save(trainee);
        log.info("Trainee {} isActive set to {}", username, active);
    }

    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(String traineeUsername) {
        return trainerRepository.findUnassignedTrainersForTrainee(traineeUsername);
    }

    @Transactional
    public Set<Trainer> updateTraineeTrainers(String username, Set<String> trainerUsernames) {
        Trainee trainee = traineeRepository.findByUsernameWithTrainers(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainee not found"));

        Set<Trainer> newTrainers = trainerRepository.findByUsernameIn(trainerUsernames);
        trainee.setTrainers(newTrainers);

        Trainee updated = traineeRepository.save(trainee);
        log.info("Updated trainers list for trainee username={}", username);
        return updated.getTrainers();
    }

    private void validateForCreate(Trainee trainee) {
        if (trainee.getFirstName() == null || trainee.getFirstName().isBlank()
                || trainee.getLastName() == null || trainee.getLastName().isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }

    private void validateForUpdate(Trainee trainee) {
        if (trainee.getFirstName() == null || trainee.getFirstName().isBlank()
                || trainee.getLastName() == null || trainee.getLastName().isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }
}