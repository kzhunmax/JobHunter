package com.github.kzhunmax.jobsearch.util;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.shared.enums.*;
import com.github.kzhunmax.jobsearch.user.dto.UserLoginDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class TestDataFactory {

    public static final Long TEST_ID = 1L;
    public static final Long NON_EXISTENT_ID = 99L;
    public static final String TEST_EMAIL = "user@example.com";
    public static final String NON_EXISTENT_EMAIL = "unknown@example.com";
    public static final String JOB_NOT_FOUND_MESSAGE = "Job with id %d not found";
    public static final String VALID_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYyNTA0ODAwMCwiZXhwIjoxNjI1MDQ4MTAwfQ.signature";
    public static final String ACCESS_TOKEN = "access-token";
    public static final String REFRESH_TOKEN = "refresh-token";
    public static final Long JWT_EXPIRATION = 3600000L;
    public static final Long REFRESH_EXPIRATION = 7200000L;

    public static final String TEST_TITLE = "Java Developer";
    public static final String TEST_DESCRIPTION = "Develop Java apps";
    public static final String TEST_LOCATION = "New York";
    public static final Double TEST_SALARY = 5000.0;
    public static final String TEST_COMPANY_NAME = "TestCo";
    public static final String TEST_COVER_LETTER = "Cover Letter";
    public static final LocalDate FIXED_DEADLINE = LocalDate.of(2025, 12, 31);
    public static final Instant FIXED_APPLIED_AT = Instant.parse("2025-01-01T00:00:00Z");

    public static User createUser(Long id, String email) {

        return User.builder()
                .id(id)
                .email(email)
                .password("Password123")
                .provider(AuthProvider.LOCAL)
                .roles(new HashSet<>(Set.of(Role.ROLE_CANDIDATE)))
                .build();
    }

    public static User createUser(String email) {
        return User.builder()
                .email(email)
                .password("Password123")
                .provider(AuthProvider.LOCAL)
                .roles(new HashSet<>(Set.of(Role.ROLE_CANDIDATE)))
                .build();
    }

    public static UserProfile createUserProfile(User user) {
        return UserProfile.builder()
                .profileType(ProfileType.RECRUITER)
                .user(user)
                .fullName("Test User")
                .country(Country.USA)
                .activityStatus(ActivityStatus.ACTIVE)
                .build();
    }

    public static Company createCompany(Long id, String name) {
        return Company.builder()
                .id(id)
                .name(name)
                .location(TEST_LOCATION)
                .build();
    }

    public static Company createCompany(String name) {
        return Company.builder()
                .name(name)
                .location(TEST_LOCATION)
                .build();
    }

    public static Job createJob(Long id, User user, Company company, boolean isActive) {
        return Job.builder()
                .id(id)
                .title(TEST_TITLE)
                .description(TEST_DESCRIPTION)
                .company(company)
                .location(TEST_LOCATION)
                .salary(TEST_SALARY)
                .applicationDeadline(FIXED_DEADLINE)
                .active(isActive)
                .postedBy(user)
                .build();
    }

    public static Job createJob(User user, Company company, boolean isActive) {
        return Job.builder()
                .title(TEST_TITLE)
                .description(TEST_DESCRIPTION)
                .company(company)
                .location(TEST_LOCATION)
                .salary(TEST_SALARY)
                .applicationDeadline(FIXED_DEADLINE)
                .active(isActive)
                .postedBy(user)
                .build();
    }

    public static Job createInvalidJob(User user, Company company) {
        return Job.builder()
                .title(TEST_TITLE)
                .description(TEST_DESCRIPTION)
                .company(company)
                .location(TEST_LOCATION)
                .salary(-100.0)
                .applicationDeadline(LocalDate.of(2025, 1, 1))
                .active(true)
                .postedBy(user)
                .build();
    }

    public static Resume createResume(Long id, UserProfile profile) {
        return Resume.builder()
                .id(id)
                .userProfile(profile)
                .title("my_resume.pdf")
                .fileUrl("http://fake.url/my_resume.pdf")
                .build();
    }

    public static Resume createResume(UserProfile profile) {
        return Resume.builder()
                .userProfile(profile)
                .title("my_resume.pdf")
                .fileUrl("http://fake.url/my_resume.pdf")
                .build();
    }

    public static JobApplication createJobApplication(User user, Job job, Resume resume) {
        return JobApplication.builder()
                .job(job)
                .candidate(user)
                .resume(resume)
                .status(ApplicationStatus.APPLIED)
                .appliedAt(FIXED_APPLIED_AT)
                .coverLetter(TEST_COVER_LETTER)
                .build();
    }

    public static JobApplication createJobApplication(Long id, User user, Job job, Resume resume) {
        return JobApplication.builder()
                .id(id)
                .job(job)
                .candidate(user)
                .resume(resume)
                .status(ApplicationStatus.APPLIED)
                .appliedAt(FIXED_APPLIED_AT)
                .coverLetter(TEST_COVER_LETTER)
                .build();
    }

    public static JobApplication createRejectedJobApplication(User user, Job job, Resume resume) {
        return JobApplication.builder()
                .job(job)
                .candidate(user)
                .resume(resume)
                .status(ApplicationStatus.REJECTED)
                .appliedAt(FIXED_APPLIED_AT)
                .coverLetter(TEST_COVER_LETTER)
                .build();
    }

    public static JobApplicationRequestDTO createJobApplicationRequestDTO(Long resumeId, String coverLetter) {
        return new JobApplicationRequestDTO(resumeId, coverLetter);
    }

    public static JobApplicationResponseDTO createJobApplicationResponseDTO(JobApplication app) {
        return new JobApplicationResponseDTO(
                app.getId(),
                app.getJob() != null ? app.getJob().getId() : null,
                app.getJob() != null ? app.getJob().getTitle() : null,
                app.getJob() != null && app.getJob().getCompany() != null ? app.getJob().getCompany().getName() : null,
                app.getCandidate() != null ? app.getCandidate().getEmail() : null,
                app.getCandidate() != null && app.getCandidate().getProfile() != null ? app.getCandidate().getProfile().getId() : null,
                app.getStatus() != null ? app.getStatus().name() : null,
                app.getAppliedAt() != null ? app.getAppliedAt().toString() : null,
                app.getCoverLetter(),
                app.getResume() != null ? app.getResume().getFileUrl() : null
        );
    }

    public static JobRequestDTO createJobRequest(Long companyId) {
        return new JobRequestDTO(TEST_TITLE, TEST_DESCRIPTION, companyId, TEST_LOCATION, TEST_SALARY, FIXED_DEADLINE);
    }

    public static JobResponseDTO createJobResponse(Long id, String companyName, String email) {
        return new JobResponseDTO(id, TEST_TITLE, TEST_DESCRIPTION, companyName, TEST_LOCATION, TEST_SALARY, FIXED_DEADLINE, true, email);
    }

    public static JobRequestDTO createInvalidJobRequest() {
        return new JobRequestDTO("", TEST_DESCRIPTION, null, "", -100.0, LocalDate.of(2025, 1, 1));
    }

    public static JobRequestDTO updateJobRequest(Long companyId) {
        return new JobRequestDTO("Updated title", "Updated description",
                companyId, "Updated location", TEST_SALARY, FIXED_DEADLINE);
    }

    public static JobResponseDTO updateJobResponse(Long id, String companyName, String email) {
        return new JobResponseDTO(id, "Updated title", "Updated description",
                companyName, "Updated location", TEST_SALARY, FIXED_DEADLINE, true, email);
    }

    public static UserRegistrationDTO createUserRegistrationDTO() {
        return new UserRegistrationDTO(TEST_EMAIL, "Password123", "Password123", Set.of(Role.ROLE_CANDIDATE));
    }

    public static UserLoginDTO createUserLoginDTO() {
        return new UserLoginDTO(TEST_EMAIL, "Password123");
    }

    public static UserResponseDTO createUserResponseDTO() {
        return new UserResponseDTO(TEST_EMAIL, Set.of(Role.ROLE_CANDIDATE));
    }

    public static UserDetails createUserDetails(String email) {
        User user = createUser(email);
        user.setEmailVerified(true);
        return new UserDetailsImpl(user);
    }
}
