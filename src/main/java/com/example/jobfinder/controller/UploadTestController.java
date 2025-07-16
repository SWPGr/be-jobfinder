package com.example.jobfinder.controller;

import com.example.jobfinder.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload-test")
@RequiredArgsConstructor
public class UploadTestController {

    private final CloudinaryService cloudinaryService;

    /**
     * Test upload PDF method 1 (bytes array)
     */
    @PostMapping("/pdf1")
    public ResponseEntity<Map<String, String>> uploadPDF1(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only PDF files are allowed");
                return ResponseEntity.badRequest().body(error);
            }
            
            String url = cloudinaryService.uploadPDF(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("method", "uploadPDF (bytes array)");
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test upload PDF method 2 (input stream)
     */
    @PostMapping("/pdf2")
    public ResponseEntity<Map<String, String>> uploadPDF2(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only PDF files are allowed");
                return ResponseEntity.badRequest().body(error);
            }
            
            String url = cloudinaryService.uploadPDFWithStream(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("method", "uploadPDFWithStream (input stream)");
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test upload với method uploadFile thông thường
     */
    @PostMapping("/file")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String url = cloudinaryService.uploadFile(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("method", "uploadFile (general)");
            response.put("filename", file.getOriginalFilename());
            response.put("contentType", file.getContentType());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Test upload PDF method 3 (with transformation)
     */
    @PostMapping("/pdf3")
    public ResponseEntity<Map<String, String>> uploadPDF3(@RequestParam("file") MultipartFile file) {
        try {
            if (!file.getContentType().equals("application/pdf")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only PDF files are allowed");
                return ResponseEntity.badRequest().body(error);
            }
            
            String url = cloudinaryService.uploadPDFWithTransformation(file);
            Map<String, String> response = new HashMap<>();
            response.put("url", url);
            response.put("method", "uploadPDFWithTransformation");
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
