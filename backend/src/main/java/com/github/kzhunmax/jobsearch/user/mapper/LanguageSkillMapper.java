package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.LanguageSkillRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.LanguageSkillResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.LanguageSkill;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LanguageSkillMapper {

    @Mapping(target = "userProfile", source = "userProfile")
    LanguageSkill toEntity(LanguageSkillRequestDTO dto, UserProfile userProfile);

    LanguageSkillResponseDTO toDto(LanguageSkill languageSkill);
}
