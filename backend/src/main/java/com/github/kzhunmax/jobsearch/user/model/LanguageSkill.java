package com.github.kzhunmax.jobsearch.user.model;

import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import com.github.kzhunmax.jobsearch.shared.enums.Language;
import com.github.kzhunmax.jobsearch.shared.enums.LanguageLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "language_skills")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LanguageSkill extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 30)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private LanguageLevel level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;
}
