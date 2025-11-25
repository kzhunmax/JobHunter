package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.company.mapper.CompanyMapper;
import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
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

@Mapper(componentModel = "spring", uses = {LanguageSkillMapper.class, ResumeMapper.class, CompanyMapper.class})
public abstract class UserProfileMapper {

    @Autowired
    private LanguageSkillMapper languageSkillMapper;

    @Autowired
    private ResumeMapper resumeMapper;

    @Autowired
    private RepositoryHelper repositoryHelper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "activityStatus", constant = "ACTIVE")
    @Mapping(target = "profileType", source = "user", qualifiedByName = "determineProfileType")
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "company", source = "dto.companyId", qualifiedByName = "mapCompanyFromId")
    public abstract UserProfile toEntity(UserProfileRequestDTO dto, User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "languages", ignore = true)
    @Mapping(target = "activityStatus", ignore = true)
    @Mapping(target = "profileType", ignore = true)
    @Mapping(target = "photoUrl", ignore = true)
    @Mapping(target = "company", source = "dto.companyId", qualifiedByName = "mapCompanyFromId")
    public abstract void updateEntityFromDto(UserProfileRequestDTO dto, @MappingTarget UserProfile userProfile);

    public abstract UserProfileResponseDTO toDto(UserProfile userProfile);

    @Named("determineProfileType")
    protected ProfileType determineProfileType(User user) {
        if (user != null && user.getRoles() != null && user.getRoles().contains(Role.ROLE_RECRUITER)) {
            return ProfileType.RECRUITER;
        }
        return ProfileType.CANDIDATE;
    }

    @AfterMapping
    protected void afterToEntity(@MappingTarget UserProfile profile, UserProfileRequestDTO dto) {
        if (profile.getId() == null) {
            mapLanguages(profile, dto);
        }
    }

    @AfterMapping
    protected void afterUpdateEntityFromDto(@MappingTarget UserProfile profile, UserProfileRequestDTO dto) {
        profile.getLanguages().clear();
        mapLanguages(profile, dto);
    }

    private void mapLanguages(UserProfile profile, UserProfileRequestDTO dto) {
        if (dto.languages() != null && !dto.languages().isEmpty()) {
            List<LanguageSkill> languageSkills = dto.languages().stream()
                    .map(langDto -> languageSkillMapper.toEntity(langDto, profile))
                    .toList();
            profile.getLanguages().addAll(languageSkills);
        }
    }

    @Named("mapCompanyFromId")
    protected Company mapCompanyFromId(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return repositoryHelper.findCompanyById(companyId);
    }
}
