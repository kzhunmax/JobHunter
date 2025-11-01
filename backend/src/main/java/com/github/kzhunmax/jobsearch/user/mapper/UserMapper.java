package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserResponseDTO;
import com.github.kzhunmax.jobsearch.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mapping(target = "password", source = "dto.password", qualifiedByName = "encodePassword")
    @Mapping(target = "provider", constant = "LOCAL")
    @Mapping(target = "email", expression = "java(dto.email().trim().toLowerCase())")
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "resetPasswordTokenExpiry", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerifyToken", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "pricingPlan", ignore = true)
    @Mapping(target = "apiKey", ignore = true)
    public abstract User toEntity(UserRegistrationDTO dto, Set<Role> roles);

    public abstract UserResponseDTO toDto(User user);

    @Named("encodePassword")
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
