package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.LabSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabSessionRepository extends JpaRepository<LabSession, Long> {

    List<LabSession> findTop30ByOrderByStartedAtDesc();
}
