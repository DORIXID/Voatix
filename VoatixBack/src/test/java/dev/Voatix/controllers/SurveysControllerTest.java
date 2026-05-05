package dev.Voatix.controllers;

import dev.Voatix.BaseIntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SurveysControllerTest extends BaseIntegrationTest {

    // -------------------------
    // GET /api/surveys
    // -------------------------
    @Test
    @Order(1)
    void getSurveys_shouldReturn200() throws Exception {

        String json = """
            {
              "projectId": 1,
              "page": 0,
              "limit": 5,
              "filterBy": "",
              "searchedValue": ""
            }
        """;

        mockMvc.perform(get("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/surveys/new
    // -------------------------
    @Test
    @Order(2)
    void createSurvey_shouldReturn200() throws Exception {

        String json = """
            {
              "title": "Новый опрос",
              "description": "Описание опроса",
              "startDate": "2030-01-01T10:00:00",
              "endDate": "2030-01-02T10:00:00",
              "type": "RADIO_BUTTON",
              "projectId": 1,
              "votingPoints": [
                { "title": "Вариант 1" },
                { "title": "Вариант 2" }
              ]
            }
        """;

        mockMvc.perform(post("/api/surveys/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // POST /api/surveys/vote
    // -------------------------
    @Test
    @Order(3)
    void voteSurvey_shouldReturn200() throws Exception {

        String json = """
            {
              "votingPointId": 1
            }
        """;

        mockMvc.perform(post("/api/surveys/vote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }

    // -------------------------
    // DELETE /api/surveys
    // -------------------------
    @Test
    @Order(99)
    void deleteSurvey_shouldReturn200() throws Exception {

        String json = """
            {
              "surveyId": 1
            }
        """;

        mockMvc.perform(delete("/api/surveys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .header("Authorization", jwtToken))
                .andExpect(status().isOk());
    }
}
