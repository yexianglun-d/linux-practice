package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.LearningPath;
import com.example.linuxlearning.domain.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LabRepository extends JpaRepository<Lab, Long> {

    Optional<Lab> findFirstByStatusAndLearningPathOrderByIdAsc(PublishStatus status, LearningPath learningPath);
}
