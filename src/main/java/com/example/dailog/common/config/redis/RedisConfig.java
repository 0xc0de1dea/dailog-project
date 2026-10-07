package com.example.dailog.common.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public JacksonJsonRedisSerializer<RedisChatMessage> chatRedisSerializer() {

        return new JacksonJsonRedisSerializer<>(
                RedisChatMessage.class
        );
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            JacksonJsonRedisSerializer<RedisChatMessage> chatRedisSerializer
    ) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(
                connectionFactory
        );

        template.setKeySerializer(
                new StringRedisSerializer()
        );

        /*
         * 채팅 메시지는 Redis에 JSON으로 저장/전달
         */
        template.setValueSerializer(
                chatRedisSerializer
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashValueSerializer(
                chatRedisSerializer
        );

        template.afterPropertiesSet();

        return template;
    }
}