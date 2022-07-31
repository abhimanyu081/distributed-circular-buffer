package dev.abhimanyu.distributedcircularbuffer.config;

import dev.abhimanyu.distributedcircularbuffer.service.RedisListCircularQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class RedisConfig {

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setEnableTransactionSupport(true);
        return template;
    }

    @Bean
    public RedisListCircularQueue<String> redisListCircularQueue(RedisTemplate<String, String> redisTemplate,
            CircularQueueConfig circularQueueLockConfig, RedisLockRegistry redisLockRegistry) {
        return new RedisListCircularQueue<String>(redisTemplate, circularQueueLockConfig, redisLockRegistry);

    }

    @Bean//(destroyMethod = "destroy")
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory,
            CircularQueueConfig circularQueueConfig) {
        return new RedisLockRegistry(redisConnectionFactory, circularQueueConfig.getLockName());
    }
}
