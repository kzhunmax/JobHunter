package com.github.kzhunmax.jobsearch.shared.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@Slf4j
public class FileValidator {

    private static final int RESUME_MAX_SIZE_MB = 5;
    private static final int PHOTO_MAX_SIZE_MB = 2;
    private static final long BYTES_PER_MB = 1024 * 1024;

    public void validateResume(MultipartFile resumeFile) {
        if (resumeFile.isEmpty() || !Objects.equals(resumeFile.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("CV must be a non-empty PDF file");
        }
        if (resumeFile.getSize() > RESUME_MAX_SIZE_MB * BYTES_PER_MB) {
            throw new IllegalArgumentException("CV size exceeds 5MB limit");
        }
    }

    public void validateProfilePhoto(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Profile photo file must not be empty");
        }
        if (file.getSize() > PHOTO_MAX_SIZE_MB * BYTES_PER_MB) {
            throw new IllegalArgumentException("Profile photo size exceeds 2MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg"))) {
            throw new IllegalArgumentException("Profile photo must be of type PNG or JPG/JPEG");
        }
    }
}
