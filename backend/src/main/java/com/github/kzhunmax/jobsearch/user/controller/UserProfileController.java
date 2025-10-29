package com.github.kzhunmax.jobsearch.user.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profiles", description = "Endpoints for user profiles and their resumes")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get current user profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    useReturnTypeSchema = true,
                    description = "User profile retrieved successfully"
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> getUserProfileById(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = userDetails.getId();
        log.info("Request [{}]: Getting profile for user='{}'", requestId, userId);
        UserProfileResponseDTO profileDto = userProfileService.getUserProfileByUserId(userId);
        log.info("Request [{}]: Profile retrieved successfully - profileId={}", requestId, profileDto.id());
        return ApiResponse.success(profileDto, requestId);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create user profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User profile created successfully",
                    useReturnTypeSchema = true
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid user profile data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> createProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User profile details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserProfileRequestDTO.class))
            )
            @Valid @RequestBody UserProfileRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        Long userId = userDetails.getId();
        log.info("Request [{}]: Creating profile for user='{}'", requestId, userId);
        UserProfileResponseDTO profile = userProfileService.createProfile(requestDTO, userId);
        log.info("Request [{}]: User profile created successfully - userId={}", requestId, userId);
        return ApiResponse.created(profile, requestId);
    }
}
