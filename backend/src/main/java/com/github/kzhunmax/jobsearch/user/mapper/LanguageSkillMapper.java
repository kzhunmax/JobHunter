package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.LanguageSkillResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.LanguageSkill;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LanguageSkillMapper {

    public LanguageSkillResponseDTO toDto(LanguageSkill languageSkill) {
        if (languageSkill == null) return null;

        return new LanguageSkillResponseDTO(
                languageSkill.getId(),
                languageSkill.getLanguage(),
                languageSkill.getLevel()
        );
    }

    public List<LanguageSkillResponseDTO> toDtoList(List<LanguageSkill> languageSkills) {
        return languageSkills.stream()
                .map(this::toDto)
                .toList();
    }
}
