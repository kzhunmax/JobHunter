package com.github.kzhunmax.jobsearch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@EnableElasticsearchRepositories(basePackages = "com.github.kzhunmax.jobsearch.repository.es")
@Configuration
public class ElasticsearchConfig { }