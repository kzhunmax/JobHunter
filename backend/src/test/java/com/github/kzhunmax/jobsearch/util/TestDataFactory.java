package com.github.kzhunmax.jobsearch.util;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;

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
        return new JobRequestDTO("Updated title", "Update description",
                "Updated company", "Updated location", 5000.0);
    }

    public static JobResponseDTO updateJobResponse(Long id) {
        return new JobResponseDTO(id, "Updated title", "Update description",
                "Updated company", "Updated location", 5000.0, true, "recruiter");
    }
}
