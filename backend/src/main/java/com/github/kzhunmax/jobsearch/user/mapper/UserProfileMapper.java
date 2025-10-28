package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.shared.enums.ActivityStatus;
import com.github.kzhunmax.jobsearch.shared.enums.ProfileType;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.LanguageSkill;
import com.github.kzhunmax.jobsearch.user.model.User;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    private final LanguageSkillMapper languageSkillMapper;
    private final ResumeMapper resumeMapper;

    public UserProfile toEntity(UserProfileRequestDTO dto, User user) {
        if (dto == null || user == null) return null;

        UserProfile profile = UserProfile.builder()
                .profileType(determineProfileType(user))
                .fullName(dto.fullName())
                .phoneNumber(dto.phoneNumber())
                .photoUrl(dto.photoUrl())
                .about(dto.about())
                .country(dto.country())
                .city(dto.city())
                .position(dto.position())
                .experience(dto.experience())
                .workMode(dto.workMode())
                .format(dto.format())
                .activityStatus(ActivityStatus.ACTIVE)
                .portfolioUrl(dto.portfolioUrl())
                .resumes(new ArrayList<>())
                .user(user)
                .build();

        List<LanguageSkill> languageSkills = dto.languages().stream()
                .map(langDto -> languageSkillMapper.toEntity(langDto, profile))
                .collect(Collectors.toList());

        profile.setLanguages(languageSkills);

        return profile;
    }

    public UserProfileResponseDTO toDto(UserProfile userProfile) {
        if (userProfile == null) return null;

        return new UserProfileResponseDTO(
                userProfile.getId(),
                userProfile.getFullName(),
                userProfile.getPhoneNumber(),
                userProfile.getPhotoUrl(),
                userProfile.getAbout(),
                userProfile.getCountry(),
                userProfile.getCity(),
                userProfile.getPosition(),
                userProfile.getExperience(),
                userProfile.getWorkMode(),
                userProfile.getFormat(),
                userProfile.getPortfolioUrl(),
                languageSkillMapper.toDtoList(userProfile.getLanguages()),
                resumeMapper.toDtoList(userProfile.getResumes())
        );
    }

    private ProfileType determineProfileType(User user) {
        if (user.getRoles().contains(Role.ROLE_RECRUITER)) {
            return ProfileType.RECRUITER;
        }
        return ProfileType.CANDIDATE;
    }
}
