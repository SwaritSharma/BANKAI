package com.digitalwallet.bnkai.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    public static final String GOLD_PRICE_CACHE = "goldPrice";
    public static final String GOLD_PRICE_HISTORY_CACHE = "goldPriceHistory";
    public static final String VENDORS_CACHE = "vendors";
    public static final String USER_DASHBOARD_CACHE = "userDashboard";
    public static final String USER_HOLDINGS_CACHE = "userHoldings";
    public static final String USER_TRANSACTIONS_CACHE = "userTransactions";
    public static final String USER_ADDRESSES_CACHE = "userAddresses";
    public static final String USER_PHYSICAL_GOLD_CACHE = "userPhysicalGold";
    public static final String USER_PAYMENTS_CACHE = "userPayments";
    public static final String VENDOR_DASHBOARD_CACHE = "vendorDashboard";
    public static final String VENDOR_BRANCHES_CACHE = "vendorBranches";
    public static final String VENDOR_TRANSACTIONS_CACHE = "vendorTransactions";

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Bean
    @ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisSerializer<Object> redisJsonSerializer() {
        return RedisSerializer.json();
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisJsonSerializer
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisJsonSerializer);
        template.setHashValueSerializer(redisJsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager cacheManager(
            RedisConnectionFactory redisConnectionFactory,
            RedisSerializer<Object> redisJsonSerializer
    ) {
        RedisCacheConfiguration defaultConfig = redisConfig(redisJsonSerializer, Duration.ofMinutes(10));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(GOLD_PRICE_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(GOLD_PRICE_HISTORY_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(15)))
                .withCacheConfiguration(VENDORS_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(5)))
                .withCacheConfiguration(USER_DASHBOARD_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(USER_HOLDINGS_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(USER_TRANSACTIONS_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(USER_ADDRESSES_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(5)))
                .withCacheConfiguration(USER_PHYSICAL_GOLD_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(USER_PAYMENTS_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(VENDOR_DASHBOARD_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(VENDOR_BRANCHES_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .withCacheConfiguration(VENDOR_TRANSACTIONS_CACHE, redisConfig(redisJsonSerializer, Duration.ofMinutes(2)))
                .transactionAware()
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager(
                GOLD_PRICE_CACHE,
                GOLD_PRICE_HISTORY_CACHE,
                VENDORS_CACHE,
                USER_DASHBOARD_CACHE,
                USER_HOLDINGS_CACHE,
                USER_TRANSACTIONS_CACHE,
                USER_ADDRESSES_CACHE,
                USER_PHYSICAL_GOLD_CACHE,
                USER_PAYMENTS_CACHE,
                VENDOR_DASHBOARD_CACHE,
                VENDOR_BRANCHES_CACHE,
                VENDOR_TRANSACTIONS_CACHE
        );
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache get failed for cache={} key={}; falling back to source. cause={} message={}",
                        cache.getName(), key, exception.getClass().getSimpleName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis cache put failed for cache={} key={}; response will still be returned. cause={} message={}",
                        cache.getName(), key, exception.getClass().getSimpleName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache evict failed for cache={} key={}; data source remains authoritative. cause={} message={}",
                        cache.getName(), key, exception.getClass().getSimpleName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis cache clear failed for cache={}; data source remains authoritative. cause={} message={}",
                        cache.getName(), exception.getClass().getSimpleName(), exception.getMessage());
            }
        };
    }

    private RedisCacheConfiguration redisConfig(
            RedisSerializer<Object> redisJsonSerializer,
            Duration ttl
    ) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisJsonSerializer));
    }
}
