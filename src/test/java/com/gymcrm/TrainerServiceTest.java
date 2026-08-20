package com.gymcrm;

import com.gymcrm.dto.CredentialsDto;
import com.gymcrm.entity.Trainer;
import com.gymcrm.entity.TrainingType;
import com.gymcrm.exception.ValidationException;
import com.gymcrm.monitoring.metrics.GymMetricsService;
import com.gymcrm.repository.TrainerRepository;
import com.gymcrm.repository.UserRepository;
import com.gymcrm.service.TrainerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GymMetricsService gymMetricsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TrainerService trainerService;

    private TrainingType specialization;

    @BeforeEach
    void setUp() {
        specialization = new TrainingType();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Strength");
    }

    @Test
    void createTrainer_shouldGenerateUsernameAndPassword() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");
        trainer.setSpecialization(specialization);

        when(userRepository.findAllUsernames()).thenReturn(new HashSet<>());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$encodedPassword");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CredentialsDto credentials = trainerService.createTrainer(trainer);

        assertEquals("Max.Verstappen", credentials.getUsername());
        assertNotNull(credentials.getPassword());
        assertEquals(10, credentials.getPassword().length());

        assertEquals("Max.Verstappen", trainer.getUsername());
        assertEquals("$2a$10$encodedPassword", trainer.getPassword());
        assertTrue(trainer.isActive());

        verify(trainerRepository).save(any(Trainer.class));
        verify(gymMetricsService).incrementTrainerRegistration();
    }

    @Test
    void createTrainer_shouldThrowWhenSpecializationMissing() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Max");
        trainer.setLastName("Verstappen");

        assertThrows(ValidationException.class, () -> trainerService.createTrainer(trainer));
    }

    @Test
    void getTrainerByUsername_shouldReturnTrainer() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");

        when(trainerRepository.findByUsernameWithTrainees("Max.Verstappen")).thenReturn(Optional.of(trainer));

        Trainer result = trainerService.getTrainerByUsername("Max.Verstappen");

        assertEquals("Max.Verstappen", result.getUsername());
    }

    @Test
    void updateTrainer_shouldUpdateFields() {
        Trainer existing = new Trainer();
        existing.setUsername("Max.Verstappen");
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setActive(true);

        Trainer updated = new Trainer();
        updated.setFirstName("New");
        updated.setLastName("Surname");
        updated.setActive(false);

        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Trainer result = trainerService.updateTrainer("Max.Verstappen", updated);

        assertEquals("New", result.getFirstName());
        assertEquals("Surname", result.getLastName());
        assertFalse(result.isActive());

        ArgumentCaptor<Trainer> captor = ArgumentCaptor.forClass(Trainer.class);
        verify(trainerRepository).save(captor.capture());
        assertEquals("New", captor.getValue().getFirstName());
    }

    @Test
    void setActive_shouldUpdateState() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");
        trainer.setActive(true);

        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        trainerService.setActive("Max.Verstappen", false);

        assertFalse(trainer.isActive());
        verify(trainerRepository).save(trainer);
    }

    @Test
    void setActive_shouldThrowWhenStateAlreadySet() {
        Trainer trainer = new Trainer();
        trainer.setUsername("Max.Verstappen");
        trainer.setActive(true);

        when(trainerRepository.findByUsername("Max.Verstappen")).thenReturn(Optional.of(trainer));

        assertThrows(ValidationException.class,
                () -> trainerService.setActive("Max.Verstappen", true));
    }
}