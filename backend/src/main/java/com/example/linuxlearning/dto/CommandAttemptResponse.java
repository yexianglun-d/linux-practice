package com.example.linuxlearning.dto;

public record CommandAttemptResponse(
        String exerciseId,
        boolean correct,
        String errorType,
        String message,
        String expectedCommand,
        String explanation,
        CommandProgressView progress
) {
}
