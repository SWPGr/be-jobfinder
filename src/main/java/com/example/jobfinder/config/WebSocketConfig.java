package com.example.jobfinder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app"); // Tiền tố cho các đích đến mà client gửi tin nhắn
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // Endpoint WebSocket của bạn
                // Đảm bảo rằng origin của frontend được cho phép ở đây.
                // THAY THẾ "*" bằng "http://localhost:3000" trong môi trường production nếu bạn chỉ có một frontend.
                .setAllowedOriginPatterns("http://localhost:3000") // <-- THAY ĐỔI TẠI ĐÂY
                // Nếu bạn có nhiều origin, bạn có thể thêm chúng vào đây, ví dụ:
                // .setAllowedOriginPatterns("http://localhost:3000", "https://yourproductionfrontend.com")
                .withSockJS(); // Bật hỗ trợ SockJS
    }

}