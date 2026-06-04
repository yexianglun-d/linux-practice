package com.example.linuxlearning.controller;

import com.example.linuxlearning.common.ApiResponse;
import com.example.linuxlearning.dto.CommandAttemptRequest;
import com.example.linuxlearning.dto.CommandAttemptResponse;
import com.example.linuxlearning.dto.CommandExerciseCatalogResponse;
import com.example.linuxlearning.service.CommandExerciseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/command-exercises")
public class CommandExerciseController {

    private final CommandExerciseService commandExerciseService;

    public CommandExerciseController(CommandExerciseService commandExerciseService) {
        this.commandExerciseService = commandExerciseService;
    }

    @GetMapping("/default")
    public ApiResponse<CommandExerciseCatalogResponse> defaultCatalog() {
        return ApiResponse.ok(commandExerciseService.getDefaultCatalog());
    }

    @PostMapping("/{exerciseId}/attempt")
    public ApiResponse<CommandAttemptResponse> attempt(@PathVariable String exerciseId,
                                                       @Valid @RequestBody CommandAttemptRequest request) {
        return ApiResponse.ok(commandExerciseService.attempt(exerciseId, request));
    }
}
