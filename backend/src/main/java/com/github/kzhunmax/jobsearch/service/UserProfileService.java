package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.response.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.exception.UserProfileNotFound;
import com.github.kzhunmax.jobsearch.mapper.UserProfileMapper;
import com.github.kzhunmax.jobsearch.model.UserProfile;
import com.github.kzhunmax.jobsearch.repository.UserProfileRepository;
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

    public UserProfileResponseDTO getUserProfileById(Long profileId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching user profiles - profileId={}", requestId, profileId);
        UserProfile userProfile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserProfileNotFound(profileId));
        log.info("Request [{}]: User profile fetched successfully - profileId={}", requestId, profileId);
        return userProfileMapper.toDto(userProfile);

    }
}
