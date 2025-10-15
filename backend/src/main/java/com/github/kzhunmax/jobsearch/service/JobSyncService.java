package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.es.JobDocument;
import com.github.kzhunmax.jobsearch.repository.JobRepository;
import com.github.kzhunmax.jobsearch.repository.es.JobSearchRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobSyncService {

    private final JobRepository jobRepository;
    private final JobSearchRepository jobSearchRepository;

    @PostConstruct
    @Transactional(readOnly = true)
    public void initialSync() {
        log.info("Starting initial sync of jobs from PostgreSQL to Elasticsearch...");
        if (jobSearchRepository.count() == 0 && jobRepository.count() > 0) {
            jobRepository.findAll().forEach(this::syncJob);
            log.info("Initial sync completed. Indexed {} jobs.", jobRepository.count());
        } else {
            log.info("Skipping initial sync. Elasticsearch index is not empty or no jobs in database.");
        }
    }

    public void syncJob(Job job) {
        JobDocument doc = JobDocument.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .company(job.getCompany())
                .location(job.getLocation())
                .salary(job.getSalary())
                .active(job.isActive())
                .build();
        jobSearchRepository.save(doc);
        log.info("Synchronized job with ID {} to Elasticsearch.", job.getId());
    }

    public void deleteJob(Long jobId) {
        jobSearchRepository.deleteById(jobId);
        log.info("Deleted job with ID {} from Elasticsearch.", jobId);
    }
}
