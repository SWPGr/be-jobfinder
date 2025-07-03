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
            String resourceType = "auto";

            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", resourceType,
                            "folder", "resumes",
                            "public_id", "resumes/" + removeExtension(file.getOriginalFilename()),
                            "access_mode", "public",
                            "type", "upload"
                    )
            );

            String url = uploadResult.get("secure_url").toString();
//            if ("raw".equals(resourceType)) {
//                url = url.replace("/upload/", "/upload/fl_attachment:false/");
//            }

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
        } else if (contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            return "raw";  // pdf, doc, docx
        }

        return "raw";  // fallback
    }

}

