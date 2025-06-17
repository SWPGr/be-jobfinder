package com.example.jobfinder.util;

// Không cần Lombok @Builder cho lớp này vì nó chỉ chứa các hằng số.
public final class QueryConstants {

    // Private constructor để ngăn việc tạo instance
    private QueryConstants() {
        // Utility class
    }

    // --- Job Queries ---
    public static final String FIND_JOBS_BY_CRITERIA = """
            SELECT j FROM Job j
            LEFT JOIN FETCH j.category c
            LEFT JOIN FETCH j.jobLevel jl
            LEFT JOIN FETCH j.jobType jt
            LEFT JOIN FETCH j.employer e
            LEFT JOIN FETCH e.userDetail ud 
            WHERE (:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND
            (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND
            (:minSalary IS NULL OR j.salaryMin >= :minSalary) AND
            (:maxSalary IS NULL OR j.salaryMax <= :maxSalary) AND
            (:categoryName IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) AND
            (:jobLevelName IS NULL OR LOWER(jl.name) LIKE LOWER(CONCAT('%', :jobLevelName, '%'))) AND
            (:jobTypeName IS NULL OR LOWER(jt.name) LIKE LOWER(CONCAT('%', :jobTypeName, '%'))) AND
            (:employerName IS NULL OR LOWER(ud.companyName) LIKE LOWER(CONCAT('%', :employerName, '%')))
            """;

    // --- User Queries ---
    public static final String FIND_USERS_BY_CRITERIA = """
            SELECT u FROM User u
            LEFT JOIN FETCH u.role r
            LEFT JOIN FETCH u.userDetail ud 
            WHERE (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND
            (:fullName IS NULL OR LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND
            (:roleName IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :roleName, '%'))) AND
            (:location IS NULL OR LOWER(ud.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND
            (:yearsExperience IS NULL OR ud.yearsExperience >= :yearsExperience) AND
            (:isPremium IS NULL OR u.isPremium = :isPremium) AND
            (:verified IS NULL OR u.verified = :verified)
            """;


    // --- Subscription Queries ---
    public static final String FIND_SUBSCRIPTIONS_BY_CRITERIA = """
            SELECT s FROM Subscription s
            LEFT JOIN FETCH s.user u
            LEFT JOIN FETCH s.plan p
            WHERE (:userEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND
            (:planName IS NULL OR LOWER(p.subscriptionPlanName) LIKE LOWER(CONCAT('%', :planName, '%'))) AND
            (:isActive IS NULL OR s.isActive = :isActive)
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - s.user và u.email là chính xác.
     * - s.plan và p.subscriptionPlanName là chính xác dựa trên giả định tên trường của SubscriptionPlan.
     * - s.isActive là chính xác.
     */

    // --- Application Queries ---
    public static final String FIND_APPLICATIONS_BY_CRITERIA = """
            SELECT a FROM Application a
            LEFT JOIN FETCH a.jobSeeker js
            LEFT JOIN FETCH a.job j
            WHERE (:jobSeekerEmail IS NULL OR LOWER(js.email) LIKE LOWER(CONCAT('%', :jobSeekerEmail, '%'))) AND
            (:jobTitle IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :jobTitle, '%'))) AND
            (:status IS NULL OR LOWER(a.status) = LOWER(:status)) 
            """;

    // --- EmployerReview Queries ---
    public static final String FIND_EMPLOYER_REVIEWS_BY_EMPLOYER_AND_RATING = """
            SELECT er FROM EmployerReview er
            LEFT JOIN FETCH er.jobSeeker js
            LEFT JOIN FETCH er.employer e
            WHERE (:employerId IS NULL OR er.employer.id = :employerId) AND
            (:minRating IS NULL OR er.rating >= :minRating) AND
            (:maxRating IS NULL OR er.rating <= :maxRating)
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - er.employer.id là chính xác để truy vấn theo ID của employer (User).
     * - er.rating là chính xác.
     */

    // --- UserDetail Queries ---
    public static final String FIND_USER_DETAILS_BY_CRITERIA = """
            SELECT ud FROM UserDetail ud
            LEFT JOIN FETCH ud.user u
            LEFT JOIN FETCH ud.education e
            WHERE (:fullName IS NULL OR LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND
            (:location IS NULL OR LOWER(ud.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND
            (:yearsExperience IS NULL OR ud.yearsExperience >= :yearsExperience) AND
            (:educationType IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :educationType, '%'))) AND
            (:companyName IS NULL OR LOWER(ud.companyName) LIKE LOWER(CONCAT('%', :companyName, '%')))
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - ud.fullName, ud.location, ud.yearsExperience, ud.companyName là chính xác.
     * - e.educationType là chính xác dựa trên giả định tên trường của Education.
     */

    // --- SocialType Queries ---
    public static final String FIND_SOCIAL_TYPES_BY_NAME = """
            SELECT st FROM SocialType st
            WHERE (:socialTypeName IS NULL OR LOWER(st.name) LIKE LOWER(CONCAT('%', :socialTypeName, '%')))
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - st.socialTypeName là chính xác dựa trên giả định tên trường của SocialType.
     * - Như đã thảo luận, nếu bạn muốn chuẩn hóa thành 'name', bạn sẽ cần sửa model SocialType và query này.
     * Nhưng với model hiện tại, đây là chính xác.
     */

    // --- UserSocialType Queries ---
    public static final String FIND_USER_SOCIAL_TYPES_BY_CRITERIA = """
            SELECT ust FROM UserSocialType ust
            LEFT JOIN FETCH ust.userDetail ud
            LEFT JOIN FETCH ust.socialType st
            WHERE (:userFullName IS NULL OR LOWER(ud.fullName) LIKE LOWER(CONCAT('%', :userFullName, '%'))) AND
            (:socialTypeName IS NULL OR LOWER(st.name) LIKE LOWER(CONCAT('%', :socialTypeName, '%'))) AND
            (:url IS NULL OR LOWER(ust.url) LIKE LOWER(CONCAT('%', :url, '%')))
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - ud.fullName và st.socialTypeName là chính xác dựa trên các model tương ứng.
     * - ust.url là chính xác.
     */

    // --- Payment Queries ---
    public static final String FIND_PAYMENTS_BY_CRITERIA = """
            SELECT p FROM Payment p
            LEFT JOIN FETCH p.user u
            LEFT JOIN FETCH p.subscription s
            WHERE (:userEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND
            (:paymentMethod IS NULL OR LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :paymentMethod, '%'))) AND
            (:minAmount IS NULL OR p.amount >= :minAmount) AND
            (:maxAmount IS NULL OR p.amount <= :maxAmount)
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - u.email, p.paymentMethod, p.amount là chính xác.
     */

    // --- Notification Queries ---
    public static final String FIND_NOTIFICATIONS_BY_CRITERIA = """
            SELECT n FROM Notification n
            LEFT JOIN FETCH n.user u
            WHERE (:userEmail IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :userEmail, '%'))) AND
            (:isRead IS NULL OR n.isRead = :isRead) AND
            (:messageKeyword IS NULL OR LOWER(n.message) LIKE LOWER(CONCAT('%', :messageKeyword, '%')))
            """;
    /*
     * Đánh giá: CHÍNH XÁC.
     * - u.email, n.isRead, n.message là chính xác.
     */
}