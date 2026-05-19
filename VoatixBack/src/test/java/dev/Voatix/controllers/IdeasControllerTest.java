package dev.Voatix.controllers;

import dev.Voatix.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class IdeasControllerTest extends BaseIntegrationTest {
    // -------------------------
    // GET /api/ideas
    // -------------------------
    @Test
    void getIdeas_shouldReturn200() throws Exception {

        String json = """
            {
              "projectId": 1,
              "page": 0,
              "limit": 12,
              "filterBy": "CREATED",
              "search": "Разра"
            }
        """;

        mockMvc.perform(post("/api/ideas/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    @Test
    void getIdea_shouldReturn200() throws Exception {

        String json = """
            {
              "ideaId": 1
            }
        """;

        mockMvc.perform(get("/api/ideas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PUT /api/ideas/like
    // -------------------------
    @Test
    void upsertLike_shouldReturn200() throws Exception {

        String json = """
            {
              "ideaId": 1,
              "like": 1
            }
        """;

        mockMvc.perform(put("/api/ideas/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/ideas
    // -------------------------
    @Test
    void createIdea_shouldReturn200() throws Exception {

        String json = """
            {
                "title": "Моя очень крутая идея для проекта",
                "description": "Здесь должно быть очень длинное описание, минимум на тридцать символов, чтобы валидация пропустила запрос.",
                "projectId": 1,
                "fileIds": [1, 2, 5]
            }
        """;

        mockMvc.perform(post("/api/ideas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/ideas
    // -------------------------
    @Test
    void updateIdea_shouldReturn200() throws Exception {

        String json = """
            {
                "ideaId": 1,
                "title": "Новый заголовок идеи (длиннее 10 символов)",
                "description": "Новое описание, которое точно больше тридцати символов для прохождения валидации."
            }
        """;

        mockMvc.perform(patch("/api/ideas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/ideas/status
    // -------------------------
    @Test
    void updateIdeaStatus_shouldReturn200() throws Exception {

        String json = """
            {
                "ideaId": 1,
                "status": "DONE"
            }
        """;

        mockMvc.perform(patch("/api/ideas/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/ideas
    // -------------------------
    @Test
    void deleteIdea_shouldReturn200() throws Exception {

        String json = """
            {
              "ideaId": 14
            }
        """;

        mockMvc.perform(delete("/api/ideas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
}
