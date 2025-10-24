package com.github.kzhunmax.jobsearch.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "language_skills")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LanguageSkill extends BaseEntity {

    @Column(name = "language", nullable = false, length = 50)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private LanguageLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;
}
