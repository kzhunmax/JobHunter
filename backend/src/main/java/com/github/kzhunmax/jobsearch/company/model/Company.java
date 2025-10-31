package com.github.kzhunmax.jobsearch.company.model;

import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String location;

    private String website;

    @Column(name = "logo_url")
    private String logoUrl;

    @Builder.Default
    @OneToMany(mappedBy = "company")
    private Set<UserProfile> recruiters = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "company")
    private Set<Job> jobs = new HashSet<>();
}
