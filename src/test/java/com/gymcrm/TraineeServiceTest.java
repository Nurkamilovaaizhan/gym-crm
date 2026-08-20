package com.gymcrm;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.entity.Trainee;
import com.gymcrm.entity.Trainer;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TraineeRepository;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.service.TraineeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
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
    private GymMetricsService gymMetricsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TraineeService traineeService;

    private Trainee trainee;

    @BeforeEach
    void setUp() {
        trainee = new Trainee();
        trainee.setFirstName("Alan");
        trainee.setLastName("Walker");
        trainee.setDateOfBirth(LocalDateTime.of(1995, 5, 15, 0, 0));
        trainee.setAddress("Bishkek");
    }

    @Test
    void createTrainee_shouldGenerateUsernameAndPassword() {
        when(userRepository.findAllUsernames()).thenReturn(new HashSet<>());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CredentialsDto credentials = traineeService.createTrainee(trainee);

        assertEquals("Alan.Walker", credentials.getUsername());
        assertNotNull(credentials.getPassword());
        assertEquals(10, credentials.getPassword().length());

        assertEquals("Alan.Walker", trainee.getUsername());
        assertEquals("$2a$10$encodedPassword", trainee.getPassword());
        assertTrue(trainee.isActive());

        verify(traineeRepository).save(any(Trainee.class));
        verify(gymMetricsService).incrementTraineeRegistration();
    }

    @Test
    void createTrainee_shouldThrowWhenNamesMissing() {
        Trainee invalid = new Trainee();
        invalid.setFirstName("");
        invalid.setLastName("");

        assertThrows(ValidationException.class, () -> traineeService.createTrainee(invalid));
    }

    @Test
    void getTraineeByUsername_shouldReturnTrainee() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.getTraineeByUsername("Alan.Walker");

        assertEquals("Alan.Walker", result.getUsername());
    }

    @Test
    void updateTrainee_shouldUpdateFields() {
        Trainee existing = new Trainee();
        existing.setUsername("Alan.Walker");
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));
        existing.setAddress("Old address");
        existing.setActive(true);

        Trainee updated = new Trainee();
        updated.setFirstName("New");
        updated.setLastName("Surname");
        updated.setDateOfBirth(LocalDateTime.of(2000, 1, 1, 0, 0));
        updated.setAddress("New address");
        updated.setActive(false);

        when(traineeRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainee result = traineeService.updateTrainee("Alan.Walker", updated);

        assertEquals("New", result.getFirstName());
        assertEquals("Surname", result.getLastName());
        assertEquals("New address", result.getAddress());
        assertFalse(result.isActive());

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        assertEquals("New", captor.getValue().getFirstName());
    }

    @Test
    void deleteTraineeByUsername_shouldDelete() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");

        when(traineeRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(trainee));
        doNothing().when(traineeRepository).delete(trainee);

        traineeService.deleteTraineeByUsername("Alan.Walker");

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void setActive_shouldUpdateState() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");
        trainee.setActive(true);

        when(traineeRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        traineeService.setActive("Alan.Walker", false);

        assertFalse(trainee.isActive());
        verify(traineeRepository).save(trainee);
    }

    @Test
    void setActive_shouldThrowWhenStateAlreadySet() {
        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");
        trainee.setActive(true);

        when(traineeRepository.findByUsername("Alan.Walker")).thenReturn(Optional.of(trainee));

        assertThrows(ValidationException.class,
                () -> traineeService.setActive("Alan.Walker", true));
    }

    @Test
    void updateTraineeTrainers_shouldReplaceTrainersSet() {
        Trainer trainer1 = new Trainer();
        trainer1.setUsername("Max.Verstappen");

        Trainer trainer2 = new Trainer();
        trainer2.setUsername("Lewis.Hamilton");

        Trainee trainee = new Trainee();
        trainee.setUsername("Alan.Walker");
        trainee.setTrainers(new HashSet<>());

        when(traineeRepository.findByUsernameWithTrainers("Alan.Walker")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUsernameIn(Set.of("Max.Verstappen", "Lewis.Hamilton")))
                .thenReturn(Set.of(trainer1, trainer2));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Set<Trainer> result = traineeService.updateTraineeTrainers(
                "Alan.Walker",
                Set.of("Max.Verstappen", "Lewis.Hamilton")
        );

        assertEquals(2, result.size());
        assertTrue(result.contains(trainer1));
        assertTrue(result.contains(trainer2));

        ArgumentCaptor<Trainee> captor = ArgumentCaptor.forClass(Trainee.class);
        verify(traineeRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getTrainers().size());
    }

    @Test
    void getUnassignedTrainers_shouldReturnList() {
        when(trainerRepository.findUnassignedTrainersForTrainee("Alan.Walker")).thenReturn(java.util.List.of());

        assertTrue(traineeService.getUnassignedTrainers("Alan.Walker").isEmpty());
    }
}