package com.example.linuxlearning.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequest(
        @NotBlank String title,
        @NotBlank String summary,
        String initialModuleTitle,
        String initialLessonTitle
) {
}
