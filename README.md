# GitHub Access Report

A Spring Boot REST API that generates a report showing which users have access to which repositories within a given GitHub organization.

---

## How to Run

### Prerequisites
- Java 17
- Maven
- A GitHub Personal Access Token (classic)

### Steps

1. Clone the repository
```bash
git clone https://github.com/Navyaa18/github-access-report.git
cd github-access-report
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

App will start on `http://localhost:8080`

---

## How Authentication is Configured

This service uses **GitHub Personal Access Token (Classic)** for authentication.

The token is **not stored** on the server — it is passed by the caller in every request via the `Authorization` header.

### How to generate a GitHub Token:
1. Go to GitHub → Settings → Developer Settings → Personal Access Tokens → **Tokens (classic)**
2. Click **Generate new token (classic)**
3. Select the following permissions:
   - ✅ `repo`
   - ✅ `read:org`
4. Copy the generated token (starts with `ghp_`)

---

## How to Call the API

### Endpoint
```
GET /api/access-report?org={organization_name}
```

### Headers
```
Authorization: Bearer ghp_yourtoken
```

### Example — curl
```bash
curl -X GET "http://localhost:8080/api/access-report?org=your_org_name" \
     -H "Authorization: Bearer ghp_yourtoken"
```

### Example — Postman
- Method: `GET`
- URL: `http://localhost:8080/api/access-report?org=your_org_name`
- Header Key: `Authorization`
- Header Value: `Bearer ghp_yourtoken`

### Sample Response
```json
{
    "Navyaa18": [
        {"repo": "navya1", "role": "ADMIN"}
    ],
    "ABHIXIT2": [
        {"repo": "navya1", "role": "WRITE"}
    ]
}
```

---

## Project Structure

```
src/main/java/com/githubaccess/
├── GithubAccessReportApplication.java   → App entry point
├── client/
│   └── GitHubClient.java                → GitHub GraphQL API calls with pagination
├── config/
│   ├── RestTemplateConfig.java          → RestTemplate bean
│   └── GitHubConstants.java             → All constants (URLs, endpoints, headers)
├── controller/
│   └── AccessReportController.java      → REST endpoint, extracts token from header
├── dto/
│   └── RepoAccessDto.java               → Response DTO (repo name + role)
└── service/
    └── AccessReportService.java         → Business logic
```

---

## Design Decisions

### 1. Token via Request Header
The GitHub token is passed in the `Authorization` header per request instead of being stored in `application.properties`. This avoids hardcoding secrets and allows different callers to use their own tokens.

### 2. GraphQL over REST API
With REST API, every repo requires a separate collaborators call:
- 100 repos → 101 API calls
- 500 repos → 501 API calls

This quickly hits GitHub's rate limit (5000 requests/hour) at scale.

Instead, the service uses **GitHub's GraphQL API** to fetch all repos and their collaborators in a **single API call**, regardless of how many repos the organization has. This is far more efficient and rate-limit friendly.

### 3. Cursor-based Pagination
GitHub API returns a maximum of 100 results per page. The client uses cursor-based pagination (`pageInfo.hasNextPage` + `endCursor`) to loop through all pages and fetch all repositories automatically.

### 4. No Database
The report is generated in real-time by fetching live data from GitHub API. No database is needed as the problem requires a fresh access report, not historical data.

### 5. Error Handling
- Invalid token → returns `Invalid GitHub token` error
- Organization not found → returns `Organization not found` error

---

## Assumptions

- The token provided must belong to a user who is an **owner or member** of the organization
- Only **direct collaborators** are included in the report (not team-based access)
- The API returns users who have **accepted** their collaboration invitation
