package com.example.linuxlearning;

import com.example.linuxlearning.service.InMemoryVmSandboxClient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryVmSandboxClientTest {

    private final InMemoryVmSandboxClient sandboxClient = new InMemoryVmSandboxClient();

    @Test
    void shouldReturnUsefulOutputForCommonCommands() {
        Long sessionId = 7L;
        sandboxClient.terminalIntro(sessionId);

        String listOutput = sandboxClient.handleTerminalInput(sessionId, "ll");
        assertThat(listOutput)
                .contains("README.md")
                .doesNotContain("command accepted");

        assertThat(sandboxClient.handleTerminalInput(sessionId, "cd //"))
                .contains("已切换到 /");
        assertThat(sandboxClient.handleTerminalInput(sessionId, "pwd"))
                .isEqualTo("/");

        assertThat(sandboxClient.handleTerminalInput(sessionId, "touch /tmp/linux-foundation/permission-ok"))
                .contains("created /tmp/linux-foundation/permission-ok");
        assertThat(sandboxClient.handleTerminalInput(sessionId, "ls /tmp/linux-foundation"))
                .contains("permission-ok");
    }

    @Test
    void shouldExplainInvalidCommandsWithSuggestions() {
        Long sessionId = 9L;
        sandboxClient.terminalIntro(sessionId);

        assertThat(sandboxClient.handleTerminalInput(sessionId, "root"))
                .contains("bash: root: command not found")
                .contains("whoami");
        assertThat(sandboxClient.handleTerminalInput(sessionId, "hinit"))
                .contains("Did you mean: hint");
        assertThat(sandboxClient.handleTerminalInput(sessionId, "system status nginx"))
                .contains("Did you mean: systemctl status nginx");
        assertThat(sandboxClient.handleTerminalInput(sessionId, "not-a-command"))
                .contains("bash: not-a-command: command not found")
                .contains("help")
                .contains("hint");
    }
}
