package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobMapper {

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "postedBy", source = "user")
    Job toEntity(JobRequestDTO dto, User user);

    @Mapping(target = "postedBy", source = "job.postedBy.email")
    JobResponseDTO toDto(Job job);

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(JobRequestDTO dto, @MappingTarget Job job);
}
