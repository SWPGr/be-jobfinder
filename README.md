# 🧑‍💼 JobFinder - Nền tảng tìm kiếm việc làm

JobFinder là một ứng dụng web được xây dựng bằng Spring Boot giúp kết nối người tìm việc với các nhà tuyển dụng. Ứng dụng hỗ trợ quản lý công việc, hồ sơ người dùng, đề xuất việc làm và tìm kiếm nâng cao.

## 🚀 Tính năng nổi bật

- Đăng ký / Đăng nhập người dùng và nhà tuyển dụng
- Quản lý thông tin cá nhân và hồ sơ xin việc (CV)
- Nhà tuyển dụng tạo, chỉnh sửa và quản lý tin tuyển dụng
- Gợi ý công việc cho người tìm việc dựa trên kỹ năng và kinh nghiệm
- Tìm kiếm công việc nâng cao theo từ khóa, vị trí, mức lương,...
- Thống kê, báo cáo lượt xem, ứng tuyển
- Phân quyền vai trò (Admin, Job Seeker, Company)

## 🛠️ Công nghệ sử dụng

- **Spring Boot** 3.x
- **Spring Security** – Xác thực & phân quyền
- **Spring Data JPA** – Kết nối cơ sở dữ liệu
- **MySQL** – Hệ quản trị cơ sở dữ liệu
- **Hibernate** – ORM
- **Lombok** – Giảm boilerplate code
- **MapStruct** – Mapping DTO <-> Entity
- **JWT** – Xác thực người dùng
- **ReactJS / Angular** *(frontend nếu có)*

## 📂 Cấu trúc thư mục

```bash
src
├── main
│   ├── java/com/example/jobfinder
│   │   ├── controller         # REST APIs
│   │   ├── dto                # DTO classes
│   │   ├── entity             # JPA entities
│   │   ├── exception          # Custom exceptions
│   │   ├── repository         # JPA repositories
│   │   ├── security           # Config JWT và bảo mật
│   │   ├── service            # Business logic
│   │   └── JobFinderApplication.java
│   └── resources
│       ├── application.yml    # File cấu hình
│       └── static/templates   # (nếu có dùng frontend nội bộ)


## ⚙️ Hướng dẫn cài đặt
git clone https://github.com/<your-username>/jobfinder.git
cd jobfinder

## Cấu hình cơ sở dữ liệu**
application.properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/jobfinder?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Mail properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=phihung19022003@gmail.com
spring.mail.password=xxdy adlt oqic wnyh
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true


logging.level.org.springframework.security=DEBUG
logging.level.com.jobplatform=DEBUG
debug=true

