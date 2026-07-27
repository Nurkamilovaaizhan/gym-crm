package com.gymcrm.service;

import com.gymcrm.entity.TrainingType;
import com.gymcrm.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAllTrainingTypesShouldReturnAllItems() {
        TrainingType type1 = new TrainingType();
        type1.setId(1L);
        type1.setTrainingTypeName("Cardio");

        TrainingType type2 = new TrainingType();
        type2.setId(2L);
        type2.setTrainingTypeName("Strength");

        when(trainingTypeRepository.findAll()).thenReturn(List.of(type1, type2));

        List<TrainingType> result = trainingTypeService.getAllTrainingTypes();

        assertEquals(2, result.size());
    }
}