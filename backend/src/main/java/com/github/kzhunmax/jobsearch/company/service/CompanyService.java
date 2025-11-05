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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        log.info("Fetching company - companyId={}", companyId);
        Company company = repositoryHelper.findCompanyById(companyId);
        return companyMapper.toDto(company);
    }

    public CompanyResponseDTO createCompany(CompanyRequestDTO dto) {
        log.info("Creating company - name={}", dto.name());
        String normalizedName = Company.normalize(dto.name());
        if (companyRepository.existsCompanyByNormalizedName(normalizedName)) {
            throw new CompanyAlreadyExistsException(dto.name());
        }
        Company company = companyMapper.toEntity(dto);
        Company savedCompany = companyRepository.save(company);
        log.info("Company created successfully - companyId={}", savedCompany.getId());
        return companyMapper.toDto(savedCompany);
    }

    public CompanyResponseDTO updateCompany(Long companyId, CompanyRequestDTO dto) {
        log.info("Updating company - companyId={}", companyId);
        Company company = repositoryHelper.findCompanyById(companyId);

        String normalizedName = Company.normalize(dto.name());
        companyRepository.findCompanyByNormalizedName(normalizedName).ifPresent(existing -> {
            if (!existing.getId().equals(companyId)) {
                throw new CompanyAlreadyExistsException(dto.name());
            }
        });

        companyMapper.updateEntityFromDto(dto, company);
        Company updatedCompany = companyRepository.save(company);
        log.info("Company updated successfully - companyId={}", companyId);
        return companyMapper.toDto(updatedCompany);
    }

}
