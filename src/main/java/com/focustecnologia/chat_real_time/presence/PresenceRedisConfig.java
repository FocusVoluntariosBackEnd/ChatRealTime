package com.focustecnologia.chat_real_time.presence;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class PresenceRedisConfig {
    @Bean
    RedisMessageListenerContainer presenceRedisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PresenceRedisSubscriber presenceRedisSubscriber
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                presenceRedisSubscriber,
                new ChannelTopic(PresenceRedisPublisher.CHANNEL)
        );

        return container;
    }
}
