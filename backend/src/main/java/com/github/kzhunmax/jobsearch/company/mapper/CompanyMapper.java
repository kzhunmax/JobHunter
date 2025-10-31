package com.github.kzhunmax.jobsearch.company.mapper;

import com.github.kzhunmax.jobsearch.company.dto.CompanyRequestDTO;
import com.github.kzhunmax.jobsearch.company.dto.CompanyResponseDTO;
import com.github.kzhunmax.jobsearch.company.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    CompanyResponseDTO toDto(Company company);

    @Mapping(target = "recruiters", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    Company toEntity(CompanyRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recruiters", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(CompanyRequestDTO dto, @MappingTarget Company company);
}
