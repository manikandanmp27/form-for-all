# REST API Specification

This document details the actual REST API endpoints implemented in the Spring Boot backend (`com.fillforme.backend`).

---

## Endpoint Summary Table

| Method | Endpoint | Purpose | Auth Required |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register a new user account | No |
| `POST` | `/api/auth/login` | Authenticate user and return JWT token | No |
| `GET` | `/api/auth/me` | Fetch authenticated user profile | Yes |
| `POST` | `/api/forms` | Create a form session from file upload or URL | Optional |
| `POST` | `/api/forms/extract-values` | Extract field labels and values from document image via Gemini Vision AI | Optional |
| `GET` | `/api/forms` | List all form sessions for current user | Optional |
| `GET` | `/api/forms/{sessionId}` | Get form session details and extracted fields | Optional |
| `POST` | `/api/translation/translate-form` | Layout-preserving document translation via Gemini Vision AI | Optional |
| `GET` | `/api/translation/languages` | Get list of supported regional languages | No |
| `POST` | `/api/chat/ask` | Send question to context-aware AI chatbot | No |
| `GET` | `/api/profile` | Get accessibility profile preferences | Optional |
| `PUT` | `/api/profile` | Update accessibility profile preferences | Optional |
| `GET` | `/api/forms/{sessionId}/risk-analysis` | Get security risk audit score and flagged fields | Optional |
| `GET` | `/api/forms/{sessionId}/export/pdf` | Export completed form as downloadable PDF | Optional |
| `GET` | `/api/export/files/{filename}` | Stream translated document images | No |

---

## Detailed Endpoint Specifications

### 1. Authentication APIs

#### `POST /api/auth/register`
* **Purpose**: Register a new user account.
* **Authentication**: Not required.
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!",
  "fullName": "Jane Doe",
  "preferredLanguage": "en"
}
```
* **Response `(200 OK)`**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "email": "user@example.com",
    "fullName": "Jane Doe",
    "preferredLanguage": "en"
  }
}
```

#### `POST /api/auth/login`
* **Purpose**: Authenticate user and return JWT bearer token.
* **Authentication**: Not required.
* **Request Body**:
```json
{
  "email": "user@example.com",
  "password": "SecurePassword123!"
}
```
* **Response `(200 OK)`**: Same as register response containing `token` and `user`.

---

### 2. Form & Document APIs

#### `POST /api/forms`
* **Purpose**: Ingest a form from multipart file upload or web URL.
* **Content-Type**: `multipart/form-data` or `application/json`
* **Multipart Parameters**:
  - `file`: MultipartFile (PDF/PNG/JPG)
  - `title` *(optional)*: String
  - `customFields` *(optional)*: JSON String array
* **JSON Body (URL Ingestion)**:
```json
{
  "formUrl": "https://example.com/sample-form",
  "formTitle": "Sample Application Form"
}
```
* **Response `(201 Created)`**:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "formTitle": "Sample Application Form",
  "status": "PROCESSING",
  "progressPercentage": 0,
  "fields": [
    {
      "id": "f1e2d3c4-b5a6-7890-abcd-ef1234567890",
      "fieldKey": "applicantFullName",
      "label": "Full Name",
      "fieldType": "TEXT",
      "required": true,
      "orderIndex": 1,
      "defaultHelpText": "Please enter your full legal name as it appears on official IDs."
    }
  ]
}
```

#### `POST /api/forms/extract-values`
* **Purpose**: Extract field labels and actual written/printed values from uploaded document photos using Gemini 2.5 Flash Vision AI.
* **Content-Type**: `multipart/form-data`
* **Parameters**: `file` (MultipartFile)
* **Response `(200 OK)`**:
```json
[
  {
    "fieldKey": "applicantName",
    "label": "Full Name",
    "extractedValue": "Manikandan",
    "fieldType": "TEXT",
    "required": true,
    "orderIndex": 1,
    "defaultHelpText": "Please enter Full Name"
  }
]
```

---

### 3. Translation APIs

#### `POST /api/translation/translate-form`
* **Purpose**: Layout-preserving form document translation using Gemini Vision AI and Java `Graphics2D` coordinate rendering.
* **Content-Type**: `multipart/form-data`
* **Parameters**:
  - `file`: MultipartFile (Document Image)
  - `targetLanguage`: String (`kn`, `hi`, `ta`, `te`, `ml`, `mr`, `bn`, `gu`, `pa`, `en`)
* **Response `(200 OK)`**:
```json
{
  "filename": "translated_1787376652140_sir.jpeg",
  "imageUrl": "/api/export/files/translated_1787376652140_sir.jpeg",
  "targetLanguage": "en",
  "originalTextRegionsCount": 31
}
```

#### `GET /api/translation/languages`
* **Purpose**: Returns available regional languages.
* **Response `(200 OK)`**:
```json
[
  { "code": "en", "name": "English" },
  { "code": "kn", "name": "Kannada (ಕನ್ನಡ)" },
  { "code": "hi", "name": "Hindi (हिंदी)" },
  { "code": "ta", "name": "Tamil (தமிழ்)" },
  { "code": "te", "name": "Telugu (తెలుగు)" },
  { "code": "ml", "name": "Malayalam (മലയാളം)" },
  { "code": "mr", "name": "Marathi (मराठी)" },
  { "code": "bn", "name": "Bengali (বাংলা)" },
  { "code": "gu", "name": "Gujarati (ગુજરાતી)" },
  { "code": "pa", "name": "Punjabi (ਪੰਜਾਬੀ)" }
]
```

---

### 4. AI Chatbot API

#### `POST /api/chat/ask`
* **Purpose**: Query the context-aware AI chatbot assistant.
* **Request Body**:
```json
{
  "message": "What documents do I need for a passport application?",
  "sessionId": "optional-form-session-id"
}
```
* **Response `(200 OK)`**:
```json
{
  "reply": "For a fresh passport application, you typically require:\n1. Proof of Address (Aadhaar / Utility Bill)\n2. Proof of Date of Birth (Birth Certificate / PAN Card)\n3. Photo ID Proof",
  "timestamp": "2026-08-22T12:00:00Z"
}
```

---

### 5. Accessibility Profile APIs

#### `GET /api/profile`
* **Response `(200 OK)`**:
```json
{
  "fontScale": 1.15,
  "highContrast": true,
  "dyslexiaFont": false,
  "voiceSpeed": 1.0,
  "autoReadHelp": true,
  "simplifiedLanguage": true,
  "preferredLanguage": "en",
  "screenReaderMode": false
}
```

#### `PUT /api/profile`
* **Request Body**: JSON containing updated profile fields.
* **Response `(200 OK)`**: Updated `AccessibilityProfile` object.
