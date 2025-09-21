package com.github.kzhunmax.jobsearch.config;

import com.github.kzhunmax.jobsearch.util.AbstractPostgresTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SecurityConfig Integration Tests")
class SecurityConfigTest extends AbstractPostgresTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("PasswordEncoder should be BCryptPasswordEncoder")
    void passwordEncoder_shouldBeBCrypt() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("Should permit all for auth endpoints")
    void permitAllForAuthEndpoints() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should permit GET requests for jobs")
    void permitGetJobs() throws Exception {
        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should require authentication for applications endpoints")
    void requireAuthForApplications() throws Exception {
        mockMvc.perform(get("/api/applications/my-applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should require authentication for POST jobs")
    void requireAuthForPostJobs_unauthenticated() throws Exception {
        mockMvc.perform(post("/api/jobs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid POST jobs for non-recruiter role")
    void forbidPostJobsForNonRecruiter() throws Exception {
        mockMvc.perform(post("/api/jobs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("Should allow POST jobs for recruiter role")
    void allowPostJobsForRecruiter() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should require authentication for PUT jobs")
    void requireAuthForPutJobs_unauthenticated() throws Exception {
        mockMvc.perform(put("/api/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid PUT jobs for non-recruiter role")
    void forbidPutJobsForNonRecruiter() throws Exception {
        mockMvc.perform(put("/api/jobs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("Should allow PUT jobs for recruiter role")
    void allowPutJobsForRecruiter() throws Exception {
        mockMvc.perform(put("/api/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should require authentication for DELETE jobs")
    void requireAuthForDeleteJobs_unauthenticated() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should forbid DELETE jobs for non-recruiter role")
    void forbidDeleteJobsForNonRecruiter() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    @DisplayName("Should allow DELETE jobs for recruiter role")
    void allowDeleteJobsForRecruiter() throws Exception {
        mockMvc.perform(delete("/api/jobs/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should require authentication for other endpoints")
    void requireAuthForOtherEndpoints() throws Exception {
        mockMvc.perform(get("/some/other/path"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should have CSRF disabled")
    void csrfDisabled() throws Exception {
        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}