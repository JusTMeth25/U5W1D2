package com.example.u5w1d2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    private final AuthChannelInterceptor authInterceptor;

    public WebSocketConfig(AuthChannelInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // /ws: unico indirizzo del canale. setAllowedOrigins sempre, altrimenti handshake 403.
        // withSockJS aggiunge il fallback HTTP: il client si connette a http://.../ws
        // e usa WebSocket se disponibile, altrimenti xhr-streaming o xhr-polling.
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigin).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /topic feed pubblico di ogni bacheca, /queue destinazioni personali.
        registry.enableSimpleBroker("/topic", "/queue");
        // prefisso virtuale che rende privata una destinazione: usato per le notifiche.
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
