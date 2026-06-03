package com.example.linuxlearning.dto;

public record CheckTaskResponse(
        Long taskId,
        String checkerType,
        boolean passed,
        String message,
        int progressPercent
) {
}
