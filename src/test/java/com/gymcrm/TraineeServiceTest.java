package com.gymcrm.service;

import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationService authenticationService;

    private SimpleMeterRegistry meterRegistry;
    private GymMetricsService gymMetricsService;
    private TraineeService traineeService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        gymMetricsService = new GymMetricsService(meterRegistry);
        traineeService = new TraineeService(
                traineeRepository,
                trainerRepository,
                userRepository,
                authenticationService,
                gymMetricsService
        );
    }

    @Test
    void createTraineeShouldSetCredentialsAndSave() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Alan");
        trainee.setLastName("Walker");

        when(userRepository.findAllUsernames()).thenReturn(Set.of());
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee saved = traineeService.createTrainee(trainee);

        assertEquals("Alan.Walker", saved.getUsername());
        assertNotNull(saved.getPassword());
        assertEquals(10, saved.getPassword().length());
        assertTrue(saved.isActive());
        assertEquals(1.0, meterRegistry.find("gym.trainee.registrations").counter().count());

        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void createTraineeShouldThrowValidationExceptionWhenNamesAreMissing() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Alan");

        assertThrows(ValidationException.class, () -> traineeService.createTrainee(trainee));
        verifyNoInteractions(traineeRepository);
    }

    @Test
    void setActiveShouldBeNonIdempotent() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("Alan.Walker");
        trainee.setActive(true);

        doNothing().when(authenticationService).authenticate("Alan.Walker", "pass");
        when(traineeRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.setActive("Alan.Walker", "pass", true));

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateTraineeTrainersShouldReplaceTrainersList() {
        Trainee trainee = new Trainee();
        trainee.setId(1L);
        trainee.setUsername("Alan.Walker");
        trainee.setTrainers(new HashSet<>());

        Trainer trainer = new Trainer();
        trainer.setId(2L);
        trainer.setUsername("Bob.Marley");

        doNothing().when(authenticationService).authenticate("Alan.Walker", "pass");
        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(Set.of("Bob.Marley"))).thenReturn(Set.of(trainer));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Trainer> updated = traineeService.updateTraineeTrainers("Alan.Walker", "pass", Set.of("Bob.Marley"));

        assertEquals(1, updated.size());
        assertTrue(updated.contains(trainer));
    }
}