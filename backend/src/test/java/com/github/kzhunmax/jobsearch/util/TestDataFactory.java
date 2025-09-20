package com.github.kzhunmax.jobsearch.util;

import com.github.kzhunmax.jobsearch.dto.request.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserLoginDTO;
import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.model.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class TestDataFactory {

    public static final String TEST_USERNAME = "user";
    public static final Long TEST_ID = 1L;
    public static final Long NON_EXISTENT_ID = 99L;
    public static final String NON_EXISTENT_USERNAME = "unknown";
    public static final String USER_NOT_FOUND_MESSAGE = "User not found";
    public static final String JOB_NOT_FOUND_MESSAGE = "Job with id %d not found";

    public static User createUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("Password123");
        return user;
    }

    public static Job createJob(Long id, User user, boolean isActive) {
        return Job.builder()
                .id(id)
                .title("Java Dev")
                .description("Backend dev")
                .company("BigTech")
                .location("Remote")
                .salary(5000.0)
                .active(isActive)
                .postedBy(user)
                .build();
    }

    public static User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPassword("Password123");
        return user;
    }

    public static Job createJob(User user, boolean isActive) {
        return Job.builder()
                .title("Java Dev")
                .description("Backend dev")
                .company("BigTech")
                .location("Remote")
                .salary(5000.0)
                .active(isActive)
                .postedBy(user)
                .build();
    }

    public static JobApplication createJobApplication(User user, Job job) {
        JobApplication app = JobApplication.builder()
                .job(job)
                .candidate(user)
                .status(ApplicationStatus.APPLIED)
                .appliedAt(Instant.MIN)
                .coverLetter("Cover Letter")
                .build();
        app.setId(TEST_ID);
        return app;
    }

    public static JobApplicationRequestDTO createJobApplicationRequestDTO(Long jobId, String coverLetter) {
        return new JobApplicationRequestDTO(jobId, coverLetter);
    }

    public static JobApplicationResponseDTO createJobApplicationResponseDTO(JobApplication app) {
        return new JobApplicationResponseDTO(
                app.getId(),
                app.getJob().getId(),
                app.getJob().getTitle(),
                app.getJob().getCompany(),
                app.getCandidate().getUsername(),
                app.getStatus().name(),
                app.getAppliedAt().toString(),
                app.getCoverLetter()
        );
    }


    public static JobRequestDTO createJobRequest() {
        return new JobRequestDTO("Java Dev", "Backend dev", "BigTech", "Remote", 5000.0);
    }

    public static JobResponseDTO createJobResponse(Long id) {
        return new JobResponseDTO(id, "Java Dev", "Backend dev", "BigTech", "Remote", 5000.0, true, "user");
    }

    public static JobRequestDTO createInvalidJobRequest() {
        return new JobRequestDTO("", "Backend dev", "BigTech", "", -100.0);
    }

    public static JobRequestDTO updateJobRequest() {
        return new JobRequestDTO("Updated title", "Updated description",
                "Updated company", "Updated location", 5000.0);
    }

    public static JobResponseDTO updateJobResponse(Long id) {
        return new JobResponseDTO(id, "Updated title", "Updated description",
                "Updated company", "Updated location", 5000.0, true, "recruiter");
    }

    public static UserRegistrationDTO createUserRegistrationDTO() {
        return new UserRegistrationDTO(TEST_USERNAME, TEST_USERNAME + "@example.com", "Password123", "Password123", Set.of(Role.ROLE_USER));
    }

    public static UserLoginDTO createUserLoginDTO() {
        return new UserLoginDTO(TEST_USERNAME, "Password123");
    }

    public static UserResponseDTO createUserResponseDTO(Long id) {
        return new UserResponseDTO(TEST_USERNAME, TEST_USERNAME + "@example.com", Set.of(Role.ROLE_USER));
    }

    public static UserDetails createUserDetails(String username) {
        return new org.springframework.security.core.userdetails.User(
                username,
                "Password123",
                true,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
