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
git clone https://github.com/your-username/github-access-report.git
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
    "user1": ["repo-frontend", "repo-backend"],
    "user2": ["repo-backend", "repo-database"],
    "user3": ["repo-frontend"]
}
```

---

## Project Structure

```
src/main/java/com/githubaccess/
├── GithubAccessReportApplication.java   → App entry point
├── client/
│   └── GitHubClient.java                → GitHub API calls with pagination
├── config/
│   ├── RestTemplateConfig.java          → RestTemplate bean
│   └── GitHubConstants.java             → API constants
├── controller/
│   └── AccessReportController.java      → REST endpoint
└── service/
    └── AccessReportService.java         → Business logic
```

---

## Design Decisions

### 1. Token via Request Header
The GitHub token is passed in the `Authorization` header per request instead of being stored in `application.properties`. This avoids hardcoding secrets and allows different callers to use their own tokens.

### 2. Concurrent API Calls
For organizations with 100+ repositories, fetching collaborators sequentially would be very slow. The service uses `CompletableFuture` to fetch collaborators for all repositories in parallel, significantly reducing response time at scale.

### 3. Pagination
GitHub API returns a maximum of 100 results per page. The client handles pagination automatically by looping through all pages until all repos and collaborators are fetched.

### 4. No Database
The report is generated in real-time by fetching live data from GitHub API. No database is needed as the problem requires a fresh access report, not historical data.

### 5. Error Handling
- Invalid token → returns `Invalid GitHub token` error
- Organization not found → returns `Organization not found` error
- Individual repo failures do not fail the entire report

---

## Assumptions

- The token provided must belong to a user who is an **owner or member** of the organization
- Only **direct collaborators** are included in the report (not team-based access)
- The API returns users who have **accepted** their collaboration invitation
