package com.focustecnologia.chat_real_time.realtime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.annotation.EnableRedisListeners;

@Configuration
@EnableRedisListeners
public class RedisPubSubConfig {

    @Bean
    RedisMessageListenerContainer realtimeRedisListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
