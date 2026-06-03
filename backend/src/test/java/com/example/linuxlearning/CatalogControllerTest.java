package com.example.linuxlearning;

import com.example.linuxlearning.dto.CatalogResponse;
import com.example.linuxlearning.service.CatalogService;
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
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CatalogService catalogService;

    @Test
    void shouldReturnCatalog() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.learningPaths[0].title").value("零基础入门"))
                .andExpect(jsonPath("$.data.courses[0].title").value("Linux 手敲实战路线"));
    }

    @Test
    void shouldStartLabSessionThroughApi() throws Exception {
        Long labId = firstLabId();
        mockMvc.perform(post("/api/lab-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labId\":" + labId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("RUNNING"))
                .andExpect(jsonPath("$.data.vmHost").value("vm-pool.local"));
    }

    private Long firstLabId() {
        CatalogResponse.LabView lab = catalogService.catalog()
                .courses()
                .getFirst()
                .modules()
                .getFirst()
                .lessons()
                .getFirst()
                .lab();
        return lab.id();
    }
}
