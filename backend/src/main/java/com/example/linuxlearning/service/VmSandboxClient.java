package com.example.linuxlearning.service;

import com.example.linuxlearning.domain.Lab;

public interface VmSandboxClient {

    SandboxAllocation allocate(Lab lab);

    SandboxAllocation reset(String vmId, Lab lab);

    void release(String vmId);

    String terminalIntro(Long sessionId);

    String handleTerminalInput(Long sessionId, String input);
}
