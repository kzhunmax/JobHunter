package com.github.kzhunmax.jobsearch.company.repository;

import com.github.kzhunmax.jobsearch.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsCompanyByNormalizedName(String normalizedName);
    Optional<Company> findCompanyByNormalizedName(String normalizedName);
}
