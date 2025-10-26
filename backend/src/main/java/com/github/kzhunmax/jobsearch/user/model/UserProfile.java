package com.github.kzhunmax.jobsearch.user.model;

import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.shared.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
    @Column(name = "position", nullable = false)
    private String position;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience", nullable = false, length = 30)
    private ExperienceLevel experience;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", nullable = false, length = 20)
    private WorkMode workMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false, length = 20)
    private WorkFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_status", nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private ActivityStatus activityStatus;

    private String portfolioUrl;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Resume> resumes = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LanguageSkill> languages = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
