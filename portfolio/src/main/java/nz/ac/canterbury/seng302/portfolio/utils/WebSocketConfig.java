package nz.ac.canterbury.seng302.portfolio.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
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
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Configures a path for websockets to "/ws" and enables SockJS for browsers that can't handle websockets
     * @param registry A registry that stores endpoints, error handlers and other options for STOMP over websockets
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("https://*.canterbury.ac.nz")
                .withSockJS()
                .setClientLibraryUrl( "https://cdn.jsdelivr.net/sockjs/1.4.0/sockjs.min.js" );
    }

}