package com.github.kzhunmax.jobsearch.company.service;

import com.github.kzhunmax.jobsearch.company.dto.CompanyRequestDTO;
import com.github.kzhunmax.jobsearch.company.dto.CompanyResponseDTO;
import com.github.kzhunmax.jobsearch.company.mapper.CompanyMapper;
import com.github.kzhunmax.jobsearch.company.model.Company;
import com.github.kzhunmax.jobsearch.company.repository.CompanyRepository;
import com.github.kzhunmax.jobsearch.exception.CompanyAlreadyExistsException;
import com.github.kzhunmax.jobsearch.shared.RepositoryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final RepositoryHelper repositoryHelper;

    @Transactional
    public CompanyResponseDTO getCompany(Long companyId) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Fetching company - companyId={}", requestId, companyId);
        Company company = repositoryHelper.findCompanyById(companyId);
        return companyMapper.toDto(company);
    }

    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Creating company - name={}", requestId, dto.name());
        String normalizedName = Company.normalize(dto.name());
        if (companyRepository.existsCompanyByNormalizedName(normalizedName)) {
            throw new CompanyAlreadyExistsException(dto.name());
        }
        Company company = companyMapper.toEntity(dto);
        Company savedCompany = companyRepository.save(company);
        log.info("Request [{}]: Company created successfully - companyId={}", requestId, savedCompany.getId());
        return companyMapper.toDto(savedCompany);
    }

    public CompanyResponseDTO updateCompany(Long companyId, CompanyRequestDTO dto) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);
        log.info("Request [{}]: Updating company - companyId={}", requestId, companyId);
        Company company = repositoryHelper.findCompanyById(companyId);

        String normalizedName = Company.normalize(dto.name());
        companyRepository.findCompanyByNormalizedName(normalizedName).ifPresent(existing -> {
            if (!existing.getId().equals(companyId)) {
                throw new CompanyAlreadyExistsException(dto.name());
            }
        });

        companyMapper.updateEntityFromDto(dto, company);
        Company updatedCompany = companyRepository.save(company);
        log.info("Request [{}]: Company updated successfully - companyId={}", requestId, companyId);
        return companyMapper.toDto(updatedCompany);
    }

}
