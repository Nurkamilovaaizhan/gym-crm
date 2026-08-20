package com.gymcrm.repository;

import com.gymcrm.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByUsername(String username);

    Set<Trainer> findByUsernameIn(Set<String> usernames);

    @Query("""
            select distinct t from Trainer t
            left join fetch t.trainees
            left join fetch t.specialization
            where t.username = :username
            """)
    Optional<Trainer> findByUsernameWithTrainees(String username);

    @Query("""
            select tr from Trainer tr
            where tr.isActive = true
            and tr.id not in (
                select assigned.id from Trainee te join te.trainers assigned
                where te.username = :traineeUsername
            )
            """)
    List<Trainer> findUnassignedTrainersForTrainee(String traineeUsername);
}