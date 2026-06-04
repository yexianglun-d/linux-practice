package com.example.linuxlearning.dto;

import java.util.List;

public record CommandExerciseCatalogResponse(
        String chapterTitle,
        List<CommandExerciseView> exercises,
        CommandProgressView progress
) {
}
