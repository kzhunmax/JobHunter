package com.github.kzhunmax.jobsearch.security;

import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class JobSecurityService {
    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Transactional(readOnly = true)
    public boolean isJobOwner(Long jobId, Authentication authentication) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Checking job ownership - jobId={}", requestId, jobId);
        if (isAuthenticated(authentication)) {
            log.debug("Request [{}]: Job ownership check failed - not authenticated", requestId);
            return false;
        }
        Job job = findJobById(jobId);
        boolean isOwner = isJobOwner(job, getEmail(authentication));
        log.debug("Request [{}]: Job ownership check completed - jobId={}, isOwner={}", requestId, jobId, isOwner);
        return isOwner;
    }

    @Transactional(readOnly = true)
    public boolean canUpdateApplication(Long applicationId, ApplicationStatus status, Authentication authentication) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Checking application update permission - appId={}, status={}", requestId, applicationId, status);
        if (isAuthenticated(authentication)) {
            log.debug("Request [{}]: Application update permission failed - not authenticated", requestId);
            return false;
        }

        JobApplication application = findApplicationById(applicationId);
        String email = getEmail(authentication);

        if (isAdmin(authentication)) {
            log.debug("Request [{}]: Application update permitted - admin user", requestId);
            return true;
        }

        if (isApplicationJobOwner(application, email)) {
            log.debug("Request [{}]: Application update permitted - job owner", requestId);
            return true;
        }

        if (isApplicationCandidate(application, email)) {
            boolean permitted = status == ApplicationStatus.REJECTED;
            log.debug("Request [{}]: Application update permitted for candidate - permitted={}", requestId, permitted);
            return permitted;
        }
        log.debug("Request [{}]: Application update permission denied", requestId);
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
        return authentication == null || !authentication.isAuthenticated();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    private String getEmail(Authentication authentication) {
        return authentication.getName();
    }

    private boolean isJobOwner(Job job, String email) {
        return job.getPostedBy().getEmail().equalsIgnoreCase(email);
    }

    private boolean isApplicationJobOwner(JobApplication application, String email) {
        return application.getJob().getPostedBy().getEmail().equalsIgnoreCase(email);
    }

    private boolean isApplicationCandidate(JobApplication application, String email) {
        return application.getCandidate().getEmail().equalsIgnoreCase(email);
    }
}
