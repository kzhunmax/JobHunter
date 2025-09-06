package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.exception.ApplicationNotFoundException;
import com.github.kzhunmax.jobsearch.exception.DuplicateApplicationException;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.exception.UserNotFoundException;
import com.github.kzhunmax.jobsearch.mapper.JobApplicationMapper;
import com.github.kzhunmax.jobsearch.model.ApplicationStatus;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final PagedResourcesAssembler<JobApplicationResponseDTO> pagedAssembler;

    @Transactional
    public JobApplicationResponseDTO applyToJob(Long jobId, String username) {
        Job job = findJobById(jobId);
        User candidate = findUserByUsername(username);
        validateNoDuplicateApplication(job, candidate);

        JobApplication application = createAndSaveApplication(job, candidate);
        return jobApplicationMapper.toDto(application);
    }

    @Transactional
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsForJob(Long jobId, Pageable pageable) {
        Job job = findJobById(jobId);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByJob(job, pageable).map(jobApplicationMapper::toDto);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    @Transactional
    public JobApplicationResponseDTO updateApplicationStatus(Long appId, ApplicationStatus status) {
        JobApplication application = findApplicationById(appId);
        application.setStatus(status);
        JobApplication savedApplication = jobApplicationRepository.save(application);
        return jobApplicationMapper.toDto(savedApplication);
    }

    @Transactional
    public PagedModel<EntityModel<JobApplicationResponseDTO>> getApplicationsByCandidate(String username, Pageable pageable) {
        User candidate = findUserByUsername(username);
        Page<JobApplicationResponseDTO> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable).map(jobApplicationMapper::toDto);
        return pagedAssembler.toModel(applicationPage, EntityModel::of);
    }

    private Job findJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private JobApplication findApplicationById(Long appId) {
        return jobApplicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationNotFoundException(appId));
    }

    private void validateNoDuplicateApplication(Job job, User candidate) {
        if (jobApplicationRepository.findByJobAndCandidate(job, candidate).isPresent()) {
            throw new DuplicateApplicationException();
        }
    }

    private JobApplication createAndSaveApplication(Job job, User candidate) {
        JobApplication application = JobApplication.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .build();

        return jobApplicationRepository.save(application);
    }
}
