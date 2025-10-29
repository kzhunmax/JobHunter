package com.github.kzhunmax.jobsearch.job.repository;

import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByJob(Job job, Pageable pageable);
    Page<JobApplication> findByCandidate(User candidate, Pageable pageable);
    Optional<JobApplication> findByJobAndCandidate(Job job, User candidate);
    List<JobApplication> findAllByCandidate(User candidate);
    boolean existsByResumeId(Long resumeId);
}
