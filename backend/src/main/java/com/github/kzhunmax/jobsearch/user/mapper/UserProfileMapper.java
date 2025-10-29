package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.shared.enums.ProfileType;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.LanguageSkill;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {LanguageSkillMapper.class, ResumeMapper.class})
public abstract class UserProfileMapper {

    @Autowired
    private LanguageSkillMapper languageSkillMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Mapping(target = "activityStatus", constant = "ACTIVE")
    @Mapping(target = "profileType", source = "user", qualifiedByName = "determineProfileType")
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "user", source = "user")
    public abstract UserProfile toEntity(UserProfileRequestDTO dto, User user);


    public abstract UserProfileResponseDTO toDto(UserProfile userProfile);

    @Named("determineProfileType")
    protected ProfileType determineProfileType(User user) {
        if (user != null && user.getRoles() != null && user.getRoles().contains(Role.ROLE_RECRUITER)) {
            return ProfileType.RECRUITER;
        }
        return ProfileType.CANDIDATE;
    }

    /*
    // After mapping DTO -> Entity, manually map the languages list
    // because MapStruct needs the 'profile' instance for the LanguageSkillMapper.toEntity method
    */
    @AfterMapping
    protected void afterToEntity(@MappingTarget UserProfile profile, UserProfileRequestDTO dto) {
        if (dto.languages() != null && !dto.languages().isEmpty()) {
            List<LanguageSkill> languageSkills = dto.languages().stream()
                    .map(langDto -> languageSkillMapper.toEntity(langDto, profile))
                    .collect(Collectors.toList());
            profile.setLanguages(languageSkills);
        }
    }
}
