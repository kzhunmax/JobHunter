package com.github.kzhunmax.jobsearch.job.validator;

import com.github.kzhunmax.jobsearch.exception.DuplicateApplicationException;
import com.github.kzhunmax.jobsearch.exception.IncompleteProfileException;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.shared.enums.ProfileType;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobApplicationValidator {

    private final JobApplicationRepository jobApplicationRepository;

    public void validateNoDuplicateApplication(Job job, User candidate) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.debug("Request [{}]: Validating no duplicate application exists for jobId={} and candidateId={}",
                requestId, job.getId(), candidate.getId());
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent()) {
            log.warn("Request [{}]: Duplicate application detected for jobId={} and candidateId={}",
                    requestId, job.getId(), candidate.getId());
            throw new DuplicateApplicationException();
        }
        log.debug("Request [{}]: No duplicate application found.", requestId);
    }

    public void validateCandidateProfileIsComplete(UserProfile profile, String requestId) {
        if (profile.getProfileType() == ProfileType.CANDIDATE) {
            if (!StringUtils.hasText(profile.getPosition()) ||
                    profile.getExperience() == null ||
                    profile.getWorkMode() == null ||
                    profile.getFormat() == null) {

                log.warn("Request [{}]: Candidate profile (ID={}) is incomplete. Blocking job application.", requestId, profile.getId());
                throw new IncompleteProfileException();
            }
        }
    }
}
