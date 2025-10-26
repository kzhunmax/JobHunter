package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.ResumeSummaryDTO;
import com.github.kzhunmax.jobsearch.user.model.Resume;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResumeMapper {

    public ResumeSummaryDTO toDto(Resume resume) {
        if (resume == null) return null;

        return new ResumeSummaryDTO(
                resume.getId(),
                resume.getTitle(),
                resume.getFileUrl()
        );
    }

    public List<ResumeSummaryDTO> toDtoList(List<Resume> resumes) {
        return resumes.stream()
                .map(this::toDto)
                .toList();
    }
}
