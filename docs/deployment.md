# Production Deployment Guide

This document describes the actual production deployment architecture for **Fill-For-Me (FormForAll)**.

---

## 1. Deployment Architecture Overview

```text
[Vercel: React 19 Frontend SPA]  ──(HTTPS REST API)──►  [Render: Spring Boot Backend Docker Container]
      https://*.vercel.app                                   https://*.onrender.com/api
```

- **Frontend**: Deployed as a static Single Page Application (SPA) on **Vercel**.
- **Backend**: Deployed as a containerized Docker Web Service on **Render** (or Railway / Fly.io).
- **Database**: H2 In-Memory for demonstration or managed PostgreSQL on Render / Supabase.

---

## 2. Frontend Deployment (Vercel)

### Prerequisites
- GitHub repository connected to Vercel account.

### Configuration Steps
1. In Vercel, create a **New Project** and import `manikandanmp27/form-for-all`.
2. Configure settings:
   - **Framework Preset**: `Vite`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
3. Add Environment Variable:
   - `VITE_API_BASE_URL`: `https://<your-render-backend-url>.onrender.com/api`
4. Deploy!

### SPA Routing Rule (`frontend/vercel.json`)
```json
{
  "rewrites": [
    {
      "source": "/(.*)",
      "destination": "/index.html"
    }
  ]
}
```

---

## 3. Backend Deployment (Render Docker Web Service)

### Prerequisites
- Render account connected to GitHub repository.

### Multi-Stage Dockerfile (`Dockerfile` / `backend/Dockerfile`)
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
RUN apk add --no-cache maven
COPY . .
RUN if [ -d "backend" ]; then cd backend; fi && \
    mvn clean package -DskipTests && \
    cp target/*.jar /app/app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Render Service Settings
- **Service Type**: `Web Service`
- **Environment**: `Docker`
- **Root Directory**: `backend` (or `.`)
- **Environment Variables**:
  - `AI_API_KEY`: *(Your Google Gemini API Key)*
  - `AI_PROVIDER`: `gemini`
  - `AI_MODEL`: `gemini-2.5-flash`
  - `JWT_SECRET`: *(Production secure random string)*

---

## 4. Production CORS Configuration

Cross-Origin Resource Sharing (CORS) is configured in `backend/src/main/java/com/fillforme/backend/common/security/SecurityConfig.java`:

```java
configuration.setAllowedOriginPatterns(List.of(
    "http://localhost:3000",
    "http://localhost:5173",
    "http://127.0.0.1:*",
    "https://*.vercel.app",
    "https://*.onrender.com",
    "https://*"
));
```
This permits secure requests from deployed Vercel frontends to the Render backend service.
