package com.example.linuxlearning.dto;

import java.util.List;

public record CommandExerciseView(
        String id,
        String title,
        String scenario,
        String expectedCommand,
        List<String> acceptedCommands,
        String hint,
        String explanation,
        String difficulty,
        List<String> tags,
        int sortOrder
) {
}
