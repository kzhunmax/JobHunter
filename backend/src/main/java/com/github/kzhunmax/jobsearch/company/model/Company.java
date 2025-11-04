package com.github.kzhunmax.jobsearch.company.model;

import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Company extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "normalized_name", unique = true)
    private String normalizedName;

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

    @Override
    protected void onSave() {
        this.normalizedName = normalize(this.name);
    }

    public static String normalize(String name) {
        if (name == null) return null;

        return name.toLowerCase()
                .replaceAll("[,.']", "")
                .replaceAll("\\s+(inc|llc|ltd|corp|corporation)$", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
