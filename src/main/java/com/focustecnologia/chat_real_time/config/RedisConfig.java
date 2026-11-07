package com.focustecnologia.chat_real_time.config;

import com.focustecnologia.chat_real_time.message.redis.RedisMessageSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {
    public static final String CHAT_CONVERSATIONS_CHANNEL_PREFIX = "chat:conversations:";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory,
                                                                       RedisMessageSubscriber messageSubscriber,
                                                                       PatternTopic conversationPatternTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageSubscriber, conversationPatternTopic);
        return container;
    }

    @Bean
    public PatternTopic conversationPatternTopic() {
        return new PatternTopic(CHAT_CONVERSATIONS_CHANNEL_PREFIX + "*");
    }
}
