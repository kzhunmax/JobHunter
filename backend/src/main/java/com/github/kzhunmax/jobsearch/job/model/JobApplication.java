package com.github.kzhunmax.jobsearch.job.model;

import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.shared.enums.ApplicationStatus;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
    @Table(name = "job_applications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_id", "candidate_id"})
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobApplication extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private Instant appliedAt = Instant.now();

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;
}
