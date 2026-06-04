package com.example.linuxlearning;

import com.example.linuxlearning.dto.CheckTaskRequest;
import com.example.linuxlearning.dto.CheckTaskResponse;
import com.example.linuxlearning.dto.LabSessionResponse;
import com.example.linuxlearning.dto.StartDefaultLabSessionRequest;
import com.example.linuxlearning.domain.LearningPath;
import com.example.linuxlearning.service.LabSessionService;
import com.example.linuxlearning.service.TerminalTranscriptService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LabSessionServiceTest {

    @Autowired
    private LabSessionService labSessionService;

    @Autowired
    private TerminalTranscriptService transcriptService;

    @Test
    void shouldStartCheckAndResetLabSession() {
        LabSessionResponse session = labSessionService.startDefault(null);

        assertThat(session.status()).isEqualTo("RUNNING");
        assertThat(session.vmId()).startsWith("vm-");
        assertThat(session.tasks()).isNotEmpty();
        assertThat(session.tasks().getFirst().instruction()).contains("pwd");
        assertThat(session.tasks().getFirst().hint()).isNotBlank();
        assertThat(session.tasks().getFirst().score()).isPositive();

        Long firstTaskId = session.tasks().getFirst().taskId();
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

    @Test
    void shouldStartOpsDefaultLabSession() {
        LabSessionResponse session = labSessionService.startDefault(new StartDefaultLabSessionRequest(LearningPath.OPS));

        assertThat(session.status()).isEqualTo("RUNNING");
        assertThat(session.labTitle()).contains("Nginx");
    }
}
