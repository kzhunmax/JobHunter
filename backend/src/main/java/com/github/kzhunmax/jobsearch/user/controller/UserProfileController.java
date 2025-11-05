package com.github.kzhunmax.jobsearch.user.controller;

import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import com.github.kzhunmax.jobsearch.security.UserDetailsImpl;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileRequestDTO;
import com.github.kzhunmax.jobsearch.user.dto.UserProfileResponseDTO;
import com.github.kzhunmax.jobsearch.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        Long userId = userDetails.getId();
        log.info("Getting profile for user='{}'", userId);
        UserProfileResponseDTO profileDto = userProfileService.getUserProfileByUserId(userId);
        log.info("Profile retrieved successfully - profileId={}", profileDto.id());
        return ApiResponse.success(profileDto);
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
        Long userId = userDetails.getId();
        log.info("Creating profile for user='{}'", userId);
        UserProfileResponseDTO profile = userProfileService.createProfile(requestDTO, userId);
        log.info("User profile created successfully - userId={}", userId);
        return ApiResponse.created(profile);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile updated successfully",
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
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<UserProfileResponseDTO>> updateProfile(
            @Valid @RequestBody UserProfileRequestDTO requestDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Updating profile for user='{}'", userId);
        UserProfileResponseDTO updatedProfile = userProfileService.updateProfile(requestDTO, userId);
        log.info("User profile updated successfully - userId={}", userId);
        return ApiResponse.success(updatedProfile);
    }

    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete user profile")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "User profile deleted successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User profile not found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<Void>> deleteProfile(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Deleting profile for user='{}'", userId);
        userProfileService.deleteProfile(userId);
        log.info("User profile deleted successfully - userId={}", userId);
        return ApiResponse.noContent();
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload profile photo")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Photo uploaded successfully, returns public URL",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid file (e.g., too large, wrong type)",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @Parameter(description = "Image of type PNG, JPG with max size 2MB", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long userId = userDetails.getId();
        log.info("Uploading profile photo for user='{}'", userId);
        String photoUrl = userProfileService.uploadProfilePhoto(file, userId);
        log.info("Profile photo uploaded successfully for user='{}' - url={}", userId, photoUrl);
        return ApiResponse.success(photoUrl);
    }
}
