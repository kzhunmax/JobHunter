package com.github.kzhunmax.jobsearch.user.service;

import com.github.kzhunmax.jobsearch.exception.MaxResumesReachedException;
import com.github.kzhunmax.jobsearch.exception.ResumeLinkedToApplicationsException;
import com.github.kzhunmax.jobsearch.exception.ResumeOwnershipException;
import com.github.kzhunmax.jobsearch.job.repository.JobApplicationRepository;
import com.github.kzhunmax.jobsearch.shared.FileStorageService;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.shared.validator.FileValidator;
import com.github.kzhunmax.jobsearch.user.dto.ResumeSummaryDTO;
import com.github.kzhunmax.jobsearch.user.mapper.ResumeMapper;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import com.github.kzhunmax.jobsearch.user.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final RepositoryHelper repositoryHelper;
    private final ResumeMapper resumeMapper;
    private final FileStorageService fileStorageService;
    private final FileValidator fileValidator;
    private final JobApplicationRepository jobApplicationRepository;

    private static final int MAX_RESUMES_PER_USER = 2;

    @Transactional(readOnly = true)
    public List<ResumeSummaryDTO> getAllResumes(Long userId) {
        log.info("Fetching all resumes for user ID={}", userId);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        return userProfile.getResumes().stream()
                .map(resumeMapper::toDto)
                .toList();
    }

    public ResumeSummaryDTO addResume(Long userId, MultipartFile file) {
        log.info("Adding resume for user ID={}", userId);
        fileValidator.validateResume(file);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        if (userProfile.getResumes().size() >= MAX_RESUMES_PER_USER) {
            log.warn("User ID={} already has the maximum ({}) number of resumes.", userId, MAX_RESUMES_PER_USER);
            throw new MaxResumesReachedException(MAX_RESUMES_PER_USER);
        }
        String fileUrl = fileStorageService.uploadFileToSupabase(file, userId);

        Resume newResume = Resume.builder()
                .userProfile(userProfile)
                .title(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())))
                .fileUrl(fileUrl)
                .build();

        Resume savedResume = resumeRepository.save(newResume);
        userProfile.getResumes().add(savedResume);

        log.info("Resume added successfully for user ID={} with ID={}", userId, savedResume.getId());
        return resumeMapper.toDto(savedResume);
    }

    public ResumeSummaryDTO updateResume(Long resumeId, Long userId, MultipartFile file) {
        log.info("Updating resume ID={} for user ID={}", resumeId, userId);

        fileValidator.validateResume(file);

        Resume resume = findResumeByIdAndUserId(resumeId, userId);
        String oldFileUrl = resume.getFileUrl();

        String newFileUrl = fileStorageService.uploadFileToSupabase(file, userId);

        resume.setTitle(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        resume.setFileUrl(newFileUrl);

        Resume updatedResume = resumeRepository.save(resume);

        fileStorageService.deleteFileFromSupabase(oldFileUrl);

        log.info("Resume ID={} updated successfully for user ID={}", resumeId, userId);
        return resumeMapper.toDto(updatedResume);
    }

    public void deleteResume(Long resumeId, Long userId) {
        log.info("Deleting resume ID={} for user ID={}", resumeId, userId);

        Resume resume = findResumeByIdAndUserId(resumeId, userId);

        // Prevent deletion if any JobApplication references this Resume
        if (jobApplicationRepository.existsByResumeId(resumeId)) {
            log.warn("Cannot delete resume ID={} because it is referenced by one or more job applications.", resumeId);
            throw new ResumeLinkedToApplicationsException();
        }

        String fileUrl = resume.getFileUrl();
        UserProfile profile = resume.getUserProfile();
        profile.getResumes().remove(resume);

        resumeRepository.delete(resume);
        fileStorageService.deleteFileFromSupabase(fileUrl);
        log.info("Resume ID={} deleted successfully for user ID={}", resumeId, userId);
    }

    private Resume findResumeByIdAndUserId(Long resumeId, Long userId) {
        Resume resume = repositoryHelper.findResumeById(resumeId);

        if (!Objects.equals(resume.getUserProfile().getUser().getId(), userId)) {
            log.warn("User ID={} attempted to access resume ID={} owned by another user.", userId, resumeId);
            throw new ResumeOwnershipException();
        }
        return resume;
    }
}
