package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.job.dto.JobRequestDTO;
import com.github.kzhunmax.jobsearch.job.dto.JobResponseDTO;
import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class JobMapper {

    @Autowired
    private RepositoryHelper repositoryHelper;

    @Mapping(target = "active", constant = "true")
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "postedBy", source = "user")
    @Mapping(target = "company", source = "dto.companyId", qualifiedByName = "mapCompanyFromId")
    public abstract Job toEntity(JobRequestDTO dto, User user);

    @Mapping(target = "postedBy", source = "job.postedBy.email")
    @Mapping(target = "company", source = "job.company.name")
    public abstract JobResponseDTO toDto(Job job);

    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "postedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", source = "dto.companyId", qualifiedByName = "mapCompanyFromId")
    public abstract void updateEntityFromDto(JobRequestDTO dto, @MappingTarget Job job);

    @Named("mapCompanyFromId")
    protected Company mapCompanyFromId(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return repositoryHelper.findCompanyById(companyId);
    }
}
