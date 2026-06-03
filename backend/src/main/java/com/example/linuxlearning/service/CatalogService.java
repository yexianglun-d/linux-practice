package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.Course;
import com.example.linuxlearning.domain.CourseModule;
import com.example.linuxlearning.domain.Lab;
import com.example.linuxlearning.domain.LabTask;
import com.example.linuxlearning.domain.Lesson;
import com.example.linuxlearning.domain.PublishStatus;
import com.example.linuxlearning.dto.CatalogResponse;
import com.example.linuxlearning.repository.CourseRepository;
import com.example.linuxlearning.repository.LabRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CourseRepository courseRepository;
    private final LabRepository labRepository;

    public CatalogService(CourseRepository courseRepository, LabRepository labRepository) {
        this.courseRepository = courseRepository;
        this.labRepository = labRepository;
    }

    @Transactional(readOnly = true)
    public CatalogResponse catalog() {
        Long firstLabId = labRepository.findAllByStatusOrderByIdAsc(PublishStatus.PUBLISHED)
                .stream()
                .findFirst()
                .map(Lab::getId)
                .orElse(null);
        List<CatalogResponse.LearningPathView> learningPaths = List.of(
                new CatalogResponse.LearningPathView("BEGINNER", "零基础入门", "从命令行、文件、权限开始，逐步进入服务和部署。", firstLabId),
                new CatalogResponse.LearningPathView("OPS", "在职运维提升", "跳过基础热身，优先训练服务、网络、脚本、容器和排障。", firstLabId)
        );
        List<CatalogResponse.CourseView> courses = courseRepository.findAllByStatusOrderBySortOrderAsc(PublishStatus.PUBLISHED)
                .stream()
                .map(this::toCourseView)
                .toList();
        return new CatalogResponse(learningPaths, courses);
    }

    private CatalogResponse.CourseView toCourseView(Course course) {
        return new CatalogResponse.CourseView(
                course.getId(),
                course.getTitle(),
                course.getSummary(),
                course.getModules().stream().map(this::toModuleView).toList()
        );
    }

    private CatalogResponse.ModuleView toModuleView(CourseModule module) {
        return new CatalogResponse.ModuleView(
                module.getId(),
                module.getTitle(),
                module.getOutcome(),
                module.getLessons().stream().map(this::toLessonView).toList()
        );
    }

    private CatalogResponse.LessonView toLessonView(Lesson lesson) {
        return new CatalogResponse.LessonView(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getObjective(),
                lesson.getDurationMinutes(),
                toLabView(lesson.getLab())
        );
    }

    private CatalogResponse.LabView toLabView(Lab lab) {
        if (lab == null) {
            return null;
        }
        return new CatalogResponse.LabView(
                lab.getId(),
                lab.getTitle(),
                lab.getDescription(),
                lab.getImageRef(),
                lab.getCpuCores(),
                lab.getMemoryMb(),
                lab.getTimeoutMinutes(),
                lab.getNetworkWhitelist(),
                lab.getSandboxTier().name(),
                lab.getTasks().stream().map(this::toTaskView).toList()
        );
    }

    private CatalogResponse.TaskView toTaskView(LabTask task) {
        return new CatalogResponse.TaskView(
                task.getId(),
                task.getTitle(),
                task.getInstruction(),
                task.getCheckerType().name(),
                task.getHint(),
                task.getScore()
        );
    }
}
