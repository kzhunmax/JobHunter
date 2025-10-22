package com.github.kzhunmax.jobsearch.mapper;

import com.github.kzhunmax.jobsearch.dto.response.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.model.JobApplication;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationMapper {

    public JobApplicationResponseDTO toDto(JobApplication application) {
        if (application == null) return null;

        var job = application.getJob();
        var candidate = application.getCandidate();
        var resume = application.getResume();

        return new JobApplicationResponseDTO(
                application.getId(),
                job != null ? job.getId() : null,
                job != null ? job.getTitle() : null,
                job != null ? job.getCompany() : null,
                candidate != null ? candidate.getEmail() : null,
                application.getStatus() != null ? application.getStatus().name() : null,
                application.getAppliedAt() != null ? application.getAppliedAt().toString() : null,
                application.getCoverLetter(),
                resume != null ? resume.getFileUrl() : null
        );
    }
}