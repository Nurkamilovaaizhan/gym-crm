package com.gymcrm.service;

import com.gymcrm.entity.User;
import com.gymcrm.exception.AuthenticationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    private SimpleMeterRegistry meterRegistry;
    private GymMetricsService gymMetricsService;
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetricsService = new GymMetricsService(meterRegistry);
        authenticationService = new AuthenticationService(userRepository, gymMetricsService);
    }

    @Test
    void authenticateShouldIncreaseSuccessCounterWhenCredentialsMatch() {
        User user = new TestUser();
        user.setUsername("Alan.Walker");
        user.setPassword("pass123");
        when(userRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> authenticationService.authenticate("Alan.Walker", "pass123"));

        assertEquals(1.0, meterRegistry.find("gym.login.success").counter().count());
        assertEquals(0.0, meterRegistry.find("gym.login.failed").counter().count());
    }

    @Test
    void authenticateShouldIncreaseFailedCounterWhenUserNotFound() {
        when(userRepository.findByUsername("Missing.User")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("Missing.User", "pass"));

        assertEquals(1.0, meterRegistry.find("gym.login.failed").counter().count());
        assertEquals(0.0, meterRegistry.find("gym.login.success").counter().count());
    }

    @Test
    void authenticateShouldIncreaseFailedCounterWhenPasswordIsWrong() {
        User user = new TestUser();
        user.setUsername("Alan.Walker");
        user.setPassword("pass123");
        when(userRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate("Alan.Walker", "wrong"));

        assertEquals(1.0, meterRegistry.find("gym.login.failed").counter().count());
        assertEquals(0.0, meterRegistry.find("gym.login.success").counter().count());
    }

    @Test
    void changePasswordShouldUpdatePassword() {
        User user = new TestUser();
        user.setUsername("Alan.Walker");
        user.setPassword("oldPass");
        when(userRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(user), Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.changePassword("Alan.Walker", "oldPass", "newPass");

        assertEquals("newPass", user.getPassword());
        verify(userRepository, times(2)).findByUsername("Alan.Walker");
        verify(userRepository).save(user);
    }

    private static class TestUser extends User {
    }
}