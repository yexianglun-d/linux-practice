package com.example.linuxlearning.service;

public record SandboxAllocation(
        String vmId,
        String vmHost,
        String snapshotRef
) {
}
