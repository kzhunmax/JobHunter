package com.github.kzhunmax.jobsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableCaching
public class JobSearchPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobSearchPlatformApplication.class, args);
	}

}
