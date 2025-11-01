package com.github.kzhunmax.jobsearch.job.service;

import com.github.kzhunmax.jobsearch.exception.ResumeOwnershipException;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.mapper.JobApplicationMapper;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.job.validator.JobApplicationValidator;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final RepositoryHelper repositoryHelper;
    private final JobApplicationValidator jobApplicationValidator;


    @Caching(evict = {
            @CacheEvict(value = "applicationByJob", allEntries = true),
            @CacheEvict(value = "applicationByCandidate", allEntries = true)
    })
    @Transactional
    public JobApplicationResponseDTO applyToJob(Long jobId, Long userId, JobApplicationRequestDTO requestDto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Applying to job - jobId={}, userId={}", requestId, jobId, userId);
        Job job = repositoryHelper.findJobById(jobId);
        User candidate = repositoryHelper.findUserById(userId);
        UserProfile candidateProfile = repositoryHelper.findUserProfileByUserId(userId);
        jobApplicationValidator.validateCandidateProfileIsComplete(candidateProfile, requestId);
        jobApplicationValidator.validateNoDuplicateApplication(job, candidate);
        Resume resume = repositoryHelper.findResumeById(requestDto.resumeId());
        if (!Objects.equals(resume.getUserProfile().getUser().getId(), userId)) {
            log.warn("Request [{}]: User ID={} attempted to apply with resume ID={} which they do not own.", requestId, userId, resume.getId());
            throw new ResumeOwnershipException();
        }
        JobApplication application = createAndSaveApplication(job, candidate, requestDto.coverLetter(), resume);
        log.info("Request [{}]: Application saved successfully - applicationId={}, jobId={}", requestId, application.getId(), jobId);
        return jobApplicationMapper.toDto(application);
    }

    @Cacheable(value = "applicationByJob", key = "{#jobId, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsForJob(
            Long jobId,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching applications for jobId={} | pageable={}", requestId, jobId, pageable);
        Job job = repositoryHelper.findJobById(jobId);

        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository
                .findByJob(job, pageable)
                .map(jobApplicationMapper::toDto);

        long total = applicationPage.getTotalElements();
        log.info("Request [{}]: Found {} applications for job - jobId={}", requestId, total, jobId);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    @Caching(evict = {
            @CacheEvict(value = "applicationByJob", allEntries = true),
            @CacheEvict(value = "applicationByCandidate", allEntries = true)
    })
    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(Long applicationId, ApplicationStatus status) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating application status - applicationId={}", requestId, applicationId);
        JobApplication application = repositoryHelper.findApplicationById(applicationId);
        application.setStatus(status);
        JobApplication savedApplication = jobApplicationRepository.save(application);
        log.info("Request [{}]: Application status updated successfully - applicationId={}", requestId, applicationId);
        return jobApplicationMapper.toDto(savedApplication);
    }

    @Cacheable(value = "applicationByCandidate", key = "{#userId, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsByCandidate(
            Long userId,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching application by candidate - userId={}, pageable={}", requestId, userId, pageable);
        User candidate = repositoryHelper.findUserById(userId);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable).map(jobApplicationMapper::toDto);
        long total = applicationPage.getTotalPages();
        log.info("Request [{}]: Found {} applications for candidate - userId={}", requestId, total, userId);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    private JobApplication createAndSaveApplication(Job job, User candidate, String coverLetter, Resume resume) {
        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(coverLetter)
                .resume(resume)
                .build();

        return jobApplicationRepository.save(application);
    }
}
