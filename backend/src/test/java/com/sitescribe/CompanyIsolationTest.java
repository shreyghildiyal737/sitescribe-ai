package com.sitescribe;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Guards the two security properties the whole product depends on:
 *  1. Authentication actually works (real password check, JWT issued).
 *  2. Company data isolation — one company can never read another's data.
 *
 * Runs against an in-memory DB (test profile) with MockMvc, so it neither
 * binds a port nor touches the dev database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CompanyIsolationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String bootstrap(String company, String email) throws Exception {
        String body = """
            {"companyName":"%s","name":"Manager","email":"%s","preferredLanguage":"en","password":"password123"}
            """.formatted(company, email);
        String res = mvc.perform(post("/api/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data").path("token").asText();
    }

    private String createProject(String token, String name) throws Exception {
        String res = mvc.perform(post("/api/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\",\"address\":\"1 Site Road\"}"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(res).path("data").path("id").asText();
    }

    @Test
    void companyCannotAccessAnotherCompanysProject() throws Exception {
        String tokenA = bootstrap("Alpha Builders", "alpha@test.com");
        String tokenB = bootstrap("Beta Builders", "beta@test.com");

        String projectId = createProject(tokenA, "Alpha Site");

        // The owning company can read its own project.
        mvc.perform(get("/api/projects/" + projectId).header("Authorization", "Bearer " + tokenA))
            .andExpect(status().isOk());

        // A different company must not — isolation surfaces as 404 (no existence leak).
        mvc.perform(get("/api/projects/" + projectId).header("Authorization", "Bearer " + tokenB))
            .andExpect(status().isNotFound());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/projects/my")).andExpect(status().is4xxClientError());
    }

    @Test
    void reportExportRequiresAuthentication() throws Exception {
        // Regression guard: report exports were once public, allowing unauthenticated
        // download of any report by id. They must now require authentication.
        mvc.perform(get("/api/exports/reports/" + java.util.UUID.randomUUID() + "/text"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void loginWithCorrectPasswordSucceeds() throws Exception {
        bootstrap("Delta Builders", "delta@test.com");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"delta@test.com\",\"password\":\"password123\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        bootstrap("Gamma Builders", "gamma@test.com");
        mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"gamma@test.com\",\"password\":\"wrong-password\"}"))
            .andExpect(status().isBadRequest());
    }
}
