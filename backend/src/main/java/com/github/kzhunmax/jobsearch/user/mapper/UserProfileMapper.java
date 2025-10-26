package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileMapper {

    private final LanguageSkillMapper languageSkillMapper;
    private final ResumeMapper resumeMapper;

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
}
