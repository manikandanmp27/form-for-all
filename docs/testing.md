# Testing Strategy & Verification Guide

This document outlines the testing strategy, build verification procedures, and critical test scenarios for **Fill-For-Me (FormForAll)**.

---

## 1. Automated Testing Suite

### Backend Unit & Integration Tests (Spring Boot + JUnit 5)
Backend tests verify authentication, JWT security filters, form ingestion logic, and DTO serializations.

* **Run All Backend Tests**:
  ```bash
  cd backend
  ./mvnw test
  ```
* **Build Verification (Clean Package)**:
  ```bash
  cd backend
  ./mvnw clean package -DskipTests
  ```

### Frontend Build Verification (Vite)
Frontend verification ensures all JSX modules compile cleanly and assets bundle without errors.

* **Run Production Build Check**:
  ```bash
  cd frontend
  npm run build
  ```

---

## 2. Critical Product Test Scenarios

### Scenario 1: Authentication & Token Lifecycle
1. Register a new user via `POST /api/auth/register`.
2. Verify token receipt and storage in local storage (`token`).
3. Access protected route `GET /api/auth/me` with `Authorization: Bearer <token>`.
4. Verify HTTP 401 Unauthorized when accessing protected routes without token.

### Scenario 2: Layout-Preserving Form Translation
1. Navigate to `/translate`.
2. Upload a regional Indian language form photo (e.g. Kannada or Hindi document).
3. Select target language `English`.
4. Click **Translate Form Document**.
5. Verify side-by-side split screen renders original document image on left and translated image on right with preserved line borders and boxes.

### Scenario 3: Vision AI Document Auto-Fill
1. Open an active form session (`/forms/:sessionId`).
2. Click **⚡ Auto-Fill from Document**.
3. Upload an ID card image (Aadhaar / Voter ID / Driver License).
4. Verify backend returns extracted fields (`Full Name`, `Date of Birth`, `Address`).
5. Click **Apply to Form** and verify form fields populate accurately on screen.

### Scenario 4: Accessibility Profile Customization
1. Open top bar profile dropdown or navigate to `/profile`.
2. Select **Low Vision / High Contrast**.
3. Verify CSS root class `body.high-contrast` applies, increasing contrast ratios to `15:1+`.
4. Select **Dyslexia Friendly Font** and verify typography switches to OpenDyslexic / clean high-legibility font stack.

### Scenario 5: Privacy Risk Audit
1. Ingest a form asking for sensitive information (e.g. password or bank PIN).
2. Open Privacy & Risk Audit Drawer.
3. Verify security score flags sensitive fields with risk level `HIGH` / `CRITICAL`.
