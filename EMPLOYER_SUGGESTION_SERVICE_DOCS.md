# Employer Suggestion Service Documentation

## Overview
`EmployerSuggestionService` cung cấp các chức năng gợi ý (auto-complete/suggestion) cho việc tìm kiếm employers, tương tự như `JobSuggestionService` nhưng được tối ưu hóa cho dữ liệu employers.

## API Endpoints

### 1. GET /api/employers/suggest/company
Gợi ý tên công ty dựa trên keyword nhập vào.

**Parameters:**
- `keyword` (String, required) - Từ khóa để tìm gợi ý company name

**Example:**
```
GET /api/employers/suggest/company?keyword=tech
```

**Response:**
```json
[
    "Tech Solutions Ltd",
    "TechCorp Vietnam",
    "Advanced Technology Company",
    "FinTech Innovations"
]
```

### 2. GET /api/employers/suggest/location
Gợi ý địa điểm dựa trên keyword từ dữ liệu employers.

**Parameters:**
- `keyword` (String, required) - Từ khóa để tìm gợi ý location

**Example:**
```
GET /api/employers/suggest/location?keyword=ha
```

**Response:**
```json
[
    "Hanoi",
    "Hai Phong", 
    "Ha Nam",
    "Hau Giang"
]
```

### 3. GET /api/employers/suggest/name
Gợi ý tên đầy đủ của employers dựa trên keyword.

**Parameters:**
- `keyword` (String, required) - Từ khóa để tìm gợi ý full name

**Example:**
```
GET /api/employers/suggest/name?keyword=john
```

**Response:**
```json
[
    "John Smith",
    "John Doe",
    "John Wilson",
    "Johnson Michael"
]
```

### 4. GET /api/employers/suggest
Gợi ý tổng hợp tìm kiếm trong company name, full name, và location.

**Parameters:**
- `keyword` (String, required) - Từ khóa để tìm gợi ý chung

**Example:**
```
GET /api/employers/suggest?keyword=soft
```

**Response:**
```json
[
    "Software Development Co",
    "Microsoft Vietnam",
    "Soft Solutions Ltd",
    "Ho Chi Minh City Software Park"
]
```

## Service Methods

### 1. suggestCompanyNames(String keyword)
- Tìm kiếm trong field `companyName`
- Sử dụng `matchPhrasePrefix` query
- Giới hạn 10 kết quả
- Chỉ tìm trong users có `roleId = 2` (EMPLOYER)

### 2. suggestLocations(String keyword)
- Tìm kiếm trong field `location`
- Sử dụng `matchPhrasePrefix` query
- Giới hạn 8 kết quả
- Lọc các location trùng lặp

### 3. suggestFullNames(String keyword)
- Tìm kiếm trong field `fullName`
- Sử dụng `matchPhrasePrefix` query
- Giới hạn 8 kết quả
- Chỉ hiển thị các tên không trùng lặp

### 4. suggestGeneral(String keyword)
- Tìm kiếm đồng thời trong `companyName`, `fullName`, `location`
- Sử dụng `bool` query với `should` clauses
- Giới hạn 15 kết quả từ Elasticsearch, sau đó filter về 10
- Kết quả được sắp xếp theo độ relevance

## Implementation Features

### 🔍 **Query Strategy**
- **Match Phrase Prefix**: Hỗ trợ auto-complete theo từng từ
- **Boolean Query**: Kết hợp nhiều điều kiện tìm kiếm
- **Role Filtering**: Tự động lọc chỉ employers (roleId = 2)

### ⚡ **Performance Optimization**
- **Limited Results**: Giới hạn số lượng kết quả để tăng tốc độ
- **Distinct Filtering**: Loại bỏ các kết quả trùng lặp
- **Field-specific Search**: Tối ưu hóa cho từng loại field

### 🛡️ **Error Handling**
- **Null/Empty Check**: Kiểm tra input trước khi thực hiện query
- **Exception Handling**: Log lỗi và trả về empty list nếu có exception
- **Graceful Degradation**: Không làm crash application khi có lỗi

## Integration Examples

### Frontend Integration (JavaScript)
```javascript
// Company name suggestion
const suggestCompany = async (keyword) => {
    const response = await fetch(`/api/employers/suggest/company?keyword=${keyword}`);
    return await response.json();
};

// General suggestion
const suggestGeneral = async (keyword) => {
    const response = await fetch(`/api/employers/suggest?keyword=${keyword}`);
    return await response.json();
};
```

### Backend Service Usage
```java
@Autowired
private EmployerSuggestionService suggestionService;

// Get company suggestions
List<String> companies = suggestionService.suggestCompanyNames("tech");

// Get location suggestions  
List<String> locations = suggestionService.suggestLocations("hanoi");
```

## Configuration Notes

1. **Elasticsearch Index**: Service sử dụng index "users"
2. **Role ID**: EMPLOYER role được giả định có ID = 2
3. **Field Mapping**: Cần đảm bảo các field `companyName`, `fullName`, `location` được index đúng
4. **Performance**: Có thể tùy chỉnh `size` parameter trong mỗi method để tối ưu performance

## Comparison with JobSuggestionService

| Feature | JobSuggestionService | EmployerSuggestionService |
|---------|---------------------|---------------------------|
| Index | jobs | users |
| Primary Field | title | companyName |
| Role Filter | None | roleId = 2 (EMPLOYER) |
| Suggestion Types | 1 (title only) | 4 (company, location, name, general) |
| Result Limit | 5 | 8-15 depending on type |
