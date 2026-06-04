package com.example.linuxlearning;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LabSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldStartBeginnerDefaultLabSessionThroughApi() throws Exception {
        mockMvc.perform(post("/api/lab-sessions/default"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.labTitle").value("基础命令热身"))
                .andExpect(jsonPath("$.data.tasks[0].instruction").isNotEmpty())
                .andExpect(jsonPath("$.data.tasks[0].hint").isNotEmpty())
                .andExpect(jsonPath("$.data.tasks[0].score").value(10));
    }

    @Test
    void shouldStartOpsDefaultLabSessionThroughApi() throws Exception {
        mockMvc.perform(post("/api/lab-sessions/default")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"learningPath\":\"OPS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.labTitle").value("Nginx 服务状态检查"));
    }

    @Test
    void shouldNotExposeContentManagementApi() throws Exception {
        mockMvc.perform(post("/api/" + "admin/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }
}
