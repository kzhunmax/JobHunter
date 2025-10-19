package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.DuplicateApplicationException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.mapper.JobApplicationMapper;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;

    @Caching(evict = {
            @CacheEvict(value = "applicationByJob", allEntries = true),
            @CacheEvict(value = "applicationByCandidate", allEntries = true)
    })
    @Transactional
    public JobApplicationResponseDTO applyToJob(Long jobId, String email, String coverLetter) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Applying to job - jobId={}, email={}", requestId, jobId, email);
        Job job = findJobById(jobId);
        User candidate = findUserByEmail(email);
        validateNoDuplicateApplication(job, candidate);

        JobApplication application = createAndSaveApplication(job, candidate, coverLetter);
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
        Job job = findJobById(jobId);

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
    public JobApplicationResponseDTO updateApplicationStatus(Long appId, ApplicationStatus status) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating application status - appId={}", requestId, appId);
        JobApplication application = findApplicationById(appId);
        application.setStatus(status);
        JobApplication savedApplication = jobApplicationRepository.save(application);
        log.info("Request [{}]: Application status updated successfully - appId={}", requestId, appId);
        return jobApplicationMapper.toDto(savedApplication);
    }

    @Cacheable(value = "applicationByCandidate", key = "{#email, #pageable}")
    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsByCandidate(
            String email,
            Pageable pageable,
            PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching application by candidate - email={}, pageable={}", requestId, email, pageable);
        User candidate = findUserByEmail(email);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable).map(jobApplicationMapper::toDto);
        long total = applicationPage.getTotalPages();
        log.info("Request [{}]: Found {} applications for candidate - email={}", requestId, total, email);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    private JobApplication findApplicationById(Long appId) {
        return jobApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));
    }

    private void validateNoDuplicateApplication(Job job, User candidate) {
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent())
            throw new DuplicateApplicationException();
    }

    private JobApplication createAndSaveApplication(Job job, User candidate, String coverLetter) {
        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(coverLetter)
                .build();

        return jobApplicationRepository.save(application);
    }
}
