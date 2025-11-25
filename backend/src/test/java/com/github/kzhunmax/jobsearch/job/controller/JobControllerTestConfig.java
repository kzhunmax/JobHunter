package com.github.kzhunmax.jobsearch.job.controller;

import com.github.kzhunmax.jobsearch.security.JobSecurityService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class JobControllerTestConfig {

    @Bean
    JobSecurityService jobSecurityService() {
        return Mockito.mock(JobSecurityService.class);
    }
}
