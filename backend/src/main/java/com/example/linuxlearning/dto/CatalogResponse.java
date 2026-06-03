package com.example.linuxlearning.dto;

import java.util.List;

public record CatalogResponse(
        List<LearningPathView> learningPaths,
        List<CourseView> courses
) {

    public record LearningPathView(
            String code,
            String title,
            String description,
            Long recommendedFirstLabId
    ) {
    }

    public record CourseView(
            Long id,
            String title,
            String summary,
            List<ModuleView> modules
    ) {
    }

    public record ModuleView(
            Long id,
            String title,
            String outcome,
            List<LessonView> lessons
    ) {
    }

    public record LessonView(
            Long id,
            String title,
            String objective,
            int durationMinutes,
            LabView lab
    ) {
    }

    public record LabView(
            Long id,
            String title,
            String description,
            String imageRef,
            int cpuCores,
            int memoryMb,
            int timeoutMinutes,
            String networkWhitelist,
            String sandboxTier,
            List<TaskView> tasks
    ) {
    }

    public record TaskView(
            Long id,
            String title,
            String instruction,
            String checkerType,
            String hint,
            int score
    ) {
    }
}
