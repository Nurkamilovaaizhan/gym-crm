package com.gymcrm.service;

import com.gymcrm.entity.User;
import com.gymcrm.exception.AuthenticationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.security.jwt.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AuthenticationService {

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    private final UserRepository userRepository;
    private final GymMetricsService gymMetricsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public AuthenticationService(UserRepository userRepository,
                                 GymMetricsService gymMetricsService,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.gymMetricsService = gymMetricsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public void authenticate(String username, String password) {
        if (isBlocked(username)) {
            log.warn("Authentication blocked for user {}", username);
            throw new AuthenticationException("User is blocked. Try later");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    gymMetricsService.incrementLoginFailed();
                    registerFailure(username);
                    log.warn("Authentication failed: user {} not found", username);
                    return new AuthenticationException("Invalid username or password");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            gymMetricsService.incrementLoginFailed();
            registerFailure(username);
            log.warn("Authentication failed: wrong password for user {}", username);
            throw new AuthenticationException("Invalid username or password");
        }

        gymMetricsService.incrementLoginSuccess();
        registerSuccess(username);
        log.info("User {} authenticated successfully", username);
    }

    @Transactional
    public String login(String username, String password) {
        authenticate(username, password);
        return jwtService.generateToken(username);
    }

    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        authenticate(username, oldPassword);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for username={}", username);
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new AuthenticationException("Bearer token is required");
        }

        blacklistedTokens.add(authorizationHeader.substring(7));
        log.info("User logged out successfully");
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    private boolean isBlocked(String username) {
        Instant until = blockedUntil.get(username);

        if (until == null) {
            return false;
        }

        if (until.isBefore(Instant.now())) {
            blockedUntil.remove(username);
            failedAttempts.remove(username);
            return false;
        }

        return true;
    }

    private void registerFailure(String username) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            blockedUntil.put(username, Instant.now().plus(BLOCK_DURATION));
            failedAttempts.remove(username);
            log.warn("User {} blocked for {} minutes", username, BLOCK_DURATION.toMinutes());
            return;
        }

        failedAttempts.put(username, attempts);
    }

    private void registerSuccess(String username) {
        failedAttempts.remove(username);
        blockedUntil.remove(username);
    }
}