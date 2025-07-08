# Job Search với isSave Status

## Tổng quan

Tính năng này được implement để trả về trường boolean `isSave` khi người dùng tìm kiếm công việc, cho phép frontend biết được job nào đã được lưu bởi người dùng hiện tại.

## Các thay đổi chính

### 1. JobSearchService

- **Phương thức `search()`**: Đã được cải thiện để set `isSave` status cho mỗi job
- **Phương thức `setIsSaveStatus()`**: Helper method để xử lý logic set isSave
- **Phương thức `getAllJobsWithIsSaveStatus()`**: Lấy tất cả jobs từ database với isSave status
- **Phương thức `convertToJobDocument()`**: Convert Job entity sang JobDocument với isSave status

### 2. JobSearchController

- **API `/search`**: Đã được cập nhật để sử dụng JobSearchService với isSave status
- Xử lý cả trường hợp tìm kiếm với filters và lấy tất cả jobs

### 3. JobDocumentMapper

- **Phương thức `toJobResponse()`**: Đảm bảo mapping chính xác trường isSave từ JobDocument sang JobResponse
- Thêm logging để debug

### 4. JobMapper

- **Mapping JobDocument → JobResponse**: Cập nhật mapping để bao gồm tất cả trường cần thiết

## Cách hoạt động

1. **Khi user chưa đăng nhập**: 
   - Tất cả jobs sẽ có `isSave = false`

2. **Khi user đã đăng nhập**:
   - Hệ thống sẽ lấy danh sách job IDs đã lưu của user
   - So sánh và set `isSave = true` cho những job đã được lưu
   - Các job khác sẽ có `isSave = false`

## API Endpoints

### GET /api/jobs/search

**Parameters:**
- `keyword` (optional): Từ khóa tìm kiếm
- `location` (optional): Địa điểm
- `categoryId` (optional): ID danh mục
- `jobLevelId` (optional): ID cấp độ công việc
- `jobTypeId` (optional): ID loại công việc
- `educationId` (optional): ID trình độ học vấn

**Response:**
```json
[
  {
    "id": 1,
    "title": "Java Developer",
    "description": "Job description...",
    "location": "Ho Chi Minh City",
    "isSave": true,
    "category": {
      "id": 1,
      "name": "Technology"
    },
    "jobLevel": {
      "id": 1,
      "name": "Junior"
    },
    "jobType": {
      "id": 1,
      "name": "Full-time"
    },
    "education": {
      "id": 1,
      "name": "Bachelor"
    }
  }
]
```

## Logging

- **INFO**: Thông tin về số lượng saved jobs của user
- **DEBUG**: Chi tiết về từng job được mark as saved

## Lưu ý

- Tính năng này tương thích với authentication hiện tại
- Không ảnh hưởng đến performance khi user chưa đăng nhập
- Sử dụng lazy loading cho saved jobs để tối ưu hiệu suất
