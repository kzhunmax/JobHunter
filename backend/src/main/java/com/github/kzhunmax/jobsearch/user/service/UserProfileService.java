package com.github.kzhunmax.jobsearch.user.service;

import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.mapper.UserProfileMapper;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.UserProfileRepository;
import com.github.kzhunmax.jobsearch.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final UserRepository userRepository;

    public UserProfileResponseDTO getUserProfileById(Long profileId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching user profiles - profileId={}", requestId, profileId);
        UserProfile userProfile = findUserProfileById(profileId);
        log.info("Request [{}]: User profile fetched successfully - profileId={}", requestId, profileId);
        return userProfileMapper.toDto(userProfile);

    }

    public UserProfileResponseDTO createProfile(UserProfileRequestDTO dto, Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Creating user profile - userId={}", requestId, userId);
        User user = findUserById(userId);
        UserProfile profile = userProfileMapper.toEntity(dto, user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Request [{}]: Job created successfully - jobId={}", requestId, savedProfile.getId());
        return userProfileMapper.toDto(savedProfile);
    }

    private UserProfile findUserProfileById(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
    }
}
