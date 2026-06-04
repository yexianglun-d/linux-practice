package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.Lab;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryVmSandboxClient implements VmSandboxClient {

    private static final String HOME_DIRECTORY = "/home/student";
    private static final String ROOT_DIRECTORY = "/";
    private static final String TMP_DIRECTORY = "/tmp";
    private static final String LAB_DIRECTORY = "/tmp/linux-foundation";
    private static final String NGINX_STATUS_COMMAND = "systemctl status nginx";

    private final Map<Long, String> currentDirectoryBySession = new ConcurrentHashMap<>();
    private final Map<Long, Set<String>> createdFilesBySession = new ConcurrentHashMap<>();

    @Override
    public SandboxAllocation allocate(Lab lab) {
        String vmId = "vm-" + UUID.randomUUID().toString().substring(0, 8);
        return new SandboxAllocation(vmId, "vm-pool.local", lab.getImageRef() + "@snapshot-" + Instant.now().getEpochSecond());
    }

    @Override
    public SandboxAllocation reset(String vmId, Lab lab) {
        return new SandboxAllocation(vmId, "vm-pool.local", lab.getImageRef() + "@reset-" + Instant.now().getEpochSecond());
    }

    @Override
    public void release(String vmId) {
        // 真实 VM 适配器应在这里做快照回滚、磁盘清理和资源回池。
    }

    @Override
    public String terminalIntro(Long sessionId) {
        currentDirectoryBySession.put(sessionId, HOME_DIRECTORY);
        createdFilesBySession.remove(sessionId);
        return """
                Connected to Linux VM sandbox.
                用户: student   主机: linux-lab-%s   当前目录: /home/student
                直接输入 Linux 命令即可练习；写错时会按 shell 风格报错并给出建议。
                可先试: pwd, whoami, ll, cd /, touch /tmp/linux-foundation/permission-ok
                """.formatted(sessionId);
    }

    @Override
    public String handleTerminalInput(Long sessionId, String input) {
        String command = input == null ? "" : input.trim();
        String lowerCommand = command.toLowerCase(Locale.ROOT);
        if (command.isBlank()) {
            return "";
        }
        if ("pwd".equals(lowerCommand)) {
            return currentDirectory(sessionId);
        }
        if ("whoami".equals(lowerCommand)) {
            return "student";
        }
        if ("id".equals(lowerCommand)) {
            return "uid=1000(student) gid=1000(student) groups=1000(student)";
        }
        if ("hostname".equals(lowerCommand)) {
            return "linux-lab-" + sessionId;
        }
        if (isListCommand(lowerCommand)) {
            return listDirectory(sessionId, command);
        }
        if (lowerCommand.equals("cd") || lowerCommand.startsWith("cd ")) {
            return changeDirectory(sessionId, command);
        }
        if (lowerCommand.startsWith("mkdir ")) {
            return makeDirectory(command);
        }
        if (lowerCommand.startsWith("touch ")) {
            return touchFile(sessionId, command);
        }
        if (NGINX_STATUS_COMMAND.equals(lowerCommand)) {
            return "nginx.service - A high performance web server\n   Loaded: loaded (/lib/systemd/system/nginx.service; enabled)\n   Active: active (running)\n Main PID: 1280 (nginx)";
        }
        if ("system status nginx".equals(lowerCommand)) {
            return """
                    bash: system: command not found
                    Did you mean: systemctl status nginx
                    说明：Linux 服务通常用 systemctl 管理，status 用来查看服务状态。
                    """;
        }
        if (lowerCommand.startsWith("systemctl ")) {
            return "systemctl: command understood, but this sandbox only has nginx.service preloaded.\nTry: systemctl status nginx";
        }
        if (lowerCommand.startsWith("curl ")) {
            return "HTTP/1.1 200 OK\nnetwork policy: whitelist matched";
        }
        if (lowerCommand.startsWith("kubectl ")) {
            return "NAME            READY   STATUS    RESTARTS   AGE\nlinux-demo      1/1     Running   0          2m";
        }
        if (lowerCommand.startsWith("echo ")) {
            return command.substring("echo ".length());
        }
        if ("root".equals(lowerCommand)) {
            return """
                    bash: root: command not found
                    提示：想确认当前用户请用 whoami；想切换 root 通常用 sudo -i。
                    """;
        }
        if ("hinit".equals(lowerCommand)) {
            return """
                    bash: hinit: command not found
                    Did you mean: hint
                    说明：hint 是学习平台指令，用来查看当前任务提示。
                    """;
        }
        if ("cat /etc/os-release".equals(lowerCommand)) {
            return "PRETTY_NAME=\"Ubuntu 22.04.5 LTS\"\nNAME=\"Ubuntu\"\nVERSION_ID=\"22.04\"";
        }
        return commandNotFound(command);
    }

    private boolean isListCommand(String lowerCommand) {
        return lowerCommand.equals("ls")
                || lowerCommand.equals("ll")
                || lowerCommand.startsWith("ls ")
                || lowerCommand.startsWith("ll ");
    }

    private String listDirectory(Long sessionId, String command) {
        String lowerCommand = command.toLowerCase(Locale.ROOT);
        String target = lowerCommand.startsWith("ll")
                ? argumentAfter(command, "ll")
                : argumentAfter(command, "ls");
        String directory = normalizeDirectory(currentDirectory(sessionId), resolveListTarget(target, currentDirectory(sessionId)));
        if (ROOT_DIRECTORY.equals(directory)) {
            return "total 28\ndrwxr-xr-x  18 root root 4096 .\ndrwxr-xr-x  18 root root 4096 ..\ndrwxr-xr-x   3 root root 4096 home\ndrwxrwxrwt  10 root root 4096 tmp\ndrwxr-xr-x  13 root root 4096 var";
        }
        if (HOME_DIRECTORY.equals(directory)) {
            return "total 12\ndrwxr-xr-x 2 student student 128 .\ndrwxr-xr-x 3 root    root    96 ..\n-rw-r--r-- 1 student student  42 README.md\ndrwxr-xr-x 2 student student 128 lab-files";
        }
        if (LAB_DIRECTORY.equals(directory)) {
            boolean hasPermissionFile = createdFiles(sessionId).contains(LAB_DIRECTORY + "/permission-ok");
            return hasPermissionFile
                    ? "total 4\n-rw-r--r-- 1 student student 0 permission-ok"
                    : "total 0";
        }
        if (TMP_DIRECTORY.equals(directory)) {
            return "total 4\ndrwxr-xr-x 2 student student 64 linux-foundation";
        }
        return "ls: cannot access '" + directory + "': No such file or directory";
    }

    private String changeDirectory(Long sessionId, String command) {
        String target = argumentAfter(command, "cd");
        String nextDirectory = target.isBlank() ? HOME_DIRECTORY : normalizeDirectory(currentDirectory(sessionId), target);
        if (isKnownDirectory(nextDirectory)) {
            currentDirectoryBySession.put(sessionId, nextDirectory);
            return "已切换到 " + nextDirectory + "。输入 pwd 可以确认当前位置。";
        }
        return "bash: cd: " + target + ": No such file or directory";
    }

    private String makeDirectory(String command) {
        String target = argumentAfter(command, "mkdir");
        if (target.isBlank() || "-p".equals(target)) {
            return "mkdir: missing operand";
        }
        return "created directory " + target.replace("-p ", "").trim();
    }

    private String touchFile(Long sessionId, String command) {
        String target = argumentAfter(command, "touch");
        if (target.isBlank()) {
            return "touch: missing file operand\nTry: touch /tmp/linux-foundation/permission-ok";
        }
        String filePath = normalizePath(currentDirectory(sessionId), target);
        createdFiles(sessionId).add(filePath);
        return "created " + filePath;
    }

    private String commandNotFound(String command) {
        String executable = command.split("\\s+")[0];
        return "bash: " + executable + ": command not found\n"
                + "提示：输入 help 看学习指令，输入 hint 看当前任务提示；普通 Linux 命令可以先试 pwd、ls、whoami。";
    }

    private String argumentAfter(String command, String executable) {
        return command.length() <= executable.length() ? "" : command.substring(executable.length()).trim();
    }

    private String currentDirectory(Long sessionId) {
        return currentDirectoryBySession.getOrDefault(sessionId, HOME_DIRECTORY);
    }

    private Set<String> createdFiles(Long sessionId) {
        return createdFilesBySession.computeIfAbsent(sessionId, key -> ConcurrentHashMap.newKeySet());
    }

    private boolean isKnownDirectory(String directory) {
        return ROOT_DIRECTORY.equals(directory)
                || HOME_DIRECTORY.equals(directory)
                || TMP_DIRECTORY.equals(directory)
                || LAB_DIRECTORY.equals(directory)
                || "/home".equals(directory)
                || "/var".equals(directory)
                || "/var/log".equals(directory);
    }

    private String resolveListTarget(String argument, String currentDirectory) {
        if (argument == null || argument.isBlank()) {
            return currentDirectory;
        }
        String target = currentDirectory;
        for (String segment : argument.split("\\s+")) {
            if (!segment.isBlank() && !segment.startsWith("-")) {
                target = segment;
            }
        }
        return target;
    }

    private String normalizeDirectory(String currentDirectory, String target) {
        if (target == null || target.isBlank() || "~".equals(target)) {
            return HOME_DIRECTORY;
        }
        if (".".equals(target)) {
            return currentDirectory;
        }
        if ("..".equals(target)) {
            return parentDirectory(currentDirectory);
        }
        if ("//".equals(target)) {
            return ROOT_DIRECTORY;
        }
        if (target.endsWith("/") && target.length() > 1) {
            return target.substring(0, target.length() - 1);
        }
        return target.startsWith("/") ? target : normalizeDirectory(currentDirectory + "/" + target);
    }

    private String normalizeDirectory(String directory) {
        if (directory.endsWith("/") && directory.length() > 1) {
            return directory.substring(0, directory.length() - 1);
        }
        return directory;
    }

    private String parentDirectory(String directory) {
        if (directory == null || directory.isBlank() || ROOT_DIRECTORY.equals(directory)) {
            return ROOT_DIRECTORY;
        }
        int splitIndex = directory.lastIndexOf('/');
        if (splitIndex <= 0) {
            return ROOT_DIRECTORY;
        }
        return directory.substring(0, splitIndex);
    }

    private String normalizePath(String currentDirectory, String target) {
        if (target.startsWith("/")) {
            return target;
        }
        if (ROOT_DIRECTORY.equals(currentDirectory)) {
            return ROOT_DIRECTORY + target;
        }
        return currentDirectory + "/" + target;
    }
}
