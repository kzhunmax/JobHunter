package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public Job toEntity(JobRequestDTO dto, User user) {
        if (dto == null || user == null) return null;

        return Job.builder()
                .title(dto.title())
                .description(dto.description())
                .company(dto.company())
                .location(dto.location())
                .salary(dto.salary())
                .applicationDeadline(dto.applicationDeadline())
                .active(true)
                .postedBy(user)
                .build();
    }

    public JobResponseDTO toDto(Job job) {
        if (job == null) return null;

        return new JobResponseDTO(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getCompany(),
                job.getLocation(),
                job.getSalary(),
                job.getApplicationDeadline(),
                job.isActive(),
                job.getPostedBy() != null ? job.getPostedBy().getEmail() : null
        );
    }

    public void updateEntityFromDto(JobRequestDTO dto, Job job) {
        if (dto == null || job == null) return;

        if (dto.title() != null) job.setTitle(dto.title());
        if (dto.description() != null) job.setDescription(dto.description());
        if (dto.company() != null) job.setCompany(dto.company());
        if (dto.location() != null) job.setLocation(dto.location());
        if (dto.salary() != null) job.setSalary(dto.salary());
    }
}
