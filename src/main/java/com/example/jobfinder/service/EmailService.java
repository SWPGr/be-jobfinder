package com.example.jobfinder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("verify your email");
        String verificationLink = "http://localhost:8080/api/auth/verify?token=" + token;
        helper.setText(
                "<h1>Please Verify Your Email</h1>" +
                        "<p>Click the link below to verify your email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verify Email</a>",
                true
        );
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Reset your password");
        String resetLink = "http://localhost:3030/api/auth/reset-password?token=" + token;
        helper.setText(
                "<h1>Reset Your Password</h1>" +
                        "<p>Click the link below to reset your password:</p>" +
                        "<a href=\"" + resetLink + "\">Reset Password</a>" +
                        "<p>This link will expire in 1 hour.</p>",
                true
        );

        mailSender.send(message);
    }

    public void sendAccountBlockedEmail(String to, String reason) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Thông báo: Tài khoản JobFinder của bạn đã bị khóa"); // Chủ đề rõ ràng

        String emailContent = "<h1>Thông báo quan trọng về tài khoản JobFinder của bạn</h1>" +
                "<p>Chào bạn,</p>" +
                "<p>Chúng tôi rất tiếc phải thông báo rằng tài khoản JobFinder của bạn (" + to + ") đã bị khóa.</p>" +
                "<p>Điều này xảy ra do tài khoản của bạn đã vi phạm các quy tắc cộng đồng hoặc điều khoản dịch vụ của chúng tôi.</p>";

        if (reason != null && !reason.trim().isEmpty()) {
            emailContent += "<p><b>Lý do cụ thể:</b> " + reason + "</p>";
        } else {
            emailContent += "<p>Để biết thêm thông tin chi tiết về lý do và các bước tiếp theo, vui lòng liên hệ với bộ phận hỗ trợ của chúng tôi.</p>";
        }

        emailContent += "<p>Chúng tôi cam kết duy trì một môi trường an toàn và công bằng cho tất cả người dùng.</p>" +
                "<p>Nếu bạn tin rằng đây là một sự nhầm lẫn hoặc muốn kháng nghị quyết định này, vui lòng liên hệ với chúng tôi qua email hỗ trợ [Địa chỉ Email Hỗ Trợ của bạn] hoặc truy cập trang trợ giúp của chúng tôi [Link Trang Trợ Giúp của bạn].</p>" +
                "<p>Trân trọng,<br>Đội ngũ JobFinder</p>";

        helper.setText(emailContent, true);
        mailSender.send(message);
    }
}
