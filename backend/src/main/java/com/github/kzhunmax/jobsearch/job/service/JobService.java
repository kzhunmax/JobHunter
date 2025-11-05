package com.github.kzhunmax.jobsearch.job.service;

import com.github.kzhunmax.jobsearch.event.producer.UserEventProducer;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.mapper.JobMapper;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.shared.event.JobSyncEvent;
import com.github.kzhunmax.jobsearch.shared.event.SyncAction;
import com.github.kzhunmax.jobsearch.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JobService {
    private final JobRepository jobRepository;
    private final RepositoryHelper repositoryHelper;
    private final JobMapper jobMapper;
    private final UserEventProducer eventProducer;

    @Transactional
    public JobResponseDTO createJob(JobRequestDTO dto, Long userId) {
        log.info("Creating job - userId={}", userId);
        User user = repositoryHelper.findUserById(userId);
        Job job = jobMapper.toEntity(dto, user);
        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully - jobId={}", savedJob.getId());
        eventProducer.sendJobSyncEvent(new JobSyncEvent(savedJob.getId(), SyncAction.UPSERT));
        return jobMapper.toDto(savedJob);
    }

    @Cacheable(value = "jobs", key = "#jobId")
    @Transactional(readOnly = true)
    public JobResponseDTO getJobById(Long jobId) {
        log.info("Fetching job - jobId={}", jobId);
        Job job = repositoryHelper.findJobById(jobId);
        log.info("Job fetched successfully - jobId={}", jobId);
        return jobMapper.toDto(job);
    }

    @CachePut(value = "jobs", key = "#jobId")
    @Transactional
    public JobResponseDTO updateJob(Long jobId, JobRequestDTO dto) {
        log.info("Updating job - jobId={}", jobId);
        Job job = repositoryHelper.findJobById(jobId);
        jobMapper.updateEntityFromDto(dto, job);
        Job updatedJob = jobRepository.save(job);
        log.info("Job updated successfully - jobId={}", jobId);
        eventProducer.sendJobSyncEvent(new JobSyncEvent(updatedJob.getId(), SyncAction.UPSERT));
        return jobMapper.toDto(updatedJob);
    }

    @CacheEvict(value = "jobs", key = "#jobId")
    @Transactional
    public void deleteJob(Long jobId) {
        log.info("Deleting job - jobId={}", jobId);

        Job job = repositoryHelper.findJobById(jobId);
        job.setActive(false);

        log.info("Deactivating job - jobId={}. Updating open applications to REJECTED.", jobId);
        for (JobApplication application : job.getApplications()) {
            if (application.getStatus() == ApplicationStatus.APPLIED || application.getStatus() == ApplicationStatus.UNDER_REVIEW) {
                application.setStatus(ApplicationStatus.REJECTED);
            }
        }
        jobRepository.save(job);
        log.info("Job deleted successfully - jobId={}", jobId);
        eventProducer.sendJobSyncEvent(new JobSyncEvent(job.getId(), SyncAction.DELETE));
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobResponseDTO>> getAllActiveJobs(
            Pageable pageable,
            PagedResourcesAssembler<JobResponseDTO> pagedAssembler
    ) {
        log.info("Fetching all active jobs - pageable={}", pageable);
        Page<JobResponseDTO> jobPage = jobRepository.findByActiveTrue(pageable)
                .map(jobMapper::toDto);
        long total = jobPage.getTotalElements();
        log.info("Found {} active jobs", total);
        return pagedAssembler.toModel(jobPage, EntityModel::of);
    }

    @Transactional(readOnly = true)
    public PagedModel<EntityModel<JobResponseDTO>> getJobsByRecruiter(
            Long userId,
            Pageable pageable,
            PagedResourcesAssembler<JobResponseDTO> pagedAssembler
    ) {
        log.info("Fetching jobs by recruiter - userId={}, pageable={}", userId, pageable);
        Page<JobResponseDTO> dtoPage = jobRepository.findByPostedById(userId, pageable)
                .map(jobMapper::toDto);

        long total = dtoPage.getTotalElements();
        log.info("Found {} jobs for recruiter={}", total, userId);
        return pagedAssembler.toModel(dtoPage, EntityModel::of);
    }
}
