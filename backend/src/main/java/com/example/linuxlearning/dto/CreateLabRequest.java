package com.example.linuxlearning.dto;

import com.example.linuxlearning.domain.SandboxTier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLabRequest(
        @NotNull Long lessonId,
        @NotBlank String title,
        @NotBlank String description,
        @NotBlank String imageRef,
        @Min(1) int cpuCores,
        @Min(512) int memoryMb,
        @Min(5) int timeoutMinutes,
        @NotBlank String networkWhitelist,
        SandboxTier sandboxTier
) {
}
