package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
