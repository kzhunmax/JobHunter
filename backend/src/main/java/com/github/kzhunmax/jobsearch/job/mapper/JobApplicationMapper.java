package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.job.dto.JobApplicationResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.JobApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobApplicationMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "jobTitle", source = "job.title")
    @Mapping(target = "company", source = "job.company")
    @Mapping(target = "candidateEmail", source = "candidate.email")
    @Mapping(target = "resumeUrl", source = "resume.fileUrl")
    @Mapping(target = "status", expression = "java(application.getStatus() != null ? application.getStatus().name() : null)")
    @Mapping(target = "appliedAt", expression = "java(application.getAppliedAt() != null ? application.getAppliedAt().toString() : null)")
    JobApplicationResponseDTO toDto(JobApplication application);
}