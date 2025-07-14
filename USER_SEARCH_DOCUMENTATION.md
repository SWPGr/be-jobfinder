# User/Employer Search Documentation

## Overview
This implementation provides Elasticsearch-based search functionality for Users and Employers in the JobFinder application. It allows for advanced searching and filtering of users based on various criteria.

## Components Created

### 1. UserDocument (Model)
- Elasticsearch document representing user data
- Includes all relevant user and employer information
- Located: `com.example.jobfinder.model.UserDocument`

### 2. UserDocumentRepository
- Spring Data Elasticsearch repository for UserDocument
- Provides basic CRUD and search methods
- Located: `com.example.jobfinder.repository.UserDocumentRepository`

### 3. UserSearchRequest (DTO)
- Extended the existing request DTO with additional search parameters
- Includes filters for employers: rating, jobs posted, company info, etc.
- Located: `com.example.jobfinder.dto.user.UserSearchRequest`

### 4. UserSearchResponse (DTO)
- Response DTO for search results with pagination info
- Located: `com.example.jobfinder.dto.user.UserSearchResponse`

### 5. UserDocumentMapper
- Maps between User entities and UserDocument
- Calculates employer metrics (jobs posted, average rating)
- Located: `com.example.jobfinder.mapper.UserDocumentMapper`

### 6. UserElasticsearchSyncService
- Synchronizes user data from database to Elasticsearch
- Scheduled to run daily at 00:05
- Located: `com.example.jobfinder.service.UserElasticsearchSyncService`

### 7. UserSearchService
- Main search service using Elasticsearch
- Supports complex queries and filtering
- Located: `com.example.jobfinder.service.UserSearchService`

### 8. UserSearchController
- REST endpoints for user/employer search
- Located: `com.example.jobfinder.controller.UserSearchController`

## API Endpoints

### General User Search
```
GET /api/users/search
```

Parameters:
- `keyword` - General keyword search
- `companyName` - Company name filter
- `location` - Location filter
- `roleName` - Role filter (JOB_SEEKER, EMPLOYER, ADMIN)
- `isPremium` - Premium status filter
- `verified` - Verification status (0 or 1)
- `minRating` - Minimum average rating
- `minJobsPosted` - Minimum jobs posted
- `teamSize` - Company team size
- `minYearEstablished` - Minimum establishment year
- `maxYearEstablished` - Maximum establishment year
- `sort` - Sort option (asc, desc, rating, jobs_posted, created_at)
- `page` - Page number (default: 1)
- `size` - Results per page (default: 10)

### Employer-Specific Search
```
GET /api/users/employers/search
```
Same parameters as above, but automatically filters for EMPLOYER role only.

### POST Endpoints
```
POST /api/users/search
POST /api/users/employers/search
```
Accept JSON request body with UserSearchRequest structure.

## Example Usage

### Search for premium employers in Hanoi with good ratings:
```
GET /api/users/employers/search?location=Hanoi&isPremium=true&minRating=4.0&sort=rating
```

### Search for companies established after 2010 with at least 5 jobs posted:
```
GET /api/users/employers/search?minYearEstablished=2010&minJobsPosted=5&sort=jobs_posted
```

### General keyword search across all user fields:
```
GET /api/users/search?keyword=technology&roleName=EMPLOYER
```

## Setup Instructions

1. **Elasticsearch Index**: The system will automatically create a "users" index when first run.

2. **Data Synchronization**: 
   - Manual sync: Call `UserElasticsearchSyncService.syncAllUsers()`
   - Automatic sync: Runs daily at 00:05 AM
   - Individual sync: Call `UserElasticsearchSyncService.syncUser(user)` or `syncUserById(userId)`

3. **Initial Data Load**: After deployment, run the sync service to populate the Elasticsearch index:
   ```java
   @Autowired
   private UserElasticsearchSyncService syncService;
   
   // In a startup component or manual endpoint
   syncService.syncAllUsers();
   ```

## Features

### Search Capabilities
- **Multi-field search**: Keyword search across name, company, description, location
- **Exact matching**: Role, verification status, premium status
- **Range queries**: Rating, jobs posted, establishment year
- **Sorting options**: By creation date, rating, jobs posted
- **Pagination**: Full pagination support

### Employer-Specific Metrics
- Jobs posted count
- Average rating from employer reviews
- Total number of reviews

### Performance
- Elasticsearch provides fast full-text search
- Indexed fields for optimal query performance
- Efficient pagination

## Configuration

Make sure your `application.properties` includes Elasticsearch configuration:

```properties
spring.elasticsearch.uris=your_elasticsearch_url
spring.elasticsearch.username=your_username
spring.elasticsearch.password=your_password
```

## Notes

- The system automatically calculates employer metrics during sync
- UserDocument includes all relevant fields from User and UserDetail entities
- Search results include calculated fields like average rating and job counts
- The sync service handles both full synchronization and individual user updates
