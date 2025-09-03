package com.github.kzhunmax.jobsearch.dto.response;

import com.github.kzhunmax.jobsearch.model.Role;
import com.github.kzhunmax.jobsearch.model.User;

import java.util.Set;

public record UserResponseDTO(String username, String email, Set<Role> roles) {
    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(user.getUsername(), user.getEmail(), user.getRoles());
    }
}

