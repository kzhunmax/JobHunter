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
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

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
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching all resumes for user ID={}", requestId, userId);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        return userProfile.getResumes().stream()
                .map(resumeMapper::toDto)
                .toList();
    }

    public ResumeSummaryDTO addResume(Long userId, MultipartFile file) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Adding resume for user ID={}", requestId, userId);
        fileValidator.validateResume(file);
        UserProfile userProfile = repositoryHelper.findUserProfileByUserId(userId);
        if (userProfile.getResumes().size() >= MAX_RESUMES_PER_USER) {
            log.warn("Request [{}]: User ID={} already has the maximum ({}) number of resumes.", requestId, userId, MAX_RESUMES_PER_USER);
            throw new MaxResumesReachedException(MAX_RESUMES_PER_USER);
        }
        String fileUrl = fileStorageService.uploadFileToSupabase(file, userId, requestId);

        Resume newResume = Resume.builder()
                .userProfile(userProfile)
                .title(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())))
                .fileUrl(fileUrl)
                .build();

        Resume savedResume = resumeRepository.save(newResume);
        userProfile.getResumes().add(savedResume);

        log.info("Request [{}]: Resume added successfully for user ID={} with ID={}", requestId, userId, savedResume.getId());
        return resumeMapper.toDto(savedResume);
    }

    public ResumeSummaryDTO updateResume(Long resumeId, Long userId, MultipartFile file) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating resume ID={} for user ID={}", requestId, resumeId, userId);

        fileValidator.validateResume(file);

        Resume resume = findResumeByIdAndUserId(resumeId, userId, requestId);
        String oldFileUrl = resume.getFileUrl();

        String newFileUrl = fileStorageService.uploadFileToSupabase(file, userId, requestId);

        resume.setTitle(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        resume.setFileUrl(newFileUrl);

        Resume updatedResume = resumeRepository.save(resume);

        fileStorageService.deleteFileFromSupabase(oldFileUrl, requestId);

        log.info("Request [{}]: Resume ID={} updated successfully for user ID={}", requestId, resumeId, userId);
        return resumeMapper.toDto(updatedResume);
    }

    public void deleteResume(Long resumeId, Long userId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Deleting resume ID={} for user ID={}", requestId, resumeId, userId);

        Resume resume = findResumeByIdAndUserId(resumeId, userId, requestId);

        // Prevent deletion if any JobApplication references this Resume
        if (jobApplicationRepository.existsByResumeId(resumeId)) {
            log.warn("Request [{}]: Cannot delete resume ID={} because it is referenced by one or more job applications.", requestId, resumeId);
            throw new ResumeLinkedToApplicationsException();
        }

        String fileUrl = resume.getFileUrl();
        UserProfile profile = resume.getUserProfile();
        profile.getResumes().remove(resume);

        resumeRepository.delete(resume);
        fileStorageService.deleteFileFromSupabase(fileUrl, requestId);
        log.info("Request [{}]: Resume ID={} deleted successfully for user ID={}", requestId, resumeId, userId);
    }

    private Resume findResumeByIdAndUserId(Long resumeId, Long userId, String requestId) {
        Resume resume = repositoryHelper.findResumeById(resumeId);

        if (!Objects.equals(resume.getUserProfile().getUser().getId(), userId)) {
            log.warn("Request [{}]: User ID={} attempted to access resume ID={} owned by another user.", requestId, userId, resumeId);
            throw new ResumeOwnershipException();
        }
        return resume;
    }
}
