// src/main/java/com/example/jobfinder/config/WebSocketAuthInterceptor.java
package com.example.jobfinder.config;

import com.example.jobfinder.config.JwtUtil; // Dịch vụ xử lý JWT
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Để ghi log
import org.springframework.messaging.Message; // Đại diện cho tin nhắn
import org.springframework.messaging.MessageChannel; // Kênh tin nhắn
import org.springframework.messaging.simp.stomp.StompCommand; // Các lệnh STOMP (CONNECT, SUBSCRIBE, etc.)
import org.springframework.messaging.simp.stomp.StompHeaderAccessor; // Dùng để truy cập các header của STOMP frame
import org.springframework.messaging.support.ChannelInterceptor; // Giao diện của Interceptor kênh
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Đối tượng xác thực của Spring Security
import org.springframework.security.core.context.SecurityContextHolder; // Để quản lý SecurityContext
import org.springframework.security.core.userdetails.UserDetails; // Thông tin chi tiết người dùng
import org.springframework.security.core.userdetails.UserDetailsService; // Dịch vụ để tải UserDetails
import org.springframework.stereotype.Component; // Đánh dấu là Spring Component

import java.util.List;
import java.util.Objects; // Tiện ích để kiểm tra null

@Component // Đánh dấu là một Spring Component để được Spring quản lý
@RequiredArgsConstructor // Lombok: Tự động tạo constructor với các trường final
@Slf4j // Lombok: Tự động tạo logger
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtService; // Inject JwtService để xác thực token
    private final UserDetailsService userDetailsService; // Inject UserDetailsService để tải thông tin người dùng

    /**
     * Phương thức này được gọi TRƯỚC KHI một tin nhắn được gửi qua kênh.
     * Chúng ta sử dụng nó để chặn và xác thực các lệnh `CONNECT` (khi client kết nối ban đầu)
     * và `SUBSCRIBE` (khi client đăng ký một kênh) của STOMP.
     * @param message Tin nhắn STOMP đang được xử lý.
     * @param channel Kênh tin nhắn mà tin nhắn đang được gửi qua.
     * @return Tin nhắn đã được xử lý (hoặc bị chặn/sửa đổi nếu cần).
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Lấy StompHeaderAccessor để dễ dàng truy cập các header của STOMP frame.
        // `wrap(message)` giúp chuyển đổi Message thành một StompHeaderAccessor.
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Chúng ta chỉ quan tâm đến việc xác thực khi client gửi lệnh CONNECT hoặc SUBSCRIBE.
        // Lệnh CONNECT là khi WebSocket handshake diễn ra và client thiết lập phiên STOMP.
        // Lệnh SUBSCRIBE là khi client muốn đăng ký nhận tin nhắn từ một kênh.
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SUBSCRIBE.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand()))  {
            // Lấy header "Authorization" từ native headers.
            // "native headers" là các HTTP header được gửi trong quá trình WebSocket handshake
            // (khi lệnh CONNECT được gửi).
            List<String> authorization = accessor.getNativeHeader("Authorization");
            String token = null;

            if (authorization != null && !authorization.isEmpty()) {
                String authHeader = authorization.get(0);
                // Kiểm tra xem header có bắt đầu bằng "Bearer " không.
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7); // Trích xuất chuỗi token sau "Bearer "
                    log.debug("Extracted JWT token for WebSocket: {}", token); // Ghi log debug token đã trích xuất
                }
            }

            if (token != null) {
                try {
                    // Trích xuất username (thường là email) từ token JWT.
                    String userEmail = jwtService.extractUsername(token);

                    // Nếu userEmail hợp lệ và hiện tại chưa có thông tin xác thực nào trong SecurityContext.
                    // `SecurityContextHolder.getContext().getAuthentication() == null`
                    // đảm bảo rằng chúng ta chỉ xác thực một lần cho mỗi phiên WebSocket STOMP
                    // và tránh ghi đè lên thông tin xác thực đã có (nếu có).
                    if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        // Tải thông tin chi tiết người dùng (`UserDetails`) từ email.
                        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                        // Kiểm tra xem token có hợp lệ (chữ ký đúng) và chưa hết hạn không.
                        if (jwtService.isTokenValid(token, userDetails)) {
                            // Tạo đối tượng xác thực cho Spring Security.
                            // `UsernamePasswordAuthenticationToken` được dùng để đại diện cho người dùng đã xác thực.
                            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                    userDetails, // Principal: đối tượng đại diện cho người dùng đã xác thực (UserDetails).
                                    null,        // Credentials: Mật khẩu, đặt là `null` vì đã xác thực bằng token.
                                    userDetails.getAuthorities() // Các quyền/vai trò của người dùng.
                            );
                            // Đặt đối tượng Authentication vào `StompHeaderAccessor`.
                            // Đây là bước RẤT QUAN TRỌNG: Nó thiết lập ngữ cảnh bảo mật cho phiên STOMP.
                            // Các lớp khác của Spring Security (như `WebSocketSecurityConfig` và `@PreAuthorize`)
                            // sẽ sử dụng thông tin này để kiểm tra quyền truy cập.
                            accessor.setUser(authToken);
                            log.info("WebSocket authenticated for user: {}", userEmail); // Ghi log xác thực thành công
                        } else {
                            log.warn("Invalid JWT token for WebSocket: {}", token); // Cảnh báo token không hợp lệ
                            // Tùy chọn: ném ngoại lệ để từ chối kết nối/message nếu token không hợp lệ
                            // throw new MessagingException("Invalid JWT token.");
                        }
                    }
                } catch (Exception e) {
                    log.error("Error authenticating WebSocket connection: {}", e.getMessage()); // Ghi log lỗi xác thực
                    // Tùy chọn: ném ngoại lệ để đóng kết nối không xác thực
                    // throw new MessagingException("Authentication failed for WebSocket.", e);
                }
            } else {
                log.warn("No JWT token found in WebSocket CONNECT/SUBSCRIBE header."); // Cảnh báo không tìm thấy token
                // Tùy chọn: Từ chối kết nối nếu không có token nào được cung cấp và yêu cầu xác thực
                // throw new MessagingException("Authentication required for WebSocket.");
            }
        }
        return message; // Trả về tin nhắn để nó tiếp tục được xử lý bởi các interceptor và handler khác
    }
}