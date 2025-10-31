package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.ResumeSummaryDTO;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResumeMapper {

    ResumeSummaryDTO toDto(Resume resume);

}
