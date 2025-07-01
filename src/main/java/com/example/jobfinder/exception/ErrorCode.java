package com.example.jobfinder.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    //1000 _> Success

    // Mã 1xxx: Các lỗi hệ thống, không phân loại hoặc liên quan đến request/response chung.
    UNCATEGORIZED_EXCEPTION(1000, "Uncategorized error"), // Lỗi không phân loại
    INVALID_KEY(1001, "Invalid key"), // Key không hợp lệ (ví dụ: trong JWT)
    INVALID_PARAMETER(1002, "Invalid parameter"), // Tham số không hợp lệ (có thể kèm thông tin chi tiết từ validation)
    INVALID_INPUT_DATA(1003, "Invalid input data"), // Dữ liệu đầu vào chung không hợp lệ (validation tổng quát)

    // -----------------------------------------------------------
    // Nhóm lỗi xác thực & phân quyền (Authentication & Authorization Errors) - Mã từ 2000 đến 2999
    // -----------------------------------------------------------
    // Mã 2xxx: Lỗi liên quan đến đăng nhập, đăng ký, token, quyền hạn.
    EMAIL_EXISTED(2001, "Email already exists"), // User đã tồn tại (khi đăng ký với email đã có)
    USERNAME_INVALID(2002, "Username is invalid, must be at least 3 characters"),
    PASSWORD_INVALID(2003, "Password is invalid, must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character"),
    USER_NOT_FOUND(2004, "User not found"), // Người dùng không tồn tại (khi tìm kiếm user không có)
    UNAUTHENTICATED(2005, "Unauthenticated"), // Chưa xác thực (ví dụ: thiếu token, token hết hạn)
    UNAUTHORIZED(2006, "You do not have permission to perform this action"), // Không có quyền (đã xác thực nhưng không đủ quyền)
    CREDENTIAL_INVALID(2007, "Invalid username or password"), // Dùng cho lỗi đăng nhập sai tài khoản/mật khẩu
    USER_NOT_VERIFIED(2008, "User not verified, please verify your email"), // Tài khoản chưa được xác minh
    INVALID_TOKEN(2009, "Invalid or expired token"), // Dùng cho JWT token không hợp lệ/hết hạn
    OLD_PASSWORD_NOT_MATCH(2010, "Old password is not correct"), // Khi đổi mật khẩu cũ không đúng
    USER_EXIST(2011, "User already exists"),

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến quản lý danh mục (Category) - Mã từ 3000 đến 3099
    // -----------------------------------------------------------
    // Mã 30xx
    CATEGORY_NOT_FOUND(3000, "Category not found"),
    CATEGORY_NAME_EXISTED(3001, "Category name already exists"), // Tên Category đã tồn tại (khi tạo/cập nhật)
    CATEGORY_IN_USE(3002, "Category cannot be deleted as it is currently in use"), // Category đang được sử dụng, không thể xóa

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến cấp độ công việc (Job Level) - Mã từ 3100 đến 3199
    // -----------------------------------------------------------
    // Mã 31xx
    JOB_LEVEL_NOT_FOUND(3100, "Job Level not found"),
    JOB_LEVEL_NAME_EXISTED(3101, "Job Level name already exists"),
    JOB_LEVEL_IN_USE(3102, "Job Level cannot be deleted as it is currently in use"),

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến loại công việc (Job Type) - Mã từ 3200 đến 3299
    // -----------------------------------------------------------
    // Mã 32xx
    JOB_TYPE_NOT_FOUND(3200, "Job Type not found"),
    JOB_TYPE_NAME_EXISTED(3201, "Job Type name already exists"),
    JOB_TYPE_IN_USE(3202, "Job Type cannot be deleted as it is currently in use"),

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến Education (Education) - Mã từ 3300 đến 3399 <-- MỚI
    // -----------------------------------------------------------
    EDUCATION_NOT_FOUND(3300, "Education not found"),
    EDUCATION_NAME_EXISTED(3301, "Education name already exists"),
    EDUCATION_IN_USE(3302, "Education cannot be deleted as it is currently in use"), // Nếu Education được dùng bởi UserProfile/Job

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến vai trò (Role) - Mã từ 4000 đến 4099
    // -----------------------------------------------------------
    // Mã 40xx
    ROLE_NOT_FOUND(4000, "Role not found"),
    ROLE_NAME_EXISTED(4001, "Role name already exists"),
    ROLE_IN_USE(4002, "Role cannot be deleted as it is currently assigned to users"), // Role đang được gán cho user

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến công việc (Job) - Mã từ 5000 đến 5099
    // -----------------------------------------------------------
    // Mã 50xx
    JOB_NOT_FOUND(5000, "Job not found"),
    JOB_ALREADY_EXISTS(5001, "Job with this title for this employer already exists"),
    INVALID_SALARY_RANGE(5002, "Minimum salary cannot be greater than maximum salary"),
    JOB_UPDATE_NOT_ALLOWED(5003, "You are not allowed to update this job"),
    JOB_DELETE_NOT_ALLOWED(5004, "You are not allowed to delete this job"),
    EMPLOYER_NOT_FOUND(5005, "Employer not found"), // Employer không tồn tại khi tạo/cập nhật Job

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến hồ sơ ứng tuyển (Application) - Mã từ 6000 đến 6099
    // -----------------------------------------------------------
    // Mã 60xx
    APPLICATION_NOT_FOUND(6000, "Application not found"),
    APPLICATION_ALREADY_SUBMITTED(6001, "Application already exists for this job and user"),
    INVALID_APPLICATION_STATUS_TRANSITION(6002, "Invalid application status transition"),

    // -----------------------------------------------------------
    // Nhóm lỗi liên quan đến Profile người dùng (User Details/Profile) - Mã từ 7000 đến 7099
    // -----------------------------------------------------------
    // Mã 70xx
    PROFILE_NOT_FOUND(7000, "User profile not found"),
    PROFILE_UPDATE_NOT_ALLOWED(7001, "You are not allowed to update this profile"),
    EMAIL_INVALID(7002, "Email is invalid"),
    PHONE_NUMBER_INVALID(7003, "Phone number is invalid"),
    ADDRESS_INVALID(7004, "Address is invalid"),
    DATE_OF_BIRTH_INVALID(7005, "Date of birth is invalid"),
    GENDER_INVALID(7006, "Gender is invalid"),

    NOTIFICATION_NOT_FOUND(8000, "Notification not found"),
    NOTIFICATION_MARK_READ_FAILED(8001, "Failed to mark notification as read"),
    NOTIFICATION_DELETE_FAILED(8002, "Failed to delete notification"),

    APPLICATION_DENIED(8003, "Application has been denied"),
    APPLICATION_WITHDRAWN(8004, "Application has been withdrawn"),
    APPLICATION_LIMIT_EXCEEDED(8005, "Application limit exceeded for this user or job"),
    APPLICATION_UPDATE_NOT_ALLOWED(8006, "You are not allowed to update this application"),
    // Thêm các lỗi sau:
    UNAUTHORIZED_APPLICATION_UPDATE(8007, "You are not authorized to update this application's status"),
    INVALID_APPLICATION_STATUS(8008, "Invalid application status provided"),

    REVIEW_NOT_FOUND(6000, "Review not found"),
    REVIEW_ALREADY_EXISTS(6001, "You have already submitted a review for this employer"),
    CANNOT_REVIEW_SELF(6002, "You cannot review yourself"),
    UNAUTHORIZED_REVIEW_ACTION(6003, "You are not authorized to perform this review action"),
    INVALID_RATING_VALUE(6005, "Rating value must be between 1 and 5"),
    REVIEW_UNAUTHORIZED_NO_RELATION(6006, "You can only review employers you have applied to"),

    // ... (các mã lỗi hiện có của bạn)

    // Nhóm lỗi liên quan đến Social Type (Mã từ 7000 đến 7099)
// Mã 70xx
    SOCIAL_TYPE_NOT_FOUND(7000, "Social type not found"),
    SOCIAL_TYPE_ALREADY_EXISTS(7001, "Social type name already exists"),
    SOCIAL_TYPE_IN_USE(7002, "Social type cannot be deleted as it is in use by user social links"),

    // Nhóm lỗi liên quan đến User Social Type (Mã từ 7100 đến 7199)
// Mã 71xx
    USER_SOCIAL_TYPE_NOT_FOUND(7100, "User social link not found"),
    USER_SOCIAL_TYPE_ALREADY_EXISTS(7101, "You already have a link for this social type"),
    UNAUTHORIZED_USER_SOCIAL_ACTION(7102, "You are not authorized to perform this action on user social link"),
    INVALID_SOCIAL_URL(7103, "Invalid social media URL format"),
  
    INVALID_EMAIL(7104, "Email not found or invalid" ),
    WRONG_PASSWORD(7105, "Wrong password or email"),


    // Role & Permission Errors (Lỗi liên quan đến Vai trò & Quyền hạn)
    PERMISSION_NOT_FOUND(2001, "Quyền không tìm thấy"),
    USER_IS_NOT_JOB_SEEKER(2002, "Người dùng này không phải là người tìm việc"), // <-- Mới
    USER_IS_NOT_EMPLOYER(2003, "Người dùng này không phải là nhà tuyển dụng"),

    RESOURCE_NOT_FOUND(2004, "Resource not found"),

    GEMINI_API_ERROR(2005, "GEMINI_API_ERROR"),
    UNEXPECTED_ERROR(2006, "Unexpected error"),

    CHATBOT_HISTORY_NOT_FOUND(2007, "Chatbot history not found"),
    FAILED_TO_SAVE_CHAT_HISTORY(2008, "Failed to save chatbot history"),
    CONVERSATION_NOT_FOUND(2009, "Conversation not found"),
    ELASTICSEARCH_ERROR(2010, "elasticsearch error" ),
    INVALID_ROLE(2011, "Role not permission"),
    EXPERIENCE_NOT_FOUND(2012," Experience not found"),
    SAVED_JOB_NOT_FOUND(2013, "You have not saved this job"),
    TOKEN_EXPIRED(2014, "Token has expired");


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
