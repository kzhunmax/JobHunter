package com.github.kzhunmax.jobsearch.company.controller;

import com.github.kzhunmax.jobsearch.company.dto.CompanyRequestDTO;
import com.github.kzhunmax.jobsearch.company.dto.CompanyResponseDTO;
import com.github.kzhunmax.jobsearch.company.service.CompanyService;
import com.github.kzhunmax.jobsearch.payload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Company", description = "Endpoints for managing companies")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    @Operation(summary = "Get company details by ID")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> getCompany(@PathVariable Long companyId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        CompanyResponseDTO company = companyService.getCompany(companyId);
        return ApiResponse.success(company, requestId);
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new company")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> createCompany(@Valid @RequestBody CompanyRequestDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        CompanyResponseDTO company = companyService.createCompany(dto);
        return ApiResponse.created(company, requestId);
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Update company details")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyRequestDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        CompanyResponseDTO company = companyService.updateCompany(companyId, dto);
        return ApiResponse.success(company, requestId);
    }
}
