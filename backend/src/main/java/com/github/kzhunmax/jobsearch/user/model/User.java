package com.github.kzhunmax.jobsearch.user.model;

import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.security.PricingPlan;
import com.github.kzhunmax.jobsearch.shared.enums.AuthProvider;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.shared.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(columnList = "email", name = "idx_users_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class User extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private AuthProvider provider;

    @Builder.Default
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 50)
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "postedBy", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Job> jobs = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<JobApplication> applications = new HashSet<>();

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    @Column(name = "reset_password_token_expiry")
    private Instant resetPasswordTokenExpiry;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(name = "email_verify_token")
    private String emailVerifyToken;

    @OneToOne(mappedBy = "user")
    private UserProfile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_plan", nullable = false)
    @Builder.Default
    private PricingPlan pricingPlan = PricingPlan.FREE;

    @Column(name = "api_key", unique = true)
    private String apiKey;

    public void generateApiKey() {
        if (this.apiKey == null) {
            this.apiKey = "js_key_" + UUID.randomUUID().toString().replace("-", "");
        }
    }

    @Override
    protected void onSave() {
        if (email != null) {
            email = email.toLowerCase();
        }
    }
}
