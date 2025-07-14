# Employer Search API Documentation

## Overview
Hệ thống tìm kiếm Employer sử dụng Elasticsearch để tìm kiếm và lọc các nhà tuyển dụng (employers) dựa trên các tiêu chí khác nhau.

## API Endpoints

### 1. GET /api/employers/search
Tìm kiếm employers sử dụng query parameters.

**Parameters:**
- `name` (String, optional) - Tìm kiếm trong fullName và companyName
- `location` (String, optional) - Lọc theo địa điểm
- `organizationId` (Long, optional) - Lọc theo ID tổ chức
- `keyword` (String, optional) - Tìm kiếm keyword chung trong nhiều field
- `isPremium` (Boolean, optional) - Lọc theo trạng thái premium
- `verified` (Integer, optional) - Lọc theo trạng thái xác minh (0 hoặc 1)
- `minRating` (Float, optional) - Rating tối thiểu
- `minJobsPosted` (Integer, optional) - Số job đã đăng tối thiểu
- `teamSize` (String, optional) - Lọc theo quy mô team
- `minYearEstablished` (Integer, optional) - Năm thành lập tối thiểu
- `maxYearEstablished` (Integer, optional) - Năm thành lập tối đa
- `sort` (String, optional) - Sắp xếp: "asc", "desc", "rating", "jobs_posted", "created_at"
- `page` (Integer, default=1) - Số trang
- `size` (Integer, default=10) - Số kết quả mỗi trang

### 2. POST /api/employers/search
Tìm kiếm employers sử dụng JSON request body.

**Request Body:**
```json
{
    "name": "ABC Company",
    "location": "Hanoi",
    "organizationId": 1,
    "keyword": "technology",
    "isPremium": true,
    "verified": 1,
    "minRating": 4.0,
    "minJobsPosted": 5,
    "teamSize": "50-100",
    "minYearEstablished": 2010,
    "maxYearEstablished": 2023,
    "sort": "rating",
    "page": 1,
    "size": 20
}
```

## Response Format

```json
{
    "data": [
        {
            "id": 1,
            "email": "employer@company.com",
            "fullName": "John Doe",
            "companyName": "ABC Technology",
            "location": "Hanoi",
            "description": "Leading technology company...",
            "website": "https://abc-tech.com",
            "phone": "0123456789",
            "avatarUrl": "https://...",
            "banner": "https://...",
            "teamSize": "50-100",
            "yearOfEstablishment": 2015,
            "isPremium": true,
            "verified": 1,
            "role": {
                "id": 2,
                "name": "EMPLOYER"
            },
            "organization": {
                "id": 1,
                "name": "Technology"
            },
            "totalJobsPosted": 25,
            "averageRating": 4.5,
            "totalReviews": 18
        }
    ],
    "totalHits": 50,
    "page": 1,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false
}
```

## Search Examples

### 1. Tìm kiếm theo tên công ty
```
GET /api/employers/search?name=ABC Technology
```

### 2. Tìm kiếm employers ở Hanoi với rating cao
```
GET /api/employers/search?location=Hanoi&minRating=4.0&sort=rating
```

### 3. Tìm kiếm employers premium đã đăng nhiều job
```
GET /api/employers/search?isPremium=true&minJobsPosted=10&sort=jobs_posted
```

### 4. Tìm kiếm theo keyword chung
```
GET /api/employers/search?keyword=technology&verified=1
```

### 5. Tìm kiếm theo tổ chức và quy mô team
```
GET /api/employers/search?organizationId=1&teamSize=50-100
```

### 6. Tìm kiếm theo khoảng năm thành lập
```
GET /api/employers/search?minYearEstablished=2010&maxYearEstablished=2020
```

## Search Features

### Multi-field Search
- **Name search**: Tìm kiếm đồng thời trong `fullName` và `companyName`
- **Keyword search**: Tìm kiếm chung trong `fullName`, `companyName`, `description`, `location`

### Advanced Filtering
- **Range queries**: Rating, số jobs posted, năm thành lập
- **Exact match**: Premium status, verification status, team size
- **Organization filtering**: Lọc theo loại tổ chức

### Sorting Options
- `asc`/`desc`: Sắp xếp theo ngày tạo
- `rating`: Sắp xếp theo rating giảm dần
- `jobs_posted`: Sắp xếp theo số job đã đăng
- `created_at`: Sắp xếp theo ngày tạo

### Pagination
- Hỗ trợ phân trang với `page` và `size`
- Response bao gồm `hasNext` và `hasPrevious` để điều hướng

## Role Filtering
Service tự động lọc chỉ những users có role EMPLOYER (roleId = 2).

## Integration Notes
- Cần có Elasticsearch running và indexed với UserDocument
- UserDocumentMapper cần được configure để resolve các SimpleNameResponse
- Service phụ thuộc vào ElasticsearchClient được configure trong ElasticsearchConfig
