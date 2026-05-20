package com.digitalwallet.bnkai.config;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheConfigTest {

    @Test
    void cacheErrorsAreSwallowedSoApisCanFallbackToRepositories() {
        RedisCacheConfig config = new RedisCacheConfig();
        CacheErrorHandler handler = config.errorHandler();
        Cache cache = mock(Cache.class);
        when(cache.getName()).thenReturn("userDashboard");

        assertThatCode(() -> handler.handleCacheGetError(new RuntimeException("redis down"), cache, 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCachePutError(new RuntimeException("redis down"), cache, 1, "value"))
                .doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheEvictError(new RuntimeException("redis down"), cache, 1))
                .doesNotThrowAnyException();
        assertThatCode(() -> handler.handleCacheClearError(new RuntimeException("redis down"), cache))
                .doesNotThrowAnyException();
    }

    @Test
    void redisJsonSerializer_goldPriceDto_roundTripsSafely() {
        RedisCacheConfig config = new RedisCacheConfig();
        RedisSerializer<Object> serializer = config.redisJsonSerializer();

        GoldPriceDTO dto = new GoldPriceDTO();
        dto.setPrice(new BigDecimal("7150.00"));
        dto.setChange24h(new BigDecimal("45.50"));
        dto.setChangePct(new BigDecimal("0.64"));

        Object restored = serializer.deserialize(serializer.serialize(dto));

        assertThat(restored).isInstanceOf(GoldPriceDTO.class);
        GoldPriceDTO restoredDto = (GoldPriceDTO) restored;
        assertThat(restoredDto.getPrice()).isEqualByComparingTo("7150.00");
        assertThat(restoredDto.getChange24h()).isEqualByComparingTo("45.50");
        assertThat(restoredDto.getChangePct()).isEqualByComparingTo("0.64");
    }
}
