package com.gymcrm;

import com.gymcrm.entity.TrainingType;
import com.gymcrm.repository.TrainingTypeRepository;
import com.gymcrm.service.TrainingTypeService;
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
    void getAllTrainingTypes_shouldReturnAllRecords() {
        TrainingType t1 = new TrainingType();
        t1.setId(1L);
        t1.setTrainingTypeName("Yoga");

        TrainingType t2 = new TrainingType();
        t2.setId(2L);
        t2.setTrainingTypeName("Strength");

        when(trainingTypeRepository.findAll()).thenReturn(List.of(t1, t2));

        List<TrainingType> result = trainingTypeService.getAllTrainingTypes();

        assertEquals(2, result.size());
        assertEquals("Yoga", result.get(0).getTrainingTypeName());
        assertEquals("Strength", result.get(1).getTrainingTypeName());
    }
}