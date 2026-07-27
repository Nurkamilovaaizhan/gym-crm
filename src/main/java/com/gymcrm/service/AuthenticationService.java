package com.gymcrm.service;

import com.gymcrm.entity.User;
import com.gymcrm.exception.AuthenticationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final GymMetricsService gymMetricsService;

    public AuthenticationService(UserRepository userRepository,
                                 GymMetricsService gymMetricsService) {
        this.userRepository = userRepository;
        this.gymMetricsService = gymMetricsService;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    gymMetricsService.incrementLoginFailed();
                    log.warn("Authentication failed: user {} not found", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!user.getPassword().equals(password)) {
            gymMetricsService.incrementLoginFailed();
            log.warn("Authentication failed: wrong password for user {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        gymMetricsService.incrementLoginSuccess();
        log.info("User {} authenticated successfully", username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticate(username, oldPassword);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(newPassword);
        userRepository.save(user);

        log.info("Password changed for username={}", username);
    }
}