package com.gymcrm.repository;

import com.gymcrm.entity.Training;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {

    @Query("""
            select distinct t from Training t
            join fetch t.trainee trainee
            join fetch t.trainer trainer
            join fetch t.trainingType type
            where trainee.username = :traineeUsername
            and (:from is null or t.trainingDate >= :from)
            and (:to is null or t.trainingDate <= :to)
            and (:trainerName is null or concat(trainer.firstName, ' ', trainer.lastName) like concat('%', :trainerName, '%'))
            and (:trainingType is null or type.trainingTypeName = :trainingType)
            """)
    List<Training> findByTraineeCriteria(String traineeUsername,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String trainerName,
                                         String trainingType);

    @Query("""
            select distinct t from Training t
            join fetch t.trainee trainee
            join fetch t.trainer trainer
            join fetch t.trainingType type
            where trainer.username = :trainerUsername
            and (:from is null or t.trainingDate >= :from)
            and (:to is null or t.trainingDate <= :to)
            and (:traineeName is null or concat(trainee.firstName, ' ', trainee.lastName) like concat('%', :traineeName, '%'))
            """)
    List<Training> findByTrainerCriteria(String trainerUsername,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String traineeName);
}