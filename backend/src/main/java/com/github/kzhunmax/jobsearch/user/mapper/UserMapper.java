package com.github.kzhunmax.jobsearch.user.mapper;

import com.github.kzhunmax.jobsearch.user.dto.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserResponseDTO;
import com.github.kzhunmax.jobsearch.shared.enums.AuthProvider;
import com.github.kzhunmax.jobsearch.shared.enums.Role;
import com.github.kzhunmax.jobsearch.user.model.User;
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
                .provider(AuthProvider.LOCAL)
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
