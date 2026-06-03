package com.example.linuxlearning.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record LabSessionResponse(
        Long id,
        Long labId,
        String labTitle,
        String status,
        String vmId,
        String vmHost,
        String snapshotRef,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt,
        OffsetDateTime expiresAt,
        int progressPercent,
        List<TaskProgressView> tasks
) {

    public record TaskProgressView(
            Long taskId,
            String title,
            String checkerType,
            boolean passed,
            String lastMessage,
            OffsetDateTime checkedAt
    ) {
    }
}
