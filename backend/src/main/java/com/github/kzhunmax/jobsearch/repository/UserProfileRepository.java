package com.github.kzhunmax.jobsearch.repository;

import com.github.kzhunmax.jobsearch.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
