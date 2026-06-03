package com.example.linuxlearning.dto;

import com.example.linuxlearning.domain.CheckerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotNull Long labId,
        @NotBlank String title,
        @NotBlank String instruction,
        @NotNull CheckerType checkerType,
        @NotBlank String expected,
        @NotBlank String hint,
        @Min(1) int score
) {
}
