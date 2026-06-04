package com.example.linuxlearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CommandExerciseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeDefaultCommandExercises() throws Exception {
        mockMvc.perform(get("/api/command-exercises/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.chapterTitle").value("第 1 章：Linux 基础命令肌肉记忆"))
                .andExpect(jsonPath("$.data.exercises[0].expectedCommand").value("pwd"));
    }

    @Test
    void shouldReturnAttemptFeedback() throws Exception {
        mockMvc.perform(post("/api/command-exercises/nginx-service-status/attempt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"input\":\"system status nginx\",\"elapsedMs\":1000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.correct").value(false))
                .andExpect(jsonPath("$.data.errorType").value("WRONG_COMMAND"))
                .andExpect(jsonPath("$.data.message").value(org.hamcrest.Matchers.containsString("systemctl status nginx")));
    }
}
