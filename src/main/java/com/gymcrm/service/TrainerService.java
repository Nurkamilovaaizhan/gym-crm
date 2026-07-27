package com.gymcrm.service;

import com.gymcrm.entity.Trainer;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final GymMetricsService gymMetricsService;

    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          AuthenticationService authenticationService,
                          GymMetricsService gymMetricsService) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.gymMetricsService = gymMetricsService;
    }

    @Transactional
    public Trainer createTrainer(Trainer trainer) {
        validateForCreate(trainer);
        UserUtils.setupCredentials(trainer, userRepository.findAllUsernames());

        Trainer saved = trainerRepository.save(trainer);
        gymMetricsService.incrementTrainerRegistration();

        log.info("Created trainer profile, username={}", saved.getUsername());
        return saved;
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(String username, String password) {
        authenticationService.authenticate(username, password);
        return trainerRepository.findByUsernameWithTrainees(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
    }

    @Transactional
    public Trainer updateTrainer(String username, String password, Trainer updated) {
        authenticationService.authenticate(username, password);
        validateForUpdate(updated);

        Trainer existing = trainerRepository.findById(updated.getId())
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setActive(updated.isActive());

        log.info("Updating trainer profile, username={}", username);
        return trainerRepository.save(existing);
    }

    @Transactional
    public void setActive(String username, String password, boolean active) {
        authenticationService.authenticate(username, password);

        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        if (trainer.isActive() == active) {
            throw new ValidationException("Trainer already has this active state");
        }

        trainer.setActive(active);
        trainerRepository.save(trainer);
        log.info("Trainer {} isActive set to {}", username, active);
    }

    private void validateForCreate(Trainer trainer) {
        if (trainer.getFirstName() == null || trainer.getFirstName().isBlank()
                || trainer.getLastName() == null || trainer.getLastName().isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
        if (trainer.getSpecialization() == null || trainer.getSpecialization().getId() == null) {
            throw new ValidationException("Trainer specialization is required");
        }
    }

    private void validateForUpdate(Trainer trainer) {
        if (trainer.getId() == null) {
            throw new ValidationException("Trainer id is required for update");
        }
        if (trainer.getFirstName() == null || trainer.getFirstName().isBlank()
                || trainer.getLastName() == null || trainer.getLastName().isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }
}