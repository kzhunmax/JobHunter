package com.github.kzhunmax.jobsearch.user.model;

import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
public class Resume extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;
}
