package com.example.linuxlearning.dto;

import com.example.linuxlearning.domain.LearningPath;

public record StartDefaultLabSessionRequest(
        LearningPath learningPath
) {
}
