package com.app.incidentManagement.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.app.incidentManagement.entity.User;
import com.app.incidentManagement.repository.UserRepository;
import com.app.incidentManagement.security.JwtService;


@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final Logger log =
            LoggerFactory.getLogger(WebSocketAuthInterceptor.class);

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            UserRepository userRepository) {

        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null &&
                    authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                try {

                    String email =
                            jwtService.extractEmail(token);

                    User user =
                            userRepository
                                    .findByEmail(email)
                                    .orElse(null);

                    if (user != null && user.isActive()) {

                        SimpleGrantedAuthority authority =
                                new SimpleGrantedAuthority(
                                        "ROLE_" +
                                        user.getRole().name()
                                );

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        List.of(authority)
                                );

                        accessor.setUser(authentication);

                    }

                } catch (Exception e) {

                	 log.warn(
                	            "WebSocket authentication failed: {}",
                	            e.getMessage()
                	    );
                }
            }
        }

        return message;
    }
}