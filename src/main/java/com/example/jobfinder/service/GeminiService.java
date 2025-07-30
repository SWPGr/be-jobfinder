// src/main/java/com/example/jobfinder/service/GeminiService.java
package com.example.jobfinder.service;

import com.example.jobfinder.dto.gemini.GeminiIntentRequest;
import com.example.jobfinder.dto.gemini.GeminiIntentResponse;
import com.example.jobfinder.exception.AppException; // Import AppException
import com.example.jobfinder.exception.ErrorCode; // Import ErrorCode
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod; // Cần thiết cho restTemplate.exchange
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException; // ✅ Import HttpServerErrorException
import org.springframework.web.client.ResourceAccessException; // ✅ Import ResourceAccessException
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections; // Cần thiết cho Collections.singletonMap/List
import java.util.List;
import java.util.Map; // Cần thiết cho Map

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${google.gemini.model-name}")
    private String modelName; // Ví dụ: gemini-1.5-flash-latest hoặc gemini-pro

    // ✅ Thêm hằng số cấu hình retry
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 giây
    private Class<?> responseType;

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getGeminiResponse(String prompt) { // Loại bỏ throws IOException để xử lý nội bộ bằng AppException
        log.info("Sending text prompt to Gemini: {}", prompt);
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        try {
            // ✅ Sử dụng ObjectMapper để tạo request body an toàn hơn thay vì String.format
            Map<String, Object> part = Collections.singletonMap("text", prompt);
            Map<String, Object> content = Collections.singletonMap("parts", Collections.singletonList(part));
            Map<String, Object> requestBodyMap = Collections.singletonMap("contents", Collections.singletonList(content));

            String jsonRequestBody = objectMapper.writeValueAsString(requestBodyMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

            return executeGeminiRequest(url, request, String.class, "candidates[0].content.parts[0].text");
        } catch (IOException e) {
            log.error("Error creating Gemini request body: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }


    public GeminiIntentResponse.IntentAnalysisResult analyzeIntent(String userQuery, String systemInstruction) throws IOException {
        log.info("Analyzing intent for query: {}", userQuery);

        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        List<GeminiIntentRequest.Content> contents = new ArrayList<>();
        contents.add(new GeminiIntentRequest.Content(
                List.of(new GeminiIntentRequest.Part(systemInstruction)), "user"
        ));
        contents.add(new GeminiIntentRequest.Content(
                List.of(new GeminiIntentRequest.Part(userQuery)), "user"
        ));

        GeminiIntentRequest requestBody = new GeminiIntentRequest(contents);

        String jsonRequestBody;
        try {
            // ✅ Sử dụng ObjectMapper để tạo JSON từ DTO
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (IOException e) {
            log.error("Error serializing GeminiIntentRequest: {}", e.getMessage(), e);
            throw new IOException("Lỗi tạo yêu cầu phân tích ý định: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        // ✅ Nhận phản hồi thô, jsonPath là null để không parse trong executeGeminiRequest
        String rawResponse = executeGeminiRequest(url, request, String.class, null);

        try {
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && !candidatesNode.isEmpty()) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && !partsNode.isEmpty()) {
                    JsonNode firstPart = partsNode.get(0);
                    JsonNode textNode = firstPart.path("text");
                    if (textNode.isTextual()) {
                        String jsonString = textNode.asText();
                        // Loại bỏ markdown code block nếu có
                        jsonString = jsonString.replace("```json", "").replace("```", "").trim();
                        return objectMapper.readValue(jsonString, GeminiIntentResponse.IntentAnalysisResult.class);
                    }
                }
            }
            log.warn("Gemini intent analysis response did not contain expected JSON: {}", rawResponse);
            GeminiIntentResponse.IntentAnalysisResult result = new GeminiIntentResponse.IntentAnalysisResult();
            result.setIntent("unclear");
            return result;
        } catch (IOException e) {
            log.error("Failed to parse Gemini intent analysis response: {}", rawResponse, e);
            throw new IOException("Lỗi khi phân tích ý định từ Gemini.", e);
        }
    }

    public String generateResponseWithContext(String userMessage, String context) { // Loại bỏ throws IOException
        log.info("Generating response with context for user message: '{}', context: '{}'", userMessage, context);

        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        List<GeminiIntentRequest.Content> contents = new ArrayList<>();

        if (context != null && !context.trim().isEmpty()) {
            contents.add(new GeminiIntentRequest.Content(
                    List.of(new GeminiIntentRequest.Part("Thông tin dữ liệu: " + context)), "user"
            ));
        }

        contents.add(new GeminiIntentRequest.Content(
                List.of(new GeminiIntentRequest.Part(userMessage)), "user"
        ));

        GeminiIntentRequest requestBody = new GeminiIntentRequest(contents);

        String jsonRequestBody;
        try {
            // ✅ Sử dụng ObjectMapper để tạo JSON từ DTO
            jsonRequestBody = objectMapper.writeValueAsString(requestBody);
        } catch (IOException e) {
            log.error("Error serializing GeminiIntentRequest for context generation: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        return executeGeminiRequest(url, request, String.class, "candidates[0].content.parts[0].text");
    }


    // Phương thức chung để xử lý gửi request và parse response
    private <T> T executeGeminiRequest(String url, HttpEntity<?> request, Class<T> responseType, String jsonPath) {
        this.responseType = responseType;
        for (int attempt = 1; true; attempt++) {
            try {
                log.debug("Executing Gemini request to {} (Attempt {}/{})", url, attempt, MAX_RETRIES);
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
                String responseBody = responseEntity.getBody();

                if (responseEntity.getStatusCode().is2xxSuccessful()) {
                    log.debug("Gemini raw response (Attempt {}): {}", attempt, responseBody);
                    if (jsonPath != null) {
                        if (responseBody == null || responseBody.isEmpty()) {
                            log.warn("Gemini API returned empty response body for jsonPath request.");
                            throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR);
                        }
                        try {
                            JsonNode rootNode = objectMapper.readTree(responseBody);
                            String extractedText = parseJsonNodeByPath(rootNode, jsonPath);
                            if (extractedText == null) {
                                log.warn("Could not extract text from Gemini response using path: {}. Raw response: {}", jsonPath, responseBody);
                                // Thay vì trả về chuỗi rỗng, nên ném lỗi để xử lý nhất quán hơn
                                throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR);
                            }
                            return (T) extractedText;
                        } catch (IOException e) {
                            log.error("Error parsing Gemini API response JSON (Attempt {}): {}", attempt, e.getMessage(), e);
                            throw new AppException(ErrorCode.GEMINI_RESPONSE_PARSING_ERROR);
                        }
                    } else {
                        // Trường hợp jsonPath == null, trả về responseBody thô (dành cho analyzeIntent)
                        return (T) responseBody;
                    }
                } else if (responseEntity.getStatusCode().value() == 503) {
                    // ✅ Xử lý lỗi 503 Service Unavailable
                    log.warn("Gemini API returned 503 Service Unavailable (Attempt {}/{}) for URL {}. Retrying...",
                            attempt, MAX_RETRIES, url);
                    if (attempt < MAX_RETRIES) {
                        long currentDelay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1); // Exponential backoff
                        try {
                            log.info("Retrying after {} ms...", currentDelay);
                            Thread.sleep(currentDelay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.error("Thread interrupted during retry attempt {} for Gemini API call.", attempt);
                            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
                        }
                    } else {
                        log.error("Exceeded max retries for Gemini API call due to 503 error for URL {}.", url);
                        throw new AppException(ErrorCode.GEMINI_API_ERROR);
                    }
                } else {
                    // Xử lý các lỗi HTTP khác (4xx, 5xx khác 503)
                    log.error("Gemini API returned non-2xx status (Attempt {}): {} - {}", attempt, responseEntity.getStatusCode(), responseBody);
                    throw new AppException(ErrorCode.GEMINI_API_ERROR);
                }
            } catch (HttpServerErrorException.ServiceUnavailable e) {
                // ✅ Bắt lỗi 503 Service Unavailable từ HttpServerErrorException
                log.warn("Gemini API returned 503 Service Unavailable (Exception, Attempt {}/{}) for URL {}: {}",
                        attempt, MAX_RETRIES, url, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    long currentDelay = INITIAL_RETRY_DELAY_MS * (long) Math.pow(2, attempt - 1); // Exponential backoff
                    try {
                        log.info("Retrying after {} ms...", currentDelay);
                        Thread.sleep(currentDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();;
                        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    log.error("Exceeded max retries for Gemini API call due to 503 exception for URL {}.", url);
                    throw new AppException(ErrorCode.GEMINI_API_ERROR);
                }
            } catch (HttpClientErrorException e) {
                log.error("Client error calling Gemini API (Attempt {}): {}: {}", attempt, e.getStatusCode(), e.getResponseBodyAsString(), e);
                // Không retry cho lỗi client (4xx)
                throw new AppException(ErrorCode.GEMINI_API_ERROR);
            } catch (ResourceAccessException e) {
                log.error("Network or connection error calling Gemini API for URL {} (Attempt {}): {}", url, attempt, e.getMessage(), e);
                // Có thể retry cho lỗi mạng nếu muốn, nhưng ở đây tôi ném lỗi ngay
                throw new AppException(ErrorCode.NETWORK_ERROR);
            } catch (Exception e) {
                log.error("Unexpected error calling Gemini API for URL {} (Attempt {}): {}", url, attempt, e.getMessage(), e);
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }
    }

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
            } else { // Xử lý đối tượng
                currentNode = currentNode.path(part);
            }
            if (currentNode.isMissingNode() || currentNode.isNull()) {
                return null;
            }
        }
        return currentNode.isTextual() ? currentNode.asText() : currentNode.toString();
    }
}