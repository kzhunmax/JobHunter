package com.github.kzhunmax.jobsearch.util;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class AbstractIntegrationTest {

    @ServiceConnection
    public static final RedisContainer REDIS = new RedisContainer(DockerImageName.parse("redis:8.2.2-alpine"))
            .withExposedPorts(6379);

    @ServiceConnection
    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17.6-alpine");

    @ServiceConnection
    public static final ElasticsearchContainer ELASTICSEARCH = new ElasticsearchContainer(DockerImageName.parse("elasticsearch:8.19.5"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Redis Properties
        registry.add("spring.redis.host", REDIS::getHost);
        registry.add("spring.redis.port", () -> REDIS.getMappedPort(6379).toString());

        // Elasticsearch Properties
        registry.add("spring.elasticsearch.uris[0]", ELASTICSEARCH::getHttpHostAddress);
    }

    static {
        REDIS.start();
        POSTGRES.start();
        ELASTICSEARCH.start();
    }

}
