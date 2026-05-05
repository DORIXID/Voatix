package dev.Voatix.controllers;

import dev.Voatix.BaseIntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectsControllerTest extends BaseIntegrationTest {

    // -------------------------
    // GET /api/projects
    // -------------------------
    @Test
    @Order(1)
    void getProjectsOfUser_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/projects/{projectId}
    // -------------------------
    @Test
    @Order(2)
    void getProject_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/projects/1")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/projects/{id}/profile
    // -------------------------
    @Test
    @Order(3)
    void getProjectProfile_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/projects/1/profile")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/projects/avatar
    // -------------------------
    @Test
    @Order(4)
    void updateAvatar_shouldReturn200() throws Exception {

        String json = """
            {
              "projectId": 1,
              "fileId": 10
            }
        """;

        mockMvc.perform(patch("/api/projects/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/projects/moderators
    // -------------------------
    @Test
    @Order(5)
    void addModerator_shouldReturn200() throws Exception {

        String json = """
            {
              "projectId": 1,
              "moderatorId": 5
            }
        """;

        mockMvc.perform(post("/api/projects/moderators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/projects
    // -------------------------
    @Test
    @Order(6)
    void createProject_shouldReturn200() throws Exception {

        String json = """
            {
              "title": "Новый проект",
              "fileId": 12
            }
        """;

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/projects/moderators
    // -------------------------
    @Test
    @Order(7)
    void deleteModerator_shouldReturn200() throws Exception {

        String json = """
            {
              "projectId": 1,
              "moderatorId": 5
            }
        """;

        mockMvc.perform(delete("/api/projects/moderators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
    @Test
    @Order(8)
    void deleteModerator_shouldReturn403() throws Exception {

        String json = """
            {
              "projectId": 1,
              "moderatorId": 2
            }
        """;

        mockMvc.perform(delete("/api/projects/moderators")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isForbidden());
    }


    // -------------------------
    // DELETE /api/projects
    // -------------------------
    @Test
    @Order(20)
    void deleteProject_shouldReturn403() throws Exception {

        String json = """
            {
              "id": 2
            }
        """;

        mockMvc.perform(delete("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isForbidden());
    }
}
