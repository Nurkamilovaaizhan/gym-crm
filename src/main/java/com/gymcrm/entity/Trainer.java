package com.gymcrm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trainer")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@ToString(exclude = {"trainings", "trainees"})
public class Trainer extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer")
    private List<Training> trainings = new ArrayList<>();

    @ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();
}