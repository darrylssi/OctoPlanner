package nz.ac.canterbury.seng302.portfolio.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Designates where websockets can be sent to
     * @param config A registry that stores options configuring messages
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long [] {9000, 20000})
                .setTaskScheduler(heartBeatScheduler());
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Configures a path for websockets to "/ws" and enables SockJS for browsers that can't handle websockets
     * @param registry A registry that stores endpoints, error handlers and other options for STOMP over websockets
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String sitePattern = "https://*.canterbury.ac.nz";
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(sitePattern);
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(sitePattern)
                .withSockJS();
        registry.addEndpoint("/events")
                .setAllowedOriginPatterns(sitePattern);
        registry.addEndpoint("/events")
                .setAllowedOriginPatterns(sitePattern)
                .withSockJS();
    }

    /**
     * Schedules the heartbeat for websockets in case the client disconnects while working on something
     * @return A scheduler object
     */
    @Bean
    public TaskScheduler heartBeatScheduler() {
        return new ThreadPoolTaskScheduler();
    }
}