package com.github.kzhunmax.jobsearch.user.repository;

import com.github.kzhunmax.jobsearch.user.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
