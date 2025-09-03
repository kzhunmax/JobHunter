package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.Job;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import com.github.kzhunmax.jobsearch.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByJob(Job job, Pageable pageable);
    Page<JobApplication> findByCandidate(User candidate, Pageable pageable);
    Optional<JobApplication> findByJobAndCandidate(Job job, User candidate);
}
