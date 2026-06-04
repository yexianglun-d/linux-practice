package com.example.linuxlearning.controller;

import com.example.linuxlearning.common.ApiResponse;
import com.example.linuxlearning.dto.CheckTaskRequest;
import com.example.linuxlearning.dto.CheckTaskResponse;
import com.example.linuxlearning.dto.LabSessionResponse;
import com.example.linuxlearning.dto.StartDefaultLabSessionRequest;
import com.example.linuxlearning.service.LabSessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab-sessions")
public class LabSessionController {

    private final LabSessionService labSessionService;

    public LabSessionController(LabSessionService labSessionService) {
        this.labSessionService = labSessionService;
    }

    @PostMapping("/default")
    public ApiResponse<LabSessionResponse> startDefault(@RequestBody(required = false) StartDefaultLabSessionRequest request) {
        return ApiResponse.ok(labSessionService.startDefault(request));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<LabSessionResponse> get(@PathVariable Long sessionId) {
        return ApiResponse.ok(labSessionService.get(sessionId));
    }

    @PostMapping("/{sessionId}/check")
    public ApiResponse<CheckTaskResponse> check(@PathVariable Long sessionId,
                                                @Valid @RequestBody CheckTaskRequest request) {
        return ApiResponse.ok(labSessionService.check(sessionId, request));
    }

    @PostMapping("/{sessionId}/reset")
    public ApiResponse<LabSessionResponse> reset(@PathVariable Long sessionId) {
        return ApiResponse.ok(labSessionService.reset(sessionId));
    }
}
