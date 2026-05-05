package dev.Voatix.controllers;

import dev.Voatix.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class CommentsControllerTest extends BaseIntegrationTest {

    // -------------------------
    // GET /api/comments
    // -------------------------
    @Test
    void getComments_shouldReturn200() throws Exception {

        String json = """
        {
          "ideaId": 12,
          "page": 0,
          "limit": 12
        }
    """;

        mockMvc.perform(get("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/comments/new
    // -------------------------
    @Test
    void createComment_shouldReturn200() throws Exception {

        String json = """
        {
          "text": "New comment",
          "username": "testUser",
          "ideaId": 12,
          "fileIds": [1, 2]
        }
    """;

        mockMvc.perform(post("/api/comments/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/comments
    // -------------------------
    @Test
    void deleteComment_shouldReturn200() throws Exception {

        String json = """
        {
          "id": 12
        }
    """;

        mockMvc.perform(delete("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/comments
    // -------------------------
    @Test
    void updateComment_shouldReturn403() throws Exception {

        String json = """
        {
          "commentId": 11,
          "text": "Updated text"
        }
    """;

        mockMvc.perform(patch("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isForbidden());
    }

    // -------------------------
    // PUT /api/comments/like
    // -------------------------
    @Test
    void upsertLike_shouldReturn200() throws Exception {

        String json = """
        {
          "commentId": 11,
          "like": 1
        }
    """;

        mockMvc.perform(put("/api/comments/like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
}
