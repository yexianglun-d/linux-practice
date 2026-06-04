package com.example.linuxlearning;

import com.example.linuxlearning.dto.CommandAttemptRequest;
import com.example.linuxlearning.dto.CommandAttemptResponse;
import com.example.linuxlearning.dto.CommandExerciseCatalogResponse;
import com.example.linuxlearning.service.CommandExerciseService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandExerciseServiceTest {

    private final CommandExerciseService commandExerciseService = new CommandExerciseService();

    @Test
    void shouldReturnDefaultCommandExercises() {
        CommandExerciseCatalogResponse catalog = commandExerciseService.getDefaultCatalog();

        assertThat(catalog.chapterTitle()).contains("Linux 基础命令");
        assertThat(catalog.exercises()).hasSizeGreaterThanOrEqualTo(7);
        assertThat(catalog.exercises().getFirst().expectedCommand()).isEqualTo("pwd");
        assertThat(catalog.progress().totalExercises()).isEqualTo(catalog.exercises().size());
    }

    @Test
    void shouldAcceptExpectedCommandAndVariant() {
        CommandAttemptResponse pwdResult = commandExerciseService.attempt(
                "pwd-current-directory",
                new CommandAttemptRequest("pwd", 1200)
        );
        CommandAttemptResponse variantResult = commandExerciseService.attempt(
                "list-hidden-permissions",
                new CommandAttemptRequest("ls -al", 1600)
        );

        assertThat(pwdResult.correct()).isTrue();
        assertThat(pwdResult.errorType()).isEqualTo("NONE");
        assertThat(variantResult.correct()).isTrue();
        assertThat(variantResult.progress().completedExercises()).isEqualTo(2);
    }

    @Test
    void shouldClassifyMissingArgumentAndTypo() {
        CommandAttemptResponse missingArgument = commandExerciseService.attempt(
                "list-hidden-permissions",
                new CommandAttemptRequest("ls", 900)
        );
        CommandAttemptResponse typo = commandExerciseService.attempt(
                "nginx-service-status",
                new CommandAttemptRequest("systemcrl status nginx", 900)
        );

        assertThat(missingArgument.correct()).isFalse();
        assertThat(missingArgument.errorType()).isEqualTo("MISSING_ARGUMENT");
        assertThat(missingArgument.message()).contains("ls -la");
        assertThat(typo.correct()).isFalse();
        assertThat(typo.errorType()).isEqualTo("TYPO");
        assertThat(typo.message()).contains("第");
    }

    @Test
    void shouldExplainWrongServiceCommand() {
        CommandAttemptResponse result = commandExerciseService.attempt(
                "nginx-service-status",
                new CommandAttemptRequest("system status nginx", 1000)
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.errorType()).isEqualTo("WRONG_COMMAND");
        assertThat(result.message()).contains("systemctl status nginx");
    }

    @Test
    void shouldDetectCommandThatDoesNotMatchCurrentTarget() {
        CommandAttemptResponse result = commandExerciseService.attempt(
                "create-file",
                new CommandAttemptRequest("pwd", 800)
        );

        assertThat(result.correct()).isFalse();
        assertThat(result.errorType()).isEqualTo("NOT_TARGET");
        assertThat(result.message()).contains("touch file");
    }
}
