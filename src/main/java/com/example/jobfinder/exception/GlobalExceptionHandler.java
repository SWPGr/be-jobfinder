package com.example.jobfinder.exception;

import com.example.jobfinder.dto.ApiResponse;
import com.example.jobfinder.exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// @ControllerAdvice: Đánh dấu lớp này là một bộ xử lý ngoại lệ toàn cục.
// Nó sẽ "lắng nghe" và xử lý các ngoại lệ ném ra từ bất kỳ Controller nào trong ứng dụng.
@ControllerAdvice
public class GlobalExceptionHandler {
    // @ExceptionHandler(value = RuntimeException.class): Chỉ định rằng phương thức này sẽ bắt
    // và xử lý tất cả các ngoại lệ thuộc loại RuntimeException (và các lớp con của nó)
    // mà không được xử lý bởi các @ExceptionHandler cụ thể hơn.
    // Những error mà chúng ta không bắt được sẽ trả về đây (lỗi không mong muốn, lỗi hệ thống)
    @ExceptionHandler(value = RuntimeException.class)
    ResponseEntity<ApiResponse> handleRuntimeException(RuntimeException ex) {

        ApiResponse apiResponse = new ApiResponse();
        // Gán mã lỗi và thông điệp từ ErrorCode.UNCATEGORY_EXCEPTION (một lỗi chung không phân loại)
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getErrorMessage());

        // Trả về một ResponseEntity với HTTP status code 400 (Bad Request)
        // và body chứa đối tượng ApiResponse đã định nghĩa lỗi chung.
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // @ExceptionHandler(value = AppException.class): Chỉ định rằng phương thức này sẽ bắt
    // và xử lý các ngoại lệ thuộc loại AppException. AppException là một ngoại lệ tùy chỉnh
    // được định nghĩa để xử lý các lỗi nghiệp vụ cụ thể.
    // Các message mà chúng ta bắt được sẽ cho vào handleAppException (lỗi nghiệp vụ đã định nghĩa)
    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handleAppException(AppException ex) {
        // Lấy ErrorCode (chứa mã và thông điệp lỗi cụ thể) từ đối tượng AppException được ném ra.
        ErrorCode errorCode = ex.getErrorCode();

        ApiResponse apiResponse = new ApiResponse();
        // Gán mã lỗi và thông điệp từ ErrorCode đã lấy được.
        apiResponse.setCode(errorCode.getErrorCode());
        apiResponse.setMessage(errorCode.getErrorMessage());

        // Trả về ResponseEntity với HTTP status code 400 (Bad Request)
        // và body chứa đối tượng ApiResponse với thông tin lỗi nghiệp vụ cụ thể.
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // @ExceptionHandler(value = MethodArgumentNotValidException.class): Chỉ định rằng phương thức này sẽ bắt
    // và xử lý các ngoại lệ xảy ra khi dữ liệu đầu vào không hợp lệ (ví dụ: validation thất bại
    // trên các trường của @RequestBody).
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException ex) {
        // Lấy defaultMessage từ FieldError đầu tiên. Thường đây là chuỗi bạn đặt trong annotation @NotNull(message = "Mã lỗi").
        String enumKey = ex.getFieldError().getDefaultMessage();
        System.out.println(enumKey); // In ra key lỗi để debug

        // Khởi tạo errorCode mặc định là INVALID_KEY.
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try {
            // Cố gắng chuyển đổi chuỗi enumKey thành một giá trị của enum ErrorCode.
            // Điều này cho phép bạn dùng key của enum làm thông điệp trong các annotation validation.
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Nếu chuỗi enumKey không khớp với bất kỳ giá trị nào trong enum ErrorCode,
            // nghĩa là có lỗi trong việc định nghĩa key, hoặc bạn chưa định nghĩa key đó.
            // Trong trường hợp này, errorCode vẫn giữ giá trị mặc định là INVALID_KEY.
            // (Bạn có thể log lỗi này để biết những key nào bị sai/thiếu)
        }

        ApiResponse apiResponse = new ApiResponse();

        // Gán mã lỗi và thông điệp dựa trên errorCode đã xác định (hoặc mặc định).
        apiResponse.setCode(errorCode.getErrorCode());
        apiResponse.setMessage(errorCode.getErrorMessage());

        // Trả về ResponseEntity với HTTP status code 400 (Bad Request)
        // và body chứa đối tượng ApiResponse với thông tin lỗi validation.
        return ResponseEntity.badRequest().body(apiResponse);
    }
}