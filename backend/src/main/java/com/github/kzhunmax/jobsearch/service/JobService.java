package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.JobRequestDTO;
import com.github.kzhunmax.jobsearch.dto.response.JobResponseDTO;
import com.github.kzhunmax.jobsearch.exception.JobNotFoundException;
import com.github.kzhunmax.jobsearch.mapper.JobMapper;
import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final PagedResourcesAssembler<JobResponseDTO> pagedAssembler;
    private final JobSyncService jobSyncService;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponseDTO createJob(JobRequestDTO dto, String username) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Creating job - username={}", requestId, username);
        User user = findUserByUsername(username);
        Job job = jobMapper.toEntity(dto, user);
        Job savedJob = jobRepository.save(job);
        log.info("Request [{}]: Job created successfully - jobId={}", requestId, savedJob.getId());
        jobSyncService.syncJob(savedJob);
        return jobMapper.toDto(savedJob);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Cacheable(value = "jobs", key = "#jobId")
    @Transactional(readOnly = true)
    public JobResponseDTO getJobById(Long jobId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching job - jobId={}", requestId, jobId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        log.info("Request [{}]: Job fetched successfully - jobId={}", requestId, jobId);
        return jobMapper.toDto(job);
    }

    @CachePut(value = "jobs", key = "#jobId")
    @Transactional
    public JobResponseDTO updateJob(Long jobId, JobRequestDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating job - jobId={}", requestId, jobId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        jobMapper.updateEntityFromDto(dto, job);

        Job updatedJob = jobRepository.save(job);
        log.info("Request [{}]: Job updated successfully - jobId={}", requestId, jobId);
        jobSyncService.syncJob(updatedJob);
        return jobMapper.toDto(updatedJob);
    }

    @CacheEvict(value = "jobs", key = "#jobId")
    @Transactional
    public void deleteJob(Long jobId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Deleting job - jobId={}", requestId, jobId);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        job.setActive(false);
        jobRepository.save(job);
        log.info("Request [{}]: Job deleted successfully - jobId={}", requestId, jobId);
        jobSyncService.deleteJob(job.getId());
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobResponseDTO>> getAllActiveJobs(Pageable pageable) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching all active jobs - pageable={}", requestId, pageable);
        Page<JobResponseDTO> jobPage = jobRepository.findByActiveTrue(pageable)
                .map(jobMapper::toDto);
        long total = jobPage.getTotalElements();
        log.info("Request [{}]: Found {} active jobs", requestId, total);
        return pagedAssembler.toModel(jobPage, EntityModel::of);
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobResponseDTO>> getJobsByRecruiter(String username, Pageable pageable) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching jobs by recruiter - username={}, pageable={}", requestId, username, pageable);
        User recruiter = findUserByUsername(username);
        Page<JobResponseDTO> jobPage = jobRepository.findByPostedBy(recruiter, pageable)
                .map(jobMapper::toDto);

        long total = jobPage.getTotalElements();
        log.info("Request [{}]: Found {} jobs for recruiter={}", requestId, total, username);
        return pagedAssembler.toModel(jobPage, EntityModel::of);
    }
}
