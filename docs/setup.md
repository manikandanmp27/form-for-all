# Developer Local Setup Guide

This guide provides step-by-step instructions for cloning, setting up, and running **Fill-For-Me (FormForAll)** locally.

---

## 1. Prerequisites

Ensure you have the following installed on your machine:

* **Node.js**: `v18.0.0` or higher (Recommended: `v20.x`)
* **npm**: `v9.x` or higher
* **Java Development Kit (JDK)**: `v21` or `v24`
* **Maven**: `v3.9+` (or use the repository's included `./mvnw` wrapper)
* **Git**: `v2.x+`

---

## 2. Clone Repository

```bash
git clone https://github.com/manikandanmp27/form-for-all.git
cd form-for-all
```

---

## 3. Backend Setup (Spring Boot)

1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```

2. Configure environment variables in `backend/.env`:
   ```env
   SERVER_PORT=8080
   JWT_SECRET=FillForMeSuperSecretJwtSigningKey98765432101234567890ForAccessibilityCoPilot
   AI_PROVIDER=gemini
   AI_MODEL=gemini-2.5-flash
   AI_API_KEY=YOUR_GEMINI_VISION_API_KEY
   ```

3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   *For Windows Command Prompt / PowerShell:*
   ```cmd
   mvnw.cmd spring-boot:run
   ```

4. Verify backend is running:
   - API Base URL: `http://localhost:8080/api`
   - H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:fillformedb`, User: `SA`, Password: empty)

---

## 4. Frontend Setup (React 19 + Vite 8)

1. Open a new terminal tab and navigate to `frontend/`:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Configure environment variables in `frontend/.env` *(optional for local dev)*:
   ```env
   VITE_API_BASE_URL=http://localhost:8080/api
   ```

4. Start Vite development server:
   ```bash
   npm run dev
   ```

5. Open browser at: **`http://localhost:5173`**

---

## 5. Environment Variables Reference

| Variable | Location | Description | Default / Example |
| :--- | :--- | :--- | :--- |
| `AI_API_KEY` | `backend/.env` | Google Gemini GenerativeLanguage API key | `AQ.Ab8RN6K...` |
| `AI_PROVIDER` | `backend/.env` | AI provider selector (`gemini` / `openai`) | `gemini` |
| `AI_MODEL` | `backend/.env` | Primary Gemini model ID | `gemini-2.5-flash` |
| `JWT_SECRET` | `backend/.env` | Secret key for signing JWT tokens | `FillForMeSuperSecret...` |
| `VITE_API_BASE_URL` | `frontend/.env` | Backend API URL for frontend Axios client | `/api` or `http://localhost:8080/api` |

*Note: Never commit real API keys or JWT secrets to Git.*

---

## 6. Common Troubleshooting

* **Backend Port 8080 Already in Use**:
  Kill existing Java processes or change `SERVER_PORT=8081` in `backend/.env`.
* **503 / 404 Gemini API Model Error**:
  Ensure `AI_API_KEY` is valid and active in `backend/.env`. The backend automatically falls back across `gemini-2.5-flash`, `gemini-1.5-flash`, and `gemini-1.5-pro`.
* **CORS Blocked Errors**:
  `SecurityConfig.java` is pre-configured for `http://localhost:5173` and `http://localhost:3000`. If using a custom port, add it to `setAllowedOriginPatterns` in `SecurityConfig.java`.
