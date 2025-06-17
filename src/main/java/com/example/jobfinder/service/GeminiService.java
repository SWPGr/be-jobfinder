package com.example.jobfinder.service;

import com.example.jobfinder.dto.gemini.GeminiIntentRequest; // Import DTO mới
import com.example.jobfinder.dto.gemini.GeminiIntentResponse; // Import DTO mới
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper; // Thêm ObjectMapper để xử lý JSON

    @Value("${google.gemini.api-key}")
    private String geminiApiKey;

    @Value("${google.gemini.model-name}")
    private String modelName; // Ví dụ: gemini-1.5-flash-latest hoặc gemini-pro

    public GeminiService(RestTemplate restTemplate, ObjectMapper objectMapper) { // Inject ObjectMapper
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String getGeminiResponse(String prompt) throws IOException {
        log.info("Sending text prompt to Gemini: {}", prompt);
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        String escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", "\\n");
        String requestBody = String.format(
                "{\"contents\": [{\"parts\": [{\"text\": \"%s\"}]}]}",
                escapedPrompt
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        return executeGeminiRequest(url, request, String.class, "candidates[0].content.parts[0].text");
    }

    public GeminiIntentResponse.IntentAnalysisResult analyzeIntent(String userQuery, String systemInstruction) throws IOException {
        log.info("Analyzing intent for query: {}", userQuery);

        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                modelName, geminiApiKey);

        // Xây dựng prompt cho Gemini để nó trả về JSON
        // SystemInstruction sẽ hướng dẫn Gemini về định dạng và nội dung cần trả về
        String promptForIntent = String.format("%s\n\nUser query: \"%s\"", systemInstruction, userQuery);

        GeminiIntentRequest requestBody = new GeminiIntentRequest(
                List.of(new GeminiIntentRequest.Content(
                        List.of(new GeminiIntentRequest.Part(promptForIntent))
                ))
        );

        // Chuyển đổi DTO thành JSON String
        String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonRequestBody, headers);

        String rawResponse = executeGeminiRequest(url, request, String.class, null); // Nhận phản hồi thô

        try {
            // Parse phản hồi thô thành GeminiIntentResponse.Candidate và sau đó trích xuất JSON Intent
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            JsonNode candidatesNode = rootNode.path("candidates");

            if (candidatesNode.isArray() && candidatesNode.size() > 0) {
                JsonNode firstCandidate = candidatesNode.get(0);
                JsonNode contentNode = firstCandidate.path("content");
                JsonNode partsNode = contentNode.path("parts");

                if (partsNode.isArray() && partsNode.size() > 0) {
                    JsonNode firstPart = partsNode.get(0);
                    JsonNode textNode = firstPart.path("text");
                    if (textNode.isTextual()) {
                        String jsonString = textNode.asText();
                        // Loại bỏ các ký tự thừa như ```json ... ``` nếu Gemini trả về
                        jsonString = jsonString.replace("```json", "").replace("```", "").trim();
                        // Parse JSON String thành IntentAnalysisResult DTO
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


    // Phương thức chung để xử lý gửi request và parse response
    private <T> T executeGeminiRequest(String url, HttpEntity<?> request, Class<T> responseType, String jsonPath) throws IOException {
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            String responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Gemini raw response: {}", responseBody);
                if (jsonPath != null) {
                    // Nếu cần parse theo jsonPath (cho getGeminiResponse)
                    JsonNode rootNode = objectMapper.readTree(responseBody);
                    // Đây là logic parse chung, bạn có thể cần một hàm helper phức tạp hơn
                    // Hoặc đơn giản hóa thành một vài trường hợp parse đã biết
                    return (T) parseJsonNodeByPath(rootNode, jsonPath);
                } else {
                    // Trả về responseBody thô nếu không cần parse theo jsonPath (cho analyzeIntent)
                    return (T) responseBody;
                }
            } else {
                log.error("Error calling Gemini API: {} - Status: {}", responseBody, response.getStatusCode());
                throw new IOException("Lỗi khi gọi Gemini API: " + responseBody);
            }
        } catch (HttpClientErrorException e) {
            log.error("Client error calling Gemini API: {} - Status: {}", e.getResponseBodyAsString(), e.getStatusCode(), e);
            throw new IOException("Lỗi client khi gọi Gemini API: " + e.getResponseBodyAsString(), e);
        } catch (IOException e) {
            log.error("Error processing Gemini API response or IO issue: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling Gemini API: {}", e.getMessage(), e);
            throw new IOException("Lỗi không xác định khi gọi Gemini API: " + e.getMessage(), e);
        }
    }

    // Helper method để parse JsonNode dựa trên path.
    // Đây là một cách đơn giản, có thể cần phức tạp hơn cho các path lồng nhau.
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