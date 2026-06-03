package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.Lab;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Component
public class InMemoryVmSandboxClient implements VmSandboxClient {

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
        return """
                Connected to Linux VM sandbox.
                user: student   host: linux-lab-%s
                Type commands such as pwd, whoami, ls, touch, systemctl status nginx.
                """.formatted(sessionId);
    }

    @Override
    public String handleTerminalInput(Long sessionId, String input) {
        String command = input == null ? "" : input.trim();
        String lowerCommand = command.toLowerCase(Locale.ROOT);
        if (command.isBlank()) {
            return "$ ";
        }
        if ("pwd".equals(lowerCommand)) {
            return "$ " + command + "\n/home/student";
        }
        if ("whoami".equals(lowerCommand)) {
            return "$ " + command + "\nstudent";
        }
        if ("ls".equals(lowerCommand) || "ls -la".equals(lowerCommand)) {
            return "$ " + command + "\ntotal 12\n-rw-r--r-- 1 student student  42 README.md\ndrwxr-xr-x 2 student student 128 lab-files";
        }
        if (lowerCommand.startsWith("touch ")) {
            return "$ " + command + "\ncreated " + command.substring("touch ".length()).trim();
        }
        if (lowerCommand.contains("systemctl status nginx")) {
            return "$ " + command + "\nnginx.service - A high performance web server\n   Active: active (running)";
        }
        if (lowerCommand.startsWith("curl ")) {
            return "$ " + command + "\nHTTP/1.1 200 OK\nnetwork policy: whitelist matched";
        }
        if (lowerCommand.startsWith("kubectl ")) {
            return "$ " + command + "\nNAME            READY   STATUS    RESTARTS   AGE\nlinux-demo      1/1     Running   0          2m";
        }
        return "$ " + command + "\ncommand accepted in sandbox transcript";
    }
}
