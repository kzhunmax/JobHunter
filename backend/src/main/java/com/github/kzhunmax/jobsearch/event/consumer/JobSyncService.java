package com.github.kzhunmax.jobsearch.event.consumer;

import com.github.kzhunmax.jobsearch.job.mapper.JobDocumentMapper;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.job.repository.es.JobSearchRepository;
import com.github.kzhunmax.jobsearch.shared.event.JobSyncEvent;
import com.github.kzhunmax.jobsearch.shared.event.SyncAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobSyncService {

    private final JobRepository jobRepository;
    private final JobSearchRepository jobSearchRepository;
    private final JobDocumentMapper jobDocumentMapper;

    @KafkaListener(
            containerFactory = "kafkaListenerContainerFactory",
            topics = "job-sync-events",
            groupId = "job-sync-group"
    )
    @Transactional(readOnly = true)
    public void onJobSyncEvent(JobSyncEvent event) {
        log.info("Received job sync event for jobId {}: {}", event.jobId(), event.action());

        if (event.action() == SyncAction.DELETE) {
            jobSearchRepository.deleteById(event.jobId());
            log.info("Deleted job with ID {} from Elasticsearch.", event.jobId());
            return;
        }

        Job job = jobRepository.findById(event.jobId()).orElse(null);
        if (job == null) {
            log.warn("Received job sync event for non-existent job ID: {}", event.jobId());
            return;
        }

        if (job.isActive()) {
            JobDocument doc = jobDocumentMapper.toDocument(job);
            jobSearchRepository.save(doc);
            log.info("Synchronized job with ID {} to Elasticsearch.", job.getId());
        } else {
            jobSearchRepository.deleteById(job.getId());
            log.info("Job with ID {} is inactive, deleting from Elasticsearch.", job.getId());
        }
    }
}
