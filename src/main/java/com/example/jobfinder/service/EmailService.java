package com.example.jobfinder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.UrlResource; // Import UrlResource
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException; // Thêm import cho MalformedURLException

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
public class EmailService {
    JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("verify your email");
        String verificationLink = "http://be-jobfinder-o830.onrender.com/api/auth/verify?token=" + token;
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
        String resetLink = "https://fe-jobfinder.vercel.app/api/auth/reset-password?token=" + token;
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
                                                 String employerMessage) throws MessagingException {

        String headerImageUrl = "https://i.imgur.com/5smF8EN.png";
        String footerImageUrl = "https://i.imgur.com/aYsQPuN.png";

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
        emailContent.append("<!DOCTYPE html>")
                .append("<html lang=\"vi\">")
                .append("<head>")
                .append("  <meta charset=\"UTF-8\">")
                .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("  <title>Cập nhật trạng thái đơn ứng tuyển của bạn</title>")
                .append("  <style>")
                .append("    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f4f4; }")
                .append("    .container { max-width: 600px; margin: 20px auto; padding: 20px; background-color: #ffffff; border: 1px solid #e0e0e0; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }")
                .append("    h1 { color: #007bff; text-align: center; margin-bottom: 20px; font-size: 24px; }")
                .append("    h2 { color: #0056b3; font-size: 20px; margin-top: 25px; border-bottom: 1px solid #eee; padding-bottom: 5px; }")
                .append("    p { margin-bottom: 10px; }")
                .append("    .highlight { font-weight: bold; color: #007bff; }") // Thêm class highlight
                .append("    .status-badge { display: inline-block; padding: 8px 15px; border-radius: 20px; font-weight: bold; color: #ffffff; text-transform: uppercase; margin-top: 10px; }")
                .append("    .status-accepted { background-color: #28a745; }") // Xanh lá cây
                .append("    .status-rejected { background-color: #dc3545; }") // Đỏ
                .append("    .status-interview { background-color: #ffc107; color: #333; }") // Vàng
                .append("    .status-pending { background-color: #6c757d; }") // Xám
                .append("    .message-box { background-color: #f0f8ff; padding: 15px; border-left: 5px solid #007bff; margin: 20px 0; border-radius: 4px; }")
                .append("    .footer { text-align: center; margin-top: 30px; font-size: 0.85em; color: #777; border-top: 1px solid #eee; padding-top: 20px; }")
                .append("    .footer img, .header img { max-width: 100%; height: auto; display: block; margin: 0 auto; }")
                .append("    .header { text-align: center; margin-bottom: 20px; }")
                .append("    .button { display: inline-block; background-color: #007bff; color: #ffffff; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 15px; }")
                .append("    .social-links a { margin: 0 5px; text-decoration: none; color: #007bff; }")
                .append("  </style>")
                .append("</head>")
                .append("<body>")
                .append("<div class=\"container\">");

// Thêm ảnh header
        emailContent.append("<div class=\"header\">")
                .append("<img src=\"cid:headerImage\" alt=\"Company Banner\">")
                .append("</div>");

        emailContent.append("<h1>Cập Nhật Đơn Ứng Tuyển Của Bạn Trên JobFinder</h1>")
                .append("<p>Chào bạn") // Có thể chèn tên người dùng nếu bạn có dữ liệu
                .append("<p>Chúng tôi xin trân trọng thông báo về cập nhật mới nhất cho đơn ứng tuyển của bạn trên hệ thống JobFinder.</p>")
                .append("<p>Đơn ứng tuyển của bạn cho vị trí <b class=\"highlight\">").append(jobTitle).append("</b> tại công ty <b class=\"highlight\">").append(employerCompanyName).append("</b> đã được cập nhật trạng thái.</p>")
                .append("<p>Trạng thái hiện tại của đơn ứng tuyển của bạn là:</p>")
                // Thêm badge trạng thái động
                .append("<p><span class=\"status-badge status-")
                .append(newStatusDisplayName.toLowerCase().replace(" ", "-")) // Ví dụ: "Đã duyệt" -> "da-duyet" để khớp với CSS class
                .append("\">").append(newStatusDisplayName).append("</span></p>");

// Thêm tin nhắn từ nhà tuyển dụng
        if (employerMessage != null && !employerMessage.trim().isEmpty()) {
            emailContent.append("<h2>Tin Nhắn Từ Nhà Tuyển Dụng</h2>")
                    .append("<div class=\"message-box\">")
                    .append("<p>").append(employerMessage).append("</p>")
                    .append("</div>");
        }

// Thêm các bước tiếp theo dựa trên trạng thái (ví dụ)
        emailContent.append("<h2>Các Bước Tiếp Theo</h2>");

        emailContent.append("<p>Để xem chi tiết cập nhật và các bước tiếp theo (nếu có), vui lòng đăng nhập vào tài khoản JobFinder của bạn:</p>")
                .append("<p><a href=\"https://fe-jobfinder.vercel.app/login\" class=\"button\">Đăng Nhập JobFinder</a></p>");


        emailContent.append("<h2>Bạn có câu hỏi?</h2>")
                .append("<p>Nếu bạn có bất kỳ câu hỏi hoặc cần hỗ trợ thêm, đừng ngần ngại liên hệ với chúng tôi qua email <a href=\"mailto:letritrung2605@gmail.com\">support@jobfinder.com</a> hoặc truy cập trang <a href=\"https://fe-jobfinder.vercel.app/help\">Trung tâm trợ giúp</a> của chúng tôi.</p>");


        emailContent.append("<p>Chúc bạn may mắn trên con đường tìm kiếm sự nghiệp!</p>")
                .append("<p>Trân trọng,<br>Đội ngũ JobFinder</p>");

        emailContent.append("<div class=\"footer\">")
                .append("<img src=\"cid:footerImage\" alt=\"JobFinder Info\">")
                .append("<p>&copy; 2025 JobFinder. All rights reserved.</p>")
                .append("<p>Địa chỉ: 45 Nguyễn Quang Bích</p>")
                .append("<p>Kết nối với chúng tôi:</p>")
                .append("<p class=\"social-links\">")
                .append("  <a href=\"[Link_Facebook]\">Facebook</a> | ")
                .append("  <a href=\"[Link_LinkedIn]\">LinkedIn</a> | ")
                .append("  <a href=\"[Link_Twitter]\">Twitter</a>")
                .append("</p>")
                .append("<p><a href=\"[Link_Chính_Sách_Bảo_Mật]\">Chính sách bảo mật</a> | <a href=\"[Link_Điều_Khoản_Dịch_Vụ]\">Điều khoản dịch vụ</a></p>")
                .append("</div>");

        emailContent.append("</div></body></html>");

        helper.setText(emailContent.toString(), true);

        try {
            // Đính kèm ảnh header
            // Thay thế URLDataSource bằng UrlResource
            UrlResource headerImageSource = new UrlResource(headerImageUrl);
            // Phương thức addInline thứ 3 là contentType, không phải Resource
            helper.addInline("headerImage", headerImageSource, "image/png");

            // Đính kèm ảnh footer
            // Thay thế URLDataSource bằng UrlResource
            UrlResource footerImageSource = new UrlResource(footerImageUrl);
            // Phương thức addInline thứ 3 là contentType, không phải Resource
            helper.addInline("footerImage", footerImageSource, "image/png");

        } catch (MalformedURLException e) {
            // Xử lý lỗi URL không hợp lệ
            throw new MessagingException("Invalid URL for image: " + e.getMessage(), e);
        } catch (IOException e) {
            // Xử lý lỗi khi đọc ảnh
            throw new MessagingException("Could not read image data: " + e.getMessage(), e);
        } catch (Exception e) {
            // Xử lý các lỗi khác
            throw new MessagingException("Could not load or attach image(s) to email: " + e.getMessage(), e);
        }

        mailSender.send(message);
    }
}