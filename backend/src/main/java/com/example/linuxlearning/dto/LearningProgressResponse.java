package com.example.linuxlearning.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LearningProgressResponse(
        List<ProgressRow> rows
) {

    public record ProgressRow(
            Long sessionId,
            String username,
            String displayName,
            String labTitle,
            String status,
            int progressPercent,
            OffsetDateTime startedAt,
            OffsetDateTime expiresAt
    ) {
    }
}
