package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findBySessionIdOrderByCheckedAtDesc(Long sessionId);

    void deleteBySessionId(Long sessionId);
}
