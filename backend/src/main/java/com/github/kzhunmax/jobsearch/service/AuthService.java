package com.github.kzhunmax.jobsearch.service;

import com.github.kzhunmax.jobsearch.dto.request.UserRegistrationDTO;
import com.github.kzhunmax.jobsearch.dto.response.UserResponseDTO;
import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;
import com.github.kzhunmax.jobsearch.repository.UserRepository;
import com.github.kzhunmax.jobsearch.validator.UserRegistrationValidator;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRegistrationValidator userRegistrationValidator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public UserResponseDTO registerUser(UserRegistrationDTO dto) {
        userRegistrationValidator.validateRegistration(dto);
        Set<Role> roles = resolveRoles(dto.roles());
        User user = buildUserEntity(dto, roles);
        User savedUser = userRepository.save(user);
        return buildUserResponse(savedUser);
    }

    private User buildUserEntity(UserRegistrationDTO dto, Set<Role> roles) {
        return User.builder()
                .username(dto.username().trim())
                .email(dto.email().trim())
                .password(passwordEncoder.encode(dto.password()))
                .roles(roles)
                .build();
    }

    private UserResponseDTO buildUserResponse(User user) {
        return new UserResponseDTO(
                user.getUsername(),
                user.getEmail(),
                user.getRoles()
        );
    }

    private Set<Role> resolveRoles(Set<Role> requestedRoles) {
        return new HashSet<>(requestedRoles != null ? requestedRoles : Set.of(Role.ROLE_CANDIDATE));
    }
}
