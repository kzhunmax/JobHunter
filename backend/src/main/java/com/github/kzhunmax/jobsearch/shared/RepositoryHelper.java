package com.github.kzhunmax.jobsearch.shared;

import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.exception.UserProfileNotFound;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.UserProfileRepository;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryHelper {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserProfileRepository userProfileRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    public UserProfile findUserProfileByUserId(Long userProfileId) {
        return userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new UserProfileNotFound(userProfileId));
    }

    public JobApplication findApplicationById(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
    }

    public Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }
}
