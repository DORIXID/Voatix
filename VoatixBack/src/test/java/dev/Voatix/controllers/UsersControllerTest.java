package dev.Voatix.controllers;

import dev.Voatix.BaseIntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsersControllerTest extends BaseIntegrationTest {

    // -------------------------
    // POST /api/users/new
    // -------------------------
    @Test
    @Order(1)
    void createUser_shouldReturn200() throws Exception {

        String json = """
            {
              "nickname": "newUser1234434",
              "password": "strongPass123",
              "eMail": "newuser@example.com"
            }
        """;

        mockMvc.perform(post("/api/users/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/users/edit
    // -------------------------
    @Test
    @Order(2)
    void editUser_shouldReturn200() throws Exception {

        String json = """
            {
              "nickname": "updatedUser",
              "password": "updatedPass123",
              "eMail": "updated@example.com"
            }
        """;

        mockMvc.perform(patch("/api/users/edit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // GET /api/users/profile
    // -------------------------
    @Test
    @Order(3)
    void getMyProfile_shouldReturn200() throws Exception {

        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // PATCH /api/users/avatar
    // -------------------------
    @Test
    @Order(4)
    void updateAvatar_shouldReturn200() throws Exception {

        String json = """
            {
              "fileId": 10
            }
        """;

        mockMvc.perform(patch("/api/users/avatar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
}
