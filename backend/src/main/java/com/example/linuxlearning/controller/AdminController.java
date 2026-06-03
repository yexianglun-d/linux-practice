package com.example.linuxlearning.controller;

import com.example.linuxlearning.common.ApiResponse;
import com.example.linuxlearning.dto.CreateCourseRequest;
import com.example.linuxlearning.dto.CreateLabRequest;
import com.example.linuxlearning.dto.CreateTaskRequest;
import com.example.linuxlearning.dto.LearningProgressResponse;
import com.example.linuxlearning.service.AdminContentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminContentService adminContentService;

    public AdminController(AdminContentService adminContentService) {
        this.adminContentService = adminContentService;
    }

    @PostMapping("/courses")
    public ApiResponse<Map<String, Long>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        return ApiResponse.ok(Map.of("id", adminContentService.createCourse(request)));
    }

    @PostMapping("/labs")
    public ApiResponse<Map<String, Long>> createLab(@Valid @RequestBody CreateLabRequest request) {
        return ApiResponse.ok(Map.of("id", adminContentService.createLab(request)));
    }

    @PostMapping("/tasks")
    public ApiResponse<Map<String, Long>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ApiResponse.ok(Map.of("id", adminContentService.createTask(request)));
    }

    @GetMapping("/learning-progress")
    public ApiResponse<LearningProgressResponse> learningProgress() {
        return ApiResponse.ok(adminContentService.learningProgress());
    }
}
