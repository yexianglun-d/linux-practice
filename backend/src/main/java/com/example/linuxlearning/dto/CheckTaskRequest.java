package com.example.linuxlearning.dto;

import jakarta.validation.constraints.NotNull;

public record CheckTaskRequest(
        @NotNull Long taskId,
        String commandSummary
) {
}
