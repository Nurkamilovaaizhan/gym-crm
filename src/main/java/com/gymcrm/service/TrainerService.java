package com.gymcrm.service;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.entity.Trainer;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;
    private final GymMetricsService gymMetricsService;
    private final PasswordEncoder passwordEncoder;

    public TrainerService(TrainerRepository trainerRepository,
                          UserRepository userRepository,
                          GymMetricsService gymMetricsService,
                          PasswordEncoder passwordEncoder) {
        this.trainerRepository = trainerRepository;
        this.userRepository = userRepository;
        this.gymMetricsService = gymMetricsService;
        this.passwordEncoder = passwordEncoder;
    }

    public CredentialsDto createTrainer(Trainer trainer) {
        validateForCreate(trainer);

        String username = UserUtils.generateUsername(
                trainer.getFirstName(),
                trainer.getLastName(),
                userRepository.findAllUsernames()
        );
        String rawPassword = UserUtils.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(passwordEncoder.encode(rawPassword));
        trainer.setActive(true);

        trainerRepository.save(trainer);

        gymMetricsService.incrementTrainerRegistration();
        log.info("Created trainer profile, username={}", username);

        CredentialsDto response = new CredentialsDto();
        response.setUsername(username);
        response.setPassword(rawPassword);
        return response;
    }

    @Transactional(readOnly = true)
    public Trainer getTrainerByUsername(String username) {
        return trainerRepository.findByUsernameWithTrainees(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
    }

    @Transactional
    public Trainer updateTrainer(String username, Trainer updated) {
        validateForUpdate(updated);

        Trainer existing = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setActive(updated.isActive());

        log.info("Updating trainer profile, username={}", username);
        return trainerRepository.save(existing);
    }

    @Transactional
    public void setActive(String username, boolean active) {
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
        if (trainer.getFirstName() == null || trainer.getFirstName().isBlank()
                || trainer.getLastName() == null || trainer.getLastName().isBlank()) {
            throw new ValidationException("First name and last name are required");
        }
    }
}