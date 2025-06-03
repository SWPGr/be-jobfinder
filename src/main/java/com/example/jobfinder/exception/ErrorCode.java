package com.example.jobfinder.exception;

public enum ErrorCode {

    //1000 _> Success

    // General Errors (Lỗi chung)
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error"), // Lỗi không phân loại
    INVALID_KEY(1001, "invalid key"), // Key không hợp lệ (ví dụ: trong trường hợp JWT)

    // User-related Errors (Lỗi liên quan đến User)
    USER_EXIST(1002, "user existed"), // Người dùng đã tồn tại (khi đăng ký với email/username đã có)
    USERNAME_INVALID(1003, "username is invalid, must at least 3 characters"), // Username không hợp lệ
    PASSWORD_INVALID(1004, "password is invalid, must at more than 8 characters"), // Mật khẩu không hợp lệ
    USER_NOT_EXIST(1005, "user not existed"), // Người dùng không tồn tại (khi tìm kiếm user không có)
    UNAUTHENTICATED(1006, "Unauthenticated"), // Chưa xác thực (ví dụ: thiếu token, token hết hạn)
    UNAUTHORIZED(1007, "You do not have permission"), // Không có quyền (đã xác thực nhưng không đủ quyền)

    // Job-related Errors (Lỗi liên quan đến Job)
    JOB_NOT_FOUND(1008, "Job not found"), // Job không tồn tại (đã đổi exsited thành found cho đúng ngữ pháp)
    JOB_INVALID_INPUT(1009, "Invalid job input data"), // Dữ liệu đầu vào cho Job không hợp lệ (validation)
    EMPLOYER_NOT_FOUND(1010, "Employer not found"), // Employer không tồn tại khi tạo/cập nhật Job
    JOB_EXIST(1023, "Job existed"),

    // Category-relatded Errors (Lỗi liên quan đến Category)
    // Category-relatded Errors (Lỗi liên quan đến Category)
    CATEGORY_NOT_FOUND(1011, "Category not found"), // Category không tồn tại
    CATEGORY_NAME_EXISTED(1012, "Category name existed"), // Tên Category đã tồn tại (khi tạo Category mới)
    CATEGORY_INVALID_INPUT(1013, "Invalid category input data"), // Dữ liệu đầu vào cho Category không hợp lệ

    // Role-related Errors (Lỗi liên quan đến Role)
    ROLE_NOT_FOUND(1014, "Role not found"), // Role không tồn tại
    ROLE_NAME_EXISTED(1015, "Role name existed"), // Tên Role đã tồn tại
    ROLE_INVALID_INPUT(1016, "Invalid role input data"), // Dữ liệu đầu vào cho Role không hợp lệ

    // Application-related Errors (Lỗi liên quan đến Application - Hồ sơ ứng tuyển)
    APPLICATION_NOT_FOUND(1017, "Application not found"), // Hồ sơ ứng tuyển không tồn tại
    APPLICATION_ALREADY_EXISTS(1018, "Application already exists for this job and user"), // Đã nộp hồ sơ cho công việc này
    INVALID_APPLICATION_STATUS(1019, "Invalid application status transition"), // Chuyển trạng thái hồ sơ không hợp lệ

    // Common Validation Errors (Lỗi Validation chung)
    INVALID_PARAMETER(1020, "Invalid parameter: "), // Tham số không hợp lệ (có thể thêm tên tham số)
    EMAIL_INVALID(1021, "Email is invalid"), // Email không đúng định dạng
    PHONE_NUMBER_INVALID(1022, "Phone number is invalid"); // Số điện thoại không đúng định dạng



    ;

    ErrorCode(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    private int errorCode;
    private String errorMessage;

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
