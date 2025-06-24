package com.example.jobfinder.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt xử lý thông điệp WebSocket được hỗ trợ bởi message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Cấu hình broker để gửi tin nhắn đến client
        // "/topic" cho các tin nhắn công khai (public messages) hoặc broadcast
        // "/user" cho các tin nhắn riêng tư (private messages) đến một người dùng cụ thể
        config.enableSimpleBroker("/topic", "/user"); // broker sẽ forward messages với prefix này

        // Cấu hình tiền tố cho các đích đến của ứng dụng
        // "/app" là prefix cho các mapping trong @MessageMapping (controller)
        // Ví dụ: gửi tin nhắn đến "/app/chat.sendMessage" sẽ được xử lý bởi @MessageMapping("/chat.sendMessage")
        config.setApplicationDestinationPrefixes("/app");

        // Cấu hình tiền tố cho đích đến dành riêng cho người dùng.
        // Điều này cho phép gửi tin nhắn riêng tư đến một người dùng cụ thể.
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint mà client sẽ kết nối đến để bắt đầu kết nối WebSocket
        // "/ws" là URL mà client sẽ sử dụng để kết nối.
        // .withSockJS(): Cho phép fallback sang SockJS nếu WebSocket không khả dụng (hữu ích cho trình duyệt cũ hơn)
        registry.addEndpoint("/ws").withSockJS();
    }
}