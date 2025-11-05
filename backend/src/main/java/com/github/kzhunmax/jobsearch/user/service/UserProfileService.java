package com.github.kzhunmax.jobsearch.user.service;

import com.github.kzhunmax.jobsearch.event.producer.UserEventProducer;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.job.repository.JobRepository;
import com.github.kzhunmax.jobsearch.shared.FileStorageService;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.enums.ProfileType;
import com.github.kzhunmax.jobsearch.shared.event.JobSyncEvent;
import com.github.kzhunmax.jobsearch.shared.event.SyncAction;
import com.github.kzhunmax.jobsearch.shared.validator.FileValidator;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.mapper.UserProfileMapper;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

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
    private final JobRepository jobRepository;
    private final UserEventProducer eventProducer;

    public UserProfileResponseDTO getUserProfileByUserId(Long userId) {
        log.info("Fetching user profile - userId={}", userId);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        log.info("User profile fetched successfully - userId={}", userId);
        return userProfileMapper.toDto(userProfile);

    }

    public UserProfileResponseDTO createProfile(UserProfileRequestDTO dto, Long userId) {
        log.info("Creating user profile - userId={}", userId);
        User user = repositoryHelper.findUserById(userId);
        UserProfile profile = userProfileMapper.toEntity(dto, user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("Job created successfully - jobId={}", savedProfile.getId());
        return userProfileMapper.toDto(savedProfile);
    }

    public UserProfileResponseDTO updateProfile(UserProfileRequestDTO dto, Long userId) {
        log.info("Updating user profile - userId={}", userId);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);
        userProfileMapper.updateEntityFromDto(dto, profile);
        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("User profile updated successfully - userId={}", userId);
        return userProfileMapper.toDto(savedProfile);
    }

    public void deleteProfile(Long userId) {
        log.info("Deleting user profile - userId={}", userId);

        User user = repositoryHelper.findUserById(userId);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);

        if (profile.getProfileType() == ProfileType.RECRUITER) {
            deactivateRecruiterJobs(user);
        }
        deleteCandidateApplications(user);

        userProfileRepository.delete(profile);
        log.info("User profile deleted successfully - userId={}", userId);
    }

    private void deactivateRecruiterJobs(User user) {
        Set<Job> jobs = user.getJobs();
        if (!jobs.isEmpty()) {
            jobs.forEach(job -> {
                job.setActive(false);
                eventProducer.sendJobSyncEvent(new JobSyncEvent(job.getId(), SyncAction.DELETE));
            });
            jobRepository.saveAll(jobs);
            log.info("Deactivated {} jobs for recruiter user {}", jobs.size(), user.getId());
        }
    }

    private void deleteCandidateApplications(User user) {
        List<JobApplication> applications = jobApplicationRepository.findAllByCandidate(user);
        if (!applications.isEmpty()) {
            jobApplicationRepository.deleteAllInBatch(applications);
            user.getApplications().clear();
            log.debug("Deleted {} job applications for user {}", applications.size(), user.getId());
        }
    }

    public String uploadProfilePhoto(MultipartFile file, Long userId) {
        log.info("Uploading profile photo - userId={}", userId);

        fileValidator.validateProfilePhoto(file);
        UserProfile profile = repositoryHelper.findUserProfileByUserId(userId);
        String oldPhotoUrl = profile.getPhotoUrl();
        String newPhotoUrl = fileStorageService.uploadFileToSupabase(file, userId);
        fileStorageService.deleteFileFromSupabase(oldPhotoUrl);
        profile.setPhotoUrl(newPhotoUrl);
        userProfileRepository.save(profile);
        log.info("Profile photo uploaded and profile updated - userId={}", userId);
        return newPhotoUrl;
    }
}
