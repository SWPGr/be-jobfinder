package com.example.jobfinder.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    public String uploadFile(MultipartFile file) throws IOException {
        try {
            // Lấy đuôi file để xác định loại
            String contentType = file.getContentType();
            String resourceType = determineResourceType(contentType);
            String originalFilename = file.getOriginalFilename();

            // Tạo unique filename để tránh conflict
            String publicId = "resumes/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis();

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", "resumes",
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload"
            );
            
            // Đặc biệt cho PDF/documents
            if ("raw".equals(resourceType)) {
                uploadParams.put("use_filename", true);
                uploadParams.put("unique_filename", false);
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            // Đặc biệt xử lý cho PDF và documents để download đúng
            if ("raw".equals(resourceType)) {
                if (contentType != null && contentType.equals("application/pdf")) {
                    // Để PDF có thể view trong browser, không thêm fl_attachment
                    // Nếu muốn force download, uncomment dòng dưới:
                    // url = url.replace("/upload/", "/upload/fl_attachment/");
                } else {
                    // Các file documents khác thì force download
                    url = url.replace("/upload/", "/upload/fl_attachment/");
                }
            }

            return url;
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String removeExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    // Hàm phụ để xác định loại file
    private String determineResourceType(String contentType) {
        if (contentType == null) return "raw";

        if (contentType.startsWith("image/")) {
            return "image";  // jpg, png, etc.
        } else if (contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                contentType.equals("text/plain") ||
                contentType.equals("application/vnd.ms-excel") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
            return "raw";  // pdf, doc, docx
        }

        return "raw";  // fallback
    }

    /**
     * Upload PDF với settings tối ưu để tránh file bị corrupt
     */
    public String uploadPDF(MultipartFile file) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            
            // Tạo unique filename
            String publicId = "pdfs/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis();

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", "raw",
                    "folder", "pdfs", 
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload",
                    "use_filename", true,
                    "unique_filename", false,
                    "format", "pdf"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            // Không thêm fl_attachment để PDF có thể view inline trong browser
            return url;
            
        } catch (IOException e) {
            throw new RuntimeException("PDF upload failed: " + e.getMessage(), e);
        }
    }
}

