// src/main/java/com/example/jobfinder/service/GeminiService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.gemini.GeminiIntentRequest; // Có thể đổi tên DTO sau nếu cần, nhưng giữ nguyên ở đây
import com.example.jobfinder.dto.gemini.GeminiIntentResponse; // Có thể đổi tên DTO sau nếu cần, nhưng giữ nguyên ở đây
import com.example.jobfinder.exception.AppException;
import com.example.jobfinder.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiService { // GIỮ NGUYÊN TÊN CLASS LÀ GeminiService

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Cập nhật các biến @Value để trỏ đến cấu hình OpenAI
    @Value("${OPENAI_API_KEY}")
    private String openaiApiKey;

    @Value("${OPENAI_MODEL_NAME}")
    private String modelName; // Sẽ là tên model của OpenAI (ví dụ: gpt-4o-mini)

    @Value("${OPENAI_BASE_URL}") // Thêm thuộc tính mới cho base URL của OpenAI
    private String openaiBaseUrl;

    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 giây

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // --- Phương thức getGeminiResponse (Nội dung đã đổi thành OpenAI) ---
    public String getGeminiResponse(String prompt) {
        log.info("Sending text prompt to OpenAI (via GeminiService): {}", prompt);
        // Endpoint của OpenAI cho Chat Completions
        String url = openaiBaseUrl + "/chat/completions";

        try {
            // Cấu trúc request body của OpenAI cho Chat Completions API
            // {
            //   "model": "gpt-4o-mini",
            //   "messages": [
            //     { "role": "user", "content": "..." }
            //   ]
            // }
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));

            Map<String, Object> requestBodyMap = Map.of(
                    "model", modelName,
                    "messages", messages
            );

            String jsonRequestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openaiApiKey); // Thêm Authorization header cho OpenAI
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            // Path để lấy nội dung từ response của OpenAI
            String jsonPath = "choices[0].message.content";
            return executeGeminiRequest(url, request, String.class, jsonPath); // Giữ nguyên tên phương thức executeGeminiRequest

        } catch (IOException e) {
            log.error("Error creating OpenAI request body (via GeminiService): {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // --- Phương thức analyzeIntent (Nội dung đã đổi thành OpenAI) ---
    public GeminiIntentResponse.IntentAnalysisResult analyzeIntent(String userQuery, String systemInstruction) throws IOException {
        log.info("Analyzing intent for query (via GeminiService/OpenAI): {}", userQuery);

        String url = openaiBaseUrl + "/chat/completions";

        // Cấu trúc messages cho OpenAI, bao gồm system instruction
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemInstruction)); // Vai trò system
        messages.add(Map.of("role", "user", "content", userQuery));

        Map<String, Object> requestBodyMap = Map.of(
                "model", modelName,
                "messages", messages,
                "response_format", Map.of("type", "json_object") // Yêu cầu output là JSON object
        );

        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBodyMap);
        } catch (IOException e) {
            log.error("Error serializing OpenAI Intent Request (via GeminiService): {}", e.getMessage(), e);
            throw new IOException("Lỗi tạo yêu cầu phân tích ý định OpenAI: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        // Path để lấy nội dung JSON string từ response của OpenAI
        String rawResponseContent = executeGeminiRequest(url, request, String.class, "choices[0].message.content");

        try {
            // rawResponseContent bây giờ là chuỗi JSON do OpenAI trả về
            // Bạn cần đảm bảo cấu trúc JSON này khớp với GeminiIntentResponse.IntentAnalysisResult
            // Nếu không khớp, bạn cần định nghĩa DTO mới và điều chỉnh logic parsing ở đây.
            // Loại bỏ các ký tự bọc (```json ... ```) nếu có
            String cleanJsonString = rawResponseContent.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJsonString, GeminiIntentResponse.IntentAnalysisResult.class);
        } catch (IOException e) {
            log.error("Failed to parse OpenAI intent analysis response (via GeminiService): {}", rawResponseContent, e);
            throw new IOException("Lỗi khi phân tích ý định từ OpenAI.", e);
        }
    }

    // --- Phương thức generateResponseWithContext (Nội dung đã đổi thành OpenAI) ---
    public String generateResponseWithContext(String userMessage, String context) {
        log.info("Generating response with context for user message: '{}', context: '{}' (via GeminiService/OpenAI)", userMessage, context);

        String url = openaiBaseUrl + "/chat/completions";

        List<Map<String, String>> messages = new ArrayList<>();

        if (context != null && !context.trim().isEmpty()) {
            messages.add(Map.of("role", "system", "content", "Thông tin dữ liệu: " + context));
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBodyMap = Map.of(
                "model", modelName,
                "messages", messages
        );

        String jsonRequestBody;
        try {
            jsonRequestBody = objectMapper.writeValueAsString(requestBodyMap);
        } catch (IOException e) {
            log.error("Error serializing OpenAI request for context generation (via GeminiService): {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        String jsonPath = "choices[0].message.content";
        return executeGeminiRequest(url, request, String.class, jsonPath);
    }


    // --- Phương thức chung để xử lý gửi request và parse response (Nội dung đã đổi thành OpenAI) ---
    // Giữ nguyên tên phương thức là executeGeminiRequest
    private <T> T executeGeminiRequest(String url, HttpEntity<?> request, Class<T> responseType, String jsonPath) {
        for (int attempt = 1; true; attempt++) {
            try {
                log.debug("Executing OpenAI request (via GeminiService) to {} (Attempt {}/{})", url, attempt, MAX_RETRIES);
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
                String responseBody = responseEntity.getBody();

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    log.debug("OpenAI raw response (Attempt {}): {}", attempt, responseBody);
                    if (jsonPath != null) {
                        if (responseBody == null || responseBody.isEmpty()) {
                            log.warn("OpenAI API returned empty response body for jsonPath request (via GeminiService).");
                            throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR); // Giữ ErrorCode cũ hoặc đổi mới
                        }
                        try {
                            JsonNode rootNode = objectMapper.readTree(responseBody);
                            String extractedText = parseJsonNodeByPath(rootNode, jsonPath);
                            if (extractedText == null) {
                                log.warn("Could not extract text from OpenAI response using path: {}. Raw response: {}", jsonPath, responseBody);
                                throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR);
                            }
                            return (T) extractedText;
                        } catch (IOException e) {
                            log.error("Error parsing OpenAI API response JSON (Attempt {}): {}", attempt, e.getMessage(), e);
                            throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR);
                        }
                    } else {
                        return (T) responseBody;
                    }
                } else if (responseEntity.getStatusCode().value() == 503 || responseEntity.getStatusCode().value() == 429) {
                    // Xử lý lỗi 503 Service Unavailable hoặc 429 Too Many Requests (Rate Limit của OpenAI)
                    log.warn("OpenAI API returned {} (Attempt {}/{}) for URL {}. Retrying...",
                            responseEntity.getStatusCode().value(), attempt, MAX_RETRIES, url);
                    if (attempt < MAX_RETRIES) {
                        long currentDelay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1); // Exponential backoff
                        try {
                            log.info("Retrying after {} ms...", currentDelay);
                            Thread.sleep(currentDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        log.error("Exceeded max retries for OpenAI API call due to {} error for URL {}.", responseEntity.getStatusCode().value(), url);
                        throw new AppException(ErrorCode.GEMINI_API_ERROR); // Giữ ErrorCode cũ hoặc đổi mới
                    }
                } else {
                    log.error("OpenAI API returned non-2xx status (Attempt {}): {} - {}", attempt, responseEntity.getStatusCode(), responseBody);
                    throw new AppException(ErrorCode.GEMINI_API_ERROR); // Giữ ErrorCode cũ hoặc đổi mới
                }
            } catch (HttpServerErrorException.ServiceUnavailable | HttpServerErrorException.BadGateway e) {
                // Bắt lỗi 503 Service Unavailable hoặc 502 Bad Gateway từ Exception
                log.warn("OpenAI API returned 5xx Service Unavailable/Bad Gateway (Exception, Attempt {}/{}) for URL {}: {}",
                        attempt, MAX_RETRIES, url, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    long currentDelay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1); // Exponential backoff
                    try {
                        log.info("Retrying after {} ms...", currentDelay);
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    log.error("Exceeded max retries for OpenAI API call due to 5xx exception for URL {}.", url);
                    throw new AppException(ErrorCode.GEMINI_API_ERROR);
                }
            } catch (HttpClientErrorException e) {
                log.error("Client error calling OpenAI API (Attempt {}): {}: {}", attempt, e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new AppException(ErrorCode.GEMINI_API_ERROR);
            } catch (ResourceAccessException e) {
                log.error("Network or connection error calling OpenAI API for URL {} (Attempt {}): {}", url, attempt, e.getMessage(), e);
                throw new AppException(ErrorCode.NETWORK_ERROR);
            } catch (Exception e) {
                log.error("Unexpected error calling OpenAI API for URL {} (Attempt {}): {}", url, attempt, e.getMessage(), e);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

    // parseJsonNodeByPath giữ nguyên
    private String parseJsonNodeByPath(JsonNode rootNode, String path) {
        String[] parts = path.split("\\.");
        JsonNode currentNode = rootNode;
        for (String part : parts) {
            if (part.contains("[")) {
                String arrayName = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));
                currentNode = currentNode.path(arrayName);
                if (currentNode.isArray() && currentNode.size() > index) {
                    currentNode = currentNode.get(index);
                } else {
                    return null;
                }
            } else {
                currentNode = currentNode.path(part);
            }
            if (currentNode.isMissingNode() || currentNode.isNull()) {
                return null;
            }
        }
        return currentNode.isTextual() ? currentNode.asText() : currentNode.toString();
    }
}