package dev.neohub.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                // Do NOT activate default typing — that's what causes PageImpl issues

                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

                RedisCacheConfiguration defaults = RedisCacheConfiguration
                                .defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer));

                return RedisCacheManager.builder(factory)
                                .cacheDefaults(defaults)
                                .withInitialCacheConfigurations(Map.of(
                                                "plugin", defaults.entryTtl(Duration.ofHours(1)),
                                                "plugins", defaults.entryTtl(Duration.ofMinutes(30)),
                                                "plugins_search", defaults.entryTtl(Duration.ofMinutes(10)),
                                                "colorschemes", defaults.entryTtl(Duration.ofHours(1))))
                                .build();
        }
}