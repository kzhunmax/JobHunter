package com.github.kzhunmax.jobsearch.mapper;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public JobApplicationResponseDTO toDto(JobApplication application) {
        return new JobApplicationResponseDTO(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                application.getJob().getCompany(),
                application.getCandidate().getUsername(),
                application.getStatus().name(),
                application.getAppliedAt().toString(),
                application.getCoverLetter()
        );
    }
}