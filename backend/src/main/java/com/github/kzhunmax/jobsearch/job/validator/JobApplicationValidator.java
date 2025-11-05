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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobApplicationValidator {

    private final JobApplicationRepository jobApplicationRepository;

    public void validateNoDuplicateApplication(Job job, User candidate) {
        log.debug("Validating no duplicate application exists for jobId={} and candidateId={}", job.getId(), candidate.getId());
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent()) {
            log.warn("Duplicate application detected for jobId={} and candidateId={}", job.getId(), candidate.getId());
            throw new DuplicateApplicationException();
        }
        log.debug("No duplicate application found.");
    }

    public void validateCandidateProfileIsComplete(UserProfile profile) {
        if (profile.getProfileType() == ProfileType.CANDIDATE) {
            if (!StringUtils.hasText(profile.getPosition()) ||
                    profile.getExperience() == null ||
                    profile.getWorkMode() == null ||
                    profile.getFormat() == null) {

                log.warn("Candidate profile (ID={}) is incomplete. Blocking job application.", profile.getId());
                throw new IncompleteProfileException();
            }
        }
    }
}
