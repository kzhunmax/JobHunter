package com.github.kzhunmax.jobsearch.user.service;

import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.shared.FileStorageService;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.validator.FileValidator;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.mapper.UserProfileMapper;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final RepositoryHelper repositoryHelper;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;
    private final JobApplicationRepository jobApplicationRepository;

    public UserProfileResponseDTO getUserProfileByUserId(Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching user profile - userId={}", requestId, userId);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        log.info("Request [{}]: User profile fetched successfully - userId={}", requestId, userId);
        return userProfileMapper.toDto(userProfile);

    }

    public UserProfileResponseDTO createProfile(UserProfileRequestDTO dto, Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Creating user profile - userId={}", requestId, userId);
        User user = repositoryHelper.findUserById(userId);
        UserProfile profile = userProfileMapper.toEntity(dto, user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Request [{}]: Job created successfully - jobId={}", requestId, savedProfile.getId());
        return userProfileMapper.toDto(savedProfile);
    }

    public UserProfileResponseDTO updateProfile(UserProfileRequestDTO dto, Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating user profile - userId={}", requestId, userId);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);
        userProfileMapper.updateEntityFromDto(dto, profile);
        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Request [{}]: User profile updated successfully - userId={}", requestId, userId);
        return userProfileMapper.toDto(savedProfile);
    }

    public void deleteProfile(Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Deleting user profile - userId={}", requestId, userId);
        User user = repositoryHelper.findUserById(userId);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);
        List<JobApplication> applications = jobApplicationRepository.findAllByCandidate(user);
        if (!applications.isEmpty()) {
            log.debug("Request [{}]: Deleting {} job applications for user {}", requestId, applications.size(), userId);
            jobApplicationRepository.deleteAllInBatch(applications);
            user.getApplications().clear();
        }
        userProfileRepository.delete(profile);
        log.info("Request [{}]: User profile deleted successfully - userId={}", requestId, userId);
    }

    public String uploadProfilePhoto(MultipartFile file, Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Uploading profile photo - userId={}", requestId, userId);

        fileValidator.validateProfilePhoto(file);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);
        String photoUrl = fileStorageService.uploadFileToSupabase(file, userId, requestId);
        profile.setPhotoUrl(photoUrl);
        userProfileRepository.save(profile);
        log.info("Request [{}]: Profile photo uploaded and profile updated - userId={}", requestId, userId);
        return photoUrl;
    }
}
