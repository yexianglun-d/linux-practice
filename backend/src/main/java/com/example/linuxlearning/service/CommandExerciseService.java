package com.example.linuxlearning.service;

import com.example.linuxlearning.common.NotFoundException;
import com.example.linuxlearning.dto.CommandAttemptRequest;
import com.example.linuxlearning.dto.CommandAttemptResponse;
import com.example.linuxlearning.dto.CommandExerciseCatalogResponse;
import com.example.linuxlearning.dto.CommandExerciseView;
import com.example.linuxlearning.dto.CommandProgressView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CommandExerciseService {

    private static final String CHAPTER_TITLE = "第 1 章：Linux 基础命令肌肉记忆";
    private static final String ERROR_NONE = "NONE";
    private static final String ERROR_EMPTY = "EMPTY";
    private static final String ERROR_TYPO = "TYPO";
    private static final String ERROR_MISSING_ARGUMENT = "MISSING_ARGUMENT";
    private static final String ERROR_WRONG_COMMAND = "WRONG_COMMAND";
    private static final String ERROR_WRONG_OPTION = "WRONG_OPTION";
    private static final String ERROR_NOT_TARGET = "NOT_TARGET";

    private final List<CommandExercise> exercises = List.of(
            new CommandExercise(
                    "pwd-current-directory",
                    "确认当前位置",
                    "你刚登录一台 Linux 机器，先确认自己当前所在目录。",
                    "pwd",
                    List.of("pwd"),
                    "直接输入 pwd。它会打印当前工作目录。",
                    "pwd 是 print working directory 的缩写，用来确认当前路径。",
                    "入门",
                    List.of("directory", "navigation"),
                    1
            ),
            new CommandExercise(
                    "list-hidden-permissions",
                    "查看隐藏文件和权限",
                    "你需要查看当前目录下的隐藏文件、文件权限和所有者信息。",
                    "ls -la",
                    List.of("ls -la", "ls -al"),
                    "ls 负责列目录，-l 显示长格式，-a 显示隐藏文件。",
                    "ls -la 是排查配置文件和权限问题最常用的观察命令之一。",
                    "入门",
                    List.of("files", "permissions"),
                    2
            ),
            new CommandExercise(
                    "change-log-directory",
                    "进入日志目录",
                    "线上服务异常时，先进入系统日志目录准备查看日志。",
                    "cd /var/log",
                    List.of("cd /var/log", "cd /var/log/"),
                    "cd 后面要带目标目录，这里是 /var/log。",
                    "cd 用来切换工作目录；/var/log 通常保存系统和服务日志。",
                    "入门",
                    List.of("navigation", "logs"),
                    3
            ),
            new CommandExercise(
                    "create-file",
                    "创建练习文件",
                    "你要创建一个空文件，作为后续权限练习的对象。",
                    "touch file",
                    List.of("touch file", "touch ./file"),
                    "touch 后面必须跟文件名。",
                    "touch 可以创建空文件，也可以更新已有文件的修改时间。",
                    "入门",
                    List.of("files"),
                    4
            ),
            new CommandExercise(
                    "chmod-readable",
                    "设置常见文件权限",
                    "把 file 设置成所有者可读写、其他人只读的常见配置文件权限。",
                    "chmod 644 file",
                    List.of("chmod 644 file"),
                    "chmod 后面先写权限数字，再写文件名。",
                    "644 表示所有者 rw-，组 r--，其他人 r--。",
                    "进阶",
                    List.of("permissions"),
                    5
            ),
            new CommandExercise(
                    "nginx-service-status",
                    "检查 Nginx 服务状态",
                    "你要确认 Nginx 服务是否正在运行。",
                    "systemctl status nginx",
                    List.of("systemctl status nginx", "systemctl status nginx.service"),
                    "管理 systemd 服务用 systemctl，不是 system。",
                    "systemctl status nginx 会显示服务是否 active running。",
                    "运维",
                    List.of("service", "nginx"),
                    6
            ),
            new CommandExercise(
                    "grep-error-log",
                    "过滤错误日志",
                    "你要从 app.log 中筛出包含 error 的日志行。",
                    "grep \"error\" app.log",
                    List.of("grep \"error\" app.log", "grep 'error' app.log", "grep error app.log"),
                    "grep 后面先写要搜索的关键字，再写文件名。",
                    "grep 是日志排查的高频命令，用来从文本中筛选匹配行。",
                    "运维",
                    List.of("logs", "troubleshooting"),
                    7
            )
    ).stream().sorted(Comparator.comparingInt(CommandExercise::sortOrder)).toList();

    private final CommandProgressState progressState = new CommandProgressState();

    public CommandExerciseCatalogResponse getDefaultCatalog() {
        return new CommandExerciseCatalogResponse(
                CHAPTER_TITLE,
                exercises.stream().map(this::toView).toList(),
                progressView()
        );
    }

    public CommandAttemptResponse attempt(String exerciseId, CommandAttemptRequest request) {
        CommandExercise exercise = findExercise(exerciseId);
        String input = normalize(request.input());
        AttemptFeedback feedback = evaluate(exercise, input);
        progressState.record(exercise.id(), feedback.correct());
        return new CommandAttemptResponse(
                exercise.id(),
                feedback.correct(),
                feedback.errorType(),
                feedback.message(),
                exercise.expectedCommand(),
                exercise.explanation(),
                progressView()
        );
    }

    private AttemptFeedback evaluate(CommandExercise exercise, String input) {
        if (input.isBlank()) {
            return new AttemptFeedback(false, ERROR_EMPTY, "先输入你认为正确的 Linux 命令。");
        }
        if (exercise.acceptedCommands().stream().map(this::normalize).anyMatch(input::equals)) {
            return new AttemptFeedback(true, ERROR_NONE, "正确。继续保持这个输入节奏。");
        }
        String expectedCommand = normalize(exercise.expectedCommand());
        String[] expectedTokens = expectedCommand.split("\\s+");
        String[] inputTokens = input.split("\\s+");
        if (inputTokens.length == 0) {
            return new AttemptFeedback(false, ERROR_EMPTY, "先输入你认为正确的 Linux 命令。");
        }

        if (matchesAnotherExercise(input)) {
            return new AttemptFeedback(false, ERROR_NOT_TARGET, "这个命令本身可能有用，但当前目标要练的是 `" + exercise.expectedCommand() + "`。");
        }
        if ("nginx-service-status".equals(exercise.id()) && "system status nginx".equals(input)) {
            return new AttemptFeedback(false, ERROR_WRONG_COMMAND, "Linux 管理 systemd 服务用 `systemctl`，建议输入 `systemctl status nginx`。");
        }
        if (isNearTypo(inputTokens[0], expectedTokens[0])) {
            return new AttemptFeedback(false, ERROR_TYPO, typoMessage(inputTokens[0], expectedTokens[0], exercise.expectedCommand()));
        }
        if (inputTokens[0].equals(expectedTokens[0]) && inputTokens.length < expectedTokens.length) {
            return new AttemptFeedback(false, ERROR_MISSING_ARGUMENT, "命令缺少参数，建议完整输入 `" + exercise.expectedCommand() + "`。");
        }
        if (inputTokens[0].equals(expectedTokens[0]) && hasUnexpectedOption(inputTokens, expectedTokens)) {
            return new AttemptFeedback(false, ERROR_WRONG_OPTION, "参数不符合当前目标，建议输入 `" + exercise.expectedCommand() + "`。");
        }
        return new AttemptFeedback(false, ERROR_WRONG_COMMAND, "还不符合当前目标。建议输入 `" + exercise.expectedCommand() + "`，理解后再继续。");
    }

    private boolean matchesAnotherExercise(String input) {
        return exercises.stream()
                .flatMap(exercise -> exercise.acceptedCommands().stream())
                .map(this::normalize)
                .anyMatch(input::equals);
    }

    private boolean isNearTypo(String inputCommand, String expectedCommand) {
        return !inputCommand.equals(expectedCommand) && levenshteinDistance(inputCommand, expectedCommand) <= 2;
    }

    private String typoMessage(String actual, String expected, String expectedFullCommand) {
        int index = firstDifferentIndex(actual, expected);
        if (index < 0) {
            return "命令拼写接近，但还不完全正确。建议输入 `" + expectedFullCommand + "`。";
        }
        String expectedChar = index < expected.length() ? "`" + expected.charAt(index) + "`" : "结束";
        String actualChar = index < actual.length() ? "`" + actual.charAt(index) + "`" : "缺少字符";
        return "命令拼写有误：第 " + (index + 1) + " 个字符应为 " + expectedChar + "，你输入的是 " + actualChar
                + "。建议输入 `" + expectedFullCommand + "`。";
    }

    private boolean hasUnexpectedOption(String[] inputTokens, String[] expectedTokens) {
        Set<String> expectedTokenSet = Set.of(expectedTokens);
        for (String token : inputTokens) {
            if (token.startsWith("-") && !expectedTokenSet.contains(token)) {
                return true;
            }
        }
        return false;
    }

    private int firstDifferentIndex(String actual, String expected) {
        int minLength = Math.min(actual.length(), expected.length());
        for (int i = 0; i < minLength; i++) {
            if (actual.charAt(i) != expected.charAt(i)) {
                return i;
            }
        }
        return actual.length() == expected.length() ? -1 : minLength;
    }

    private int levenshteinDistance(String left, String right) {
        int[][] distances = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            distances[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            distances[0][j] = j;
        }
        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                distances[i][j] = Math.min(
                        Math.min(distances[i - 1][j] + 1, distances[i][j - 1] + 1),
                        distances[i - 1][j - 1] + cost
                );
            }
        }
        return distances[left.length()][right.length()];
    }

    private CommandExercise findExercise(String exerciseId) {
        return exercises.stream()
                .filter(exercise -> exercise.id().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("命令练习不存在"));
    }

    private CommandExerciseView toView(CommandExercise exercise) {
        return new CommandExerciseView(
                exercise.id(),
                exercise.title(),
                exercise.scenario(),
                exercise.expectedCommand(),
                exercise.acceptedCommands(),
                exercise.hint(),
                exercise.explanation(),
                exercise.difficulty(),
                exercise.tags(),
                exercise.sortOrder()
        );
    }

    private CommandProgressView progressView() {
        return progressState.toView(exercises.size());
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim().replaceAll("\\s+", " ");
    }

    private record CommandExercise(
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

    private record AttemptFeedback(boolean correct, String errorType, String message) {
    }

    private static class CommandProgressState {

        private final Set<String> completedExerciseIds = ConcurrentHashMap.newKeySet();
        private final AtomicInteger attempts = new AtomicInteger();
        private final AtomicInteger correctAttempts = new AtomicInteger();
        private final AtomicInteger currentStreak = new AtomicInteger();
        private final AtomicInteger bestStreak = new AtomicInteger();

        synchronized void record(String exerciseId, boolean correct) {
            attempts.incrementAndGet();
            if (correct) {
                completedExerciseIds.add(exerciseId);
                correctAttempts.incrementAndGet();
                int streak = currentStreak.incrementAndGet();
                bestStreak.updateAndGet(best -> Math.max(best, streak));
                return;
            }
            currentStreak.set(0);
        }

        CommandProgressView toView(int totalExercises) {
            int attemptCount = attempts.get();
            int correctCount = correctAttempts.get();
            int accuracyPercent = attemptCount == 0 ? 0 : (int) Math.round(correctCount * 100.0 / attemptCount);
            List<String> completedIds = new ArrayList<>(completedExerciseIds);
            completedIds.sort(String::compareTo);
            return new CommandProgressView(
                    totalExercises,
                    completedExerciseIds.size(),
                    completedIds,
                    attemptCount,
                    correctCount,
                    currentStreak.get(),
                    bestStreak.get(),
                    accuracyPercent,
                    mastery(totalExercises)
            );
        }

        private String mastery(int totalExercises) {
            if (completedExerciseIds.size() >= totalExercises) {
                return "MASTERED";
            }
            if (correctAttempts.get() >= 3) {
                return "BUILDING";
            }
            return "WARMING_UP";
        }
    }
}
