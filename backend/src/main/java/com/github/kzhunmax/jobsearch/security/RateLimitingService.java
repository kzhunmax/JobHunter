package com.github.kzhunmax.jobsearch.security;

import com.github.kzhunmax.jobsearch.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.function.Supplier;

import static com.github.kzhunmax.jobsearch.constants.LoggingConstants.REQUEST_ID_MDC_KEY;

@RequiredArgsConstructor
@Service
@Slf4j
public class RateLimitingService {

    private final ProxyManager<String> proxyManager;

    public Bucket resolveBucket(String key, PricingPlan plan) {
        Supplier<BucketConfiguration> configurationSupplier = () -> {
            Bandwidth limit = Bandwidth.builder()
                    .capacity(plan.getCapacity())
                    .refillGreedy(plan.getCapacity(), plan.getDuration())
                    .build();

            return BucketConfiguration.builder()
                    .addLimit(limit)
                    .build();
        };
        return proxyManager.getProxy(key, configurationSupplier);
    }

    public void consumeToken(String key, PricingPlan plan, String keyTypeForLog) {
        String requestId = MDC.get(REQUEST_ID_MDC_KEY);

        String bucketKey = key + ":" + plan.name();
        Bucket bucket = resolveBucket(bucketKey, plan);

        if (!bucket.tryConsume(1)) {
            log.warn("Request [{}]: Rate limit exceeded for {} ({}) on plan={}",
                    requestId, keyTypeForLog, key, plan.name());
            throw new RateLimitExceededException();
        }
    }
}
