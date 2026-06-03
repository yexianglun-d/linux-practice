package com.example.linuxlearning.repository;

import com.example.linuxlearning.domain.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
}
