package com.example.linuxlearning.service;

public record CheckResult(
        boolean passed,
        String message
) {
}
