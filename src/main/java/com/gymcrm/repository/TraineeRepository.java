package com.gymcrm.repository;

import com.gymcrm.entity.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TraineeRepository extends JpaRepository<Trainee, Long> {

    Optional<Trainee> findByUsername(String username);

    @Query("""
            select distinct t from Trainee t
            left join fetch t.trainers tr
            left join fetch tr.specialization
            where t.username = :username
            """)
    Optional<Trainee> findByUsernameWithTrainers(String username);
}