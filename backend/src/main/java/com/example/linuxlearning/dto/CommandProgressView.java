package com.example.linuxlearning.dto;

import java.util.List;

public record CommandProgressView(
        int totalExercises,
        int completedExercises,
        List<String> completedExerciseIds,
        int attempts,
        int correctAttempts,
        int currentStreak,
        int bestStreak,
        int accuracyPercent,
        String mastery
) {
}
