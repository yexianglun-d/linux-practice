package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.Course;
import com.example.linuxlearning.domain.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findAllByStatusOrderBySortOrderAsc(PublishStatus status);
}
