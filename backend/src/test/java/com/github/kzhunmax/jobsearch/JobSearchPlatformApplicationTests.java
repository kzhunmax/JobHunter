package com.github.kzhunmax.jobsearch;

import com.github.kzhunmax.jobsearch.util.AbstractPostgresTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JobSearchPlatformApplicationTests extends AbstractPostgresTest {

    @Test
    void contextLoads() {
        assertThat(POSTGRES.isRunning()).isTrue();
    }
}
