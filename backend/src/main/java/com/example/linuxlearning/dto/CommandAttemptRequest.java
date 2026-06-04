package com.example.linuxlearning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record CommandAttemptRequest(
        @NotBlank
        String input,
        @PositiveOrZero
        long elapsedMs
) {
}
