package com.github.kzhunmax.jobsearch.controller;

import com.github.kzhunmax.jobsearch.dto.response.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.service.UserProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profiles", description = "Endpoints for user profiles and their resumes")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getUserProfileById(
            Authentication authentication
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = getCurrentUserId(authentication);
        log.info("Request [{}]: Getting profile for user='{}'", requestId, userId);
        UserProfileResponseDTO profileDto = userProfileService.getUserProfileById(userId);
        log.info("Request [{}]: Profile retrieved successfully - profileId={}", requestId, profileDto.id());
        return ApiResponse.success(profileDto, requestId);
    }

    private Long getCurrentUserId(Authentication authentication) {
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }
}
