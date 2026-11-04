package com.sitescribe;

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

/** Guards the fixes for account enumeration, weak sign-up input and a public API schema. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthHardeningTest {

    @Autowired MockMvc mvc;

    private void signUp(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyName":"Epsilon","name":"Manager","email":"%s","preferredLanguage":"en","password":"%s"}
                    """.formatted(email, password)))
            .andExpect(status().isOk());
    }

    private String loginMessage(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();
    }

    @Test
    void unknownEmailAndWrongPasswordAreIndistinguishable() throws Exception {
        signUp("epsilon@test.com", "password123");

        String wrongPassword = loginMessage("epsilon@test.com", "not-the-password");
        String unknownEmail = loginMessage("nobody@test.com", "not-the-password");

        org.assertj.core.api.Assertions.assertThat(unknownEmail).isEqualTo(wrongPassword);
    }

    @Test
    void signUpRejectsShortPasswords() throws Exception {
        mvc.perform(post("/api/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyName":"Zeta","name":"Manager","email":"zeta@test.com","preferredLanguage":"en","password":"short"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("password: must be 8 to 72 characters"));
    }

    @Test
    void signUpRequiresAnEmail() throws Exception {
        mvc.perform(post("/api/auth/bootstrap")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyName":"Eta","name":"Manager","preferredLanguage":"en","password":"password123"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void apiSchemaIsNotPublishedByDefault() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().is4xxClientError());
    }
}
