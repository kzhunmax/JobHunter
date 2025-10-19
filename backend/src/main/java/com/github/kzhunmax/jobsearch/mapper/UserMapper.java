package com.github.kzhunmax.jobsearch.mapper;

import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User toEntity(UserRegistrationDTO dto, Set<Role> roles) {
        if (dto == null) return null;

        return User.builder()
                .email(dto.email().trim())
                .password(passwordEncoder.encode(dto.password()))
                .roles(roles)
                .build();
    }

    public UserResponseDTO toDto(User user) {
        if (user == null) return null;

        return new UserResponseDTO(
                user.getEmail(),
                user.getRoles()
        );
    }
}
