package com.example.jobfinder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

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

    public void sendApplicationStatusUpdateEmail(String toEmail, String jobTitle,
                                                 String newStatusDisplayName, String employerCompanyName,
                                                 String employerMessage) throws MessagingException { // <-- Thêm employerMessage
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        try {
            helper.setFrom("no-reply@jobfinder.com", "JobFinder Support");
        } catch (UnsupportedEncodingException e) {
            throw new MessagingException("Could not set sender email address", e);
        }

        helper.setTo(toEmail);
        helper.setSubject("Cập nhật trạng thái đơn ứng tuyển của bạn - " + jobTitle);

        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h1>Thông báo cập nhật đơn ứng tuyển của bạn</h1>")
                .append("<p>Chào bạn,</p>")
                .append("<p>Chúng tôi muốn thông báo rằng trạng thái đơn ứng tuyển của bạn cho vị trí <b>")
                .append(jobTitle).append("</b> tại <b>").append(employerCompanyName)
                .append("</b> đã được cập nhật.</p>")
                .append("<p>Trạng thái hiện tại của đơn ứng tuyển của bạn là: <b>")
                .append(newStatusDisplayName).append("</b></p>");

        // Thêm tin nhắn của nhà tuyển dụng nếu có
        if (employerMessage != null && !employerMessage.trim().isEmpty()) {
            emailContent.append("<p><b>Tin nhắn từ nhà tuyển dụng:</b></p>")
                    .append("<div style=\"background-color: #f0f0f0; padding: 10px; border-left: 5px solid #007bff; margin: 15px 0;\">")
                    .append("<p>").append(employerMessage).append("</p>")
                    .append("</div>");
        }

        emailContent.append("<p>Tùy thuộc vào trạng thái, bạn có thể nhận được thông tin chi tiết hơn về các bước tiếp theo trong email này hoặc trong hệ thống JobFinder.</p>")
                .append("<p>Vui lòng đăng nhập vào tài khoản JobFinder của bạn để xem chi tiết đơn ứng tuyển và bất kỳ tin nhắn mới nào từ nhà tuyển dụng.</p>")
                .append("<p><a href=\"[URL_TO_YOUR_APPLICATION_DETAIL_PAGE]\">Xem chi tiết đơn ứng tuyển của bạn</a></p>")
                .append("<p>Chúc bạn may mắn!</p>")
                .append("<p>Trân trọng,<br>Đội ngũ JobFinder</p>");

        helper.setText(emailContent.toString(), true);
        mailSender.send(message);
    }
}
