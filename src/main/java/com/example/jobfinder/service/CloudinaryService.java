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
            String contentType = file.getContentType();
            String resourceType = determineResourceType(contentType);
            String originalFilename = file.getOriginalFilename();

            String publicId;
            if ("raw".equals(resourceType) && contentType != null && contentType.equals("application/pdf")) {
                publicId = "resumes/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis() + ".pdf";
            } else {
                publicId = "resumes/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", resourceType,
                    "folder", "resumes",
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload"
            );
            
            if ("raw".equals(resourceType)) {
                uploadParams.put("use_filename", false);
                uploadParams.put("unique_filename", false);
                uploadParams.put("overwrite", true);
                uploadParams.put("invalidate", true);
            }

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            if ("raw".equals(resourceType)) {
                if (contentType != null && contentType.equals("application/pdf")) {
                    if (!url.contains("?")) {
                        url += "?";
                    } else {
                        url += "&";
                    }
                    url += "content_type=application/pdf";
                } else {
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

    public String uploadPDF(MultipartFile file) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            
            String publicId = "pdfs/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis() + ".pdf";

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload",
                    "use_filename", false,
                    "unique_filename", false,
                    "overwrite", true,
                    "invalidate", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            if (!url.contains("?")) {
                url += "?";
            } else {
                url += "&";
            }
            url += "content_type=application/pdf";
            
            return url;
            
        } catch (IOException e) {
            throw new RuntimeException("PDF upload failed: " + e.getMessage(), e);
        }
    }

    public String uploadPDFWithStream(MultipartFile file) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            
            String publicId = "pdfs/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis() + ".pdf";

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload",
                    "use_filename", false,
                    "unique_filename", false,
                    "overwrite", true
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getInputStream(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            return url;
            
        } catch (IOException e) {
            throw new RuntimeException("PDF upload with stream failed: " + e.getMessage(), e);
        }
    }

    public String uploadPDFWithTransformation(MultipartFile file) throws IOException {
        try {
            String originalFilename = file.getOriginalFilename();
            
            String publicId = "documents/" + removeExtension(originalFilename) + "_" + System.currentTimeMillis();

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                    "resource_type", "raw",
                    "public_id", publicId,
                    "access_mode", "public",
                    "type", "upload",
                    "use_filename", false,
                    "unique_filename", true,
                    "overwrite", false,
                    "flags", "immutable",
                    "raw_convert", "aspose"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getInputStream(), uploadParams);

            String url = uploadResult.get("secure_url").toString();
            
            url = url.replace("/upload/", "/upload/f_auto/");
            
            return url;
            
        } catch (IOException e) {
            throw new RuntimeException("PDF upload with transformation failed: " + e.getMessage(), e);
        }
    }
}

