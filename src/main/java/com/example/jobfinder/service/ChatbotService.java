package com.example.jobfinder.service;


import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
public class ChatbotService  {

    public String getChatbotResponse(String userQuery) {
        StringBuilder response = new StringBuilder();
        try {
            // Xác định đường dẫn đến script Python
            // Đảm bảo rằng file Python_scripts/chatbot_gemini.py có quyền thực thi
            // và rằng interpreter Python có sẵn trong PATH của hệ thống.
            // Hoặc cung cấp đường dẫn đầy đủ đến interpreter Python của bạn:
            // String pythonExecutable = "/usr/bin/python3"; // hoặc "python" trên Windows

            // Sử dụng ClassLoader để lấy đường dẫn tương đối đến resource
            // Đây là cách an toàn hơn khi triển khai ứng dụng
            String scriptPath = getClass().getClassLoader().getResource("python_scripts/chatbot_gemini.py").getFile();

            // Nếu bạn muốn truyền userQuery vào script Python
            // Đảm bảo script Python có thể nhận tham số dòng lệnh (sys.argv)
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, userQuery);

            // Nếu bạn cần cấu hình thư mục làm việc (working directory)
            // File scriptDir = new File(getClass().getClassLoader().getResource("python_scripts/").getFile());
            // pb.directory(scriptDir);

            Process p = pb.start(); // Bắt đầu tiến trình Python

            // Đọc output từ Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }

            // Đọc lỗi nếu có
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.err.println("Python Error: " + errorLine);
            }

            int exitCode = p.waitFor(); // Chờ script Python kết thúc
            System.out.println("Python script exited with code: " + exitCode);

            if (exitCode == 0) {
                return response.toString().trim();
            } else {
                return "Error calling Python script: " + response.toString().trim();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Internal server error during chatbot request.";
        }
    }
}
