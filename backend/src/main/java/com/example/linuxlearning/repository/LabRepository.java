package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabRepository extends JpaRepository<Lab, Long> {

    List<Lab> findAllByStatusOrderByIdAsc(PublishStatus status);
}
