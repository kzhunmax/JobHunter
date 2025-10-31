package com.github.kzhunmax.jobsearch.job.mapper;

import com.github.kzhunmax.jobsearch.job.model.Job;
import com.github.kzhunmax.jobsearch.job.model.es.JobDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JobDocumentMapper {

    @Mapping(target = "active", source = "active")
    @Mapping(target = "company", source = "job.company.name")
    JobDocument toDocument(Job job);

}
