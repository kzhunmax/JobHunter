package com.github.kzhunmax.jobsearch.security;

import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobSecurityService {
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional(readOnly = true)
    public boolean isJobOwner(Long jobId, Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }
        Job job = findJobById(jobId);
        return isJobOwner(job, getUsername(authentication));
    }

    @Transactional(readOnly = true)
    public boolean canUpdateApplication(Long applicationId, ApplicationStatus status, Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return false;
        }

        JobApplication application = findApplicationById(applicationId);
        String username = getUsername(authentication);

        if (isAdmin(authentication)) {
            return true;
        }

        if (isApplicationJobOwner(application, username)) {
            return true;
        }

        if (isApplicationCandidate(application, username)) {
            return status == ApplicationStatus.REJECTED;
        }

        return false;
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private JobApplication findApplicationById(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getUsername(Authentication authentication) {
        return authentication.getName();
    }

    private boolean isJobOwner(Job job, String username) {
        return job.getPostedBy().getUsername().equals(username);
    }

    private boolean isApplicationJobOwner(JobApplication application, String username) {
        return application.getJob().getPostedBy().getUsername().equals(username);
    }

    private boolean isApplicationCandidate(JobApplication application, String username) {
        return application.getCandidate().getUsername().equals(username);
    }
}
