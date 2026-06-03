package com.example.linuxlearning.dto;

import jakarta.validation.constraints.NotNull;

public record StartLabSessionRequest(
        Long userId,
        @NotNull Long labId
) {
}
