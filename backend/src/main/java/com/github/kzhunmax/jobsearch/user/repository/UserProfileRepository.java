package com.github.kzhunmax.jobsearch.user.repository;

import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
