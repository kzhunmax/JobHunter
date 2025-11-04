package com.github.kzhunmax.jobsearch.user.model;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.shared.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserProfile extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_type", nullable = false, length = 20)
    private ProfileType profileType;

    // general fields
    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phoneNumber;
    private String photoUrl;
    private String about;

    @Enumerated(EnumType.STRING)
    @Column(name = "country", nullable = false, length = 50)
    private Country country;

    private String city;

    // specific fields for candidate
    @Column(name = "position")
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience", length = 30)
    private ExperienceLevel experience;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", length = 20)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 20)
    private WorkFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private ActivityStatus activityStatus;

    private String portfolioUrl;

    @Builder.Default
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LanguageSkill> languages = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

}
