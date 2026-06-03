package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.LabTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabTaskRepository extends JpaRepository<LabTask, Long> {

    List<LabTask> findByLabIdOrderBySortOrderAsc(Long labId);
}
