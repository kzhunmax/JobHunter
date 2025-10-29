package com.github.kzhunmax.jobsearch.job.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Component
@Slf4j
public class ResumeValidator {

    private static final int ACCEPTABLE_FILE_SIZE = 5 * 1024 * 1024;

    public void validateResume(MultipartFile resumeFile) {
        if (resumeFile.isEmpty() || !Objects.equals(resumeFile.getContentType(), "application/pdf")) {
            throw new IllegalArgumentException("CV must be a non-empty PDF file");
        }
        if (resumeFile.getSize() > ACCEPTABLE_FILE_SIZE) {
            throw new IllegalArgumentException("CV size exceeds 5MB limit");
        }
    }
}
