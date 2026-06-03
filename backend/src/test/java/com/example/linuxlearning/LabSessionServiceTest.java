package com.example.linuxlearning;

import com.example.linuxlearning.dto.CatalogResponse;
import com.example.linuxlearning.dto.CheckTaskRequest;
import com.example.linuxlearning.dto.CheckTaskResponse;
import com.example.linuxlearning.dto.LabSessionResponse;
import com.example.linuxlearning.dto.StartLabSessionRequest;
import com.example.linuxlearning.service.CatalogService;
import com.example.linuxlearning.service.LabSessionService;
import com.example.linuxlearning.service.TerminalTranscriptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LabSessionServiceTest {

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private LabSessionService labSessionService;

    @Autowired
    private TerminalTranscriptService transcriptService;

    @Test
    void shouldStartCheckAndResetLabSession() {
        CatalogResponse.LabView lab = firstLab();
        LabSessionResponse session = labSessionService.start(new StartLabSessionRequest(null, lab.id()));

        assertThat(session.status()).isEqualTo("RUNNING");
        assertThat(session.vmId()).startsWith("vm-");
        assertThat(session.tasks()).hasSameSizeAs(lab.tasks());

        Long firstTaskId = lab.tasks().getFirst().id();
        transcriptService.append(session.id(), "$ pwd\n/home/student");
        CheckTaskResponse checkResult = labSessionService.check(
                session.id(),
                new CheckTaskRequest(firstTaskId, "pwd\n/home/student")
        );

        assertThat(checkResult.passed()).isTrue();
        assertThat(checkResult.progressPercent()).isGreaterThan(0);

        LabSessionResponse resetSession = labSessionService.reset(session.id());
        assertThat(resetSession.status()).isEqualTo("RUNNING");
        assertThat(resetSession.progressPercent()).isZero();
        assertThat(resetSession.tasks()).allMatch(task -> !task.passed());
    }

    private CatalogResponse.LabView firstLab() {
        return catalogService.catalog()
                .courses()
                .getFirst()
                .modules()
                .getFirst()
                .lessons()
                .getFirst()
                .lab();
    }
}
