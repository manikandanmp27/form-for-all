# FormForAll: AI-Powered Form & Document Accessibility Co-Pilot

FormForAll is an intelligent web application designed to break down barriers when filling out complex government, banking, healthcare, and administrative forms. It combines client-side OCR, Gemini Vision AI, layout-preserving multi-language translation, cognitive accessibility profiles, and an interactive AI chatbot assistant into a seamless experience.

---

## Overview

Filling out official documents can be overwhelming due to complicated terminology, foreign regional languages, small font sizes, or privacy concerns regarding sensitive data. 

**FormForAll** solves this problem by acting as an AI Co-Pilot that:
* **Ingests & Auto-Fills Documents**: Converts scanned paper forms or digital PDFs into clean, step-by-step interactive inputs.
* **Translates Document Layouts**: Overlays translated text in 10 regional Indian languages directly onto document images like Google Lens, preserving 100% of form borders, tables, and logos.
* **Adapts to User Needs**: Dynamically customizes font sizes, high-contrast modes, OpenDyslexic typography, TTS audio reading, and simple language explanations.
* **Protects User Privacy**: Evaluates sensitive inputs (such as Aadhaar or bank account numbers) and flags privacy risks before submission.
* **Guides Users via AI Chat**: Provides an embedded AI chatbot assistant capable of answering questions in real-time based on the active form context.

---

## Features

* **Intelligent Document Ingestion**: Upload PDF or image (PNG/JPG) forms. Uses client-side Tesseract.js OCR and backend Gemini Vision AI to detect and structure input fields automatically.
* **Visual Layout-Preserving Translator**: Translates form documents into 10 regional languages (*English, Kannada, Hindi, Tamil, Telugu, Malayalam, Marathi, Bengali, Gujarati, Punjabi*) while preserving the exact original document layout.
* **Personalized Accessibility Profiles**: Tailors the interface according to user preferences including contrast themes, font scaling, OpenDyslexic font toggles, audio text-to-speech, and focus mode.
* **Privacy & Risk Evaluator**: Scans form questions to identify high-risk sensitive data fields and displays clear warning indicators before submission.
* **Context-Aware AI Chatbot Assistant**: Embedded drawer assistant that understands the current form session and provides immediate guidance for tricky form questions.
* **PDF Generation & Submission Review**: Generates clean, completed PDF documents ready for download alongside a comprehensive review summary.
* **JWT Authentication**: User registration, login, token-based session management, and profile preference persistence.

---

## Tech Stack

### Frontend
* **Core Framework**: React 19, React Router DOM v7
* **Build System**: Vite 8
* **Styling**: TailwindCSS v4
* **Icons**: Lucide React
* **Client-Side OCR**: Tesseract.js
* **HTTP Client**: Axios

### Backend
* **Language & Framework**: Java 21 / 24, Spring Boot 3.3.3
* **Security & Auth**: Spring Security, JJWT (io.jsonwebtoken 0.12.6)
* **Data Access**: Spring Data JPA
* **Document Processing**: Apache PDFBox 3.0.3, Jsoup
* **Environment Configuration**: Dotenv Java

### Database & Storage
* **Development Database**: H2 (In-memory database with `/h2-console` enabled)
* **File Storage**: Local file system storage for uploaded and generated document layouts

### AI Engines
* **Vision AI**: Google Gemini Vision (`gemini-2.5-flash`, `gemini-2.0-flash`)
* **Text Extraction**: Tesseract OCR & Apache PDFBox

---

## Project Structure

```
form-for-all/
├── backend/                  # Spring Boot REST API Application
│   ├── src/main/java/com/fillforme/backend/
│   │   ├── ai/              # Gemini Vision AI service & risk evaluator
│   │   ├── auth/            # JWT authentication & user account controllers
│   │   ├── common/          # Security configuration & exception handling
│   │   ├── conversation/    # AI Chatbot assistant service & REST controllers
│   │   ├── document/        # PDFBox parser, OCR, file storage controllers
│   │   ├── export/          # PDF document generation & export controllers
│   │   ├── form/            # Form template & session management
│   │   ├── profile/         # Accessibility profile entities & services
│   │   ├── risk/            # Sensitivity risk analysis engine
│   │   └── translation/     # Layout-preserving document translation service
│   ├── src/main/resources/  # Application properties yml configuration
│   ├── pom.xml              # Maven dependencies & build configuration
│   └── .env.example         # Template for backend environment variables
├── frontend/                 # React + Vite Web Application
│   ├── src/
│   │   ├── api/             # Axios API client modules for backend endpoints
│   │   ├── components/      # Reusable UI components (Navbar, ProfileBar, AIChatbotDrawer)
│   │   ├── context/         # AuthContext & AccessibilityContext providers
│   │   ├── pages/           # Application views (Dashboard, NewForm, LiveSession, FormTranslation)
│   │   └── utils/           # Tesseract OCR & formatting helper utilities
│   ├── package.json         # Frontend dependencies & scripts
│   └── vite.config.js       # Vite dev server configuration & API proxying
└── README.md                # Project documentation
```

---

## Getting Started

### Prerequisites
* **Node.js**: v18.0.0 or higher
* **Java Development Kit (JDK)**: Java 21 or higher
* **Maven**: (Maven Wrapper `./mvnw` is included in the project)

---

### Installation & Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/manikandanmp27/form-for-all.git
cd form-for-all
```

#### 2. Backend Setup
1. Navigate to the `backend` directory:
   ```bash
   cd backend
   ```
2. Copy the example environment file:
   ```bash
   cp .env.example .env
   ```
3. Configure your `.env` file with your Gemini API key:
   ```env
   AI_PROVIDER=gemini
   AI_API_KEY=your_gemini_api_key_here
   AI_MODEL=gemini-2.5-flash
   JWT_SECRET=YourSuperSecretJwtSigningKeyForFillForMeBackend987654321
   ```
4. Start the backend server:
   ```bash
   ./mvnw spring-boot:run
   ```
   *The backend REST API will run on `http://localhost:8080`.*
   *The H2 database console is available at `http://localhost:8080/h2-console`.*

#### 3. Frontend Setup
1. Open a new terminal window and navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
   *The web application will run on `http://localhost:5173`.*

---

## How It Works

```
[ User Uploads Form / Selects Template ]
                   │
                   ▼
       [ React Frontend App ] 
   (Client Tesseract OCR & Preview)
                   │
                   ▼ (REST API / JSON / Multipart)
      [ Spring Boot Backend ]
                   │
         ┌─────────┴─────────┐
         ▼                   ▼
 [ Gemini Vision AI ]  [ Apache PDFBox ]
(Field Extraction &    (PDF Generation &
 Layout Translation)    Parsing)
         │                   │
         └─────────┬─────────┘
                   ▼
    [ Database / Storage Service ]
(H2 DB Persistence & File Serving)
                   │
                   ▼
  [ Interactive Form & Overlay Output ]
```

1. **Upload & Parse**: The user uploads a form image or PDF. The system extracts questions using client-side OCR and backend Gemini Vision AI.
2. **Personalized Assistance**: Form questions are presented according to the user's accessibility profile. High-risk inputs are flagged.
3. **AI Chat & Guidance**: If confused, the user opens the embedded AI Chatbot for instant context-aware help.
4. **Layout Translation**: Document forms can be rendered with translated text overlays right over the original image, preserving the document structure.
5. **Review & Export**: The completed form can be reviewed and downloaded as a formatted PDF.

---

## API Overview

| Endpoint Area | Method | Description |
| :--- | :--- | :--- |
| `/api/auth/register` | `POST` | Register a new user account |
| `/api/auth/login` | `POST` | Authenticate user and return JWT bearer token |
| `/api/auth/me` | `GET` | Retrieve current authenticated user profile |
| `/api/forms` | `GET / POST` | List template forms or create form session from uploaded file |
| `/api/sessions/{sessionId}` | `GET / POST` | Retrieve active form session or save user answers |
| `/api/profile` | `GET / PUT` | Fetch or update user accessibility profile preferences |
| `/api/chat` | `POST` | Send message to context-aware AI chatbot assistant |
| `/api/translation/translate-form` | `POST` | Perform Google Lens style layout-preserving document translation |
| `/api/translation/languages` | `GET` | Fetch list of supported regional translation languages |
| `/api/export/files/{filename}` | `GET` | Serve stored form images and translated document files |
| `/api/sessions/{sessionId}/export` | `POST` | Generate completed PDF document for active session |

---

## Development Commands

### Frontend (`/frontend`)
* **Start Dev Server**: `npm run dev`
* **Production Build**: `npm run build`
* **Linting**: `npx oxlint`
* **Preview Build**: `npm run preview`

### Backend (`/backend`)
* **Start Application**: `./mvnw spring-boot:run`
* **Compile Project**: `./mvnw clean compile`
* **Run Tests**: `./mvnw test`
* **Package JAR**: `./mvnw clean package`

---

## Security & Important Notes

* **API Key Protection**: Never commit your real `AI_API_KEY` or `JWT_SECRET` to source control. Use the `.env` file locally.
* **File Upload Boundaries**: Maximum file upload size is set to 15MB for form images and PDFs.
* **CORS & Proxying**: Frontend Vite dev server proxies `/api` requests to backend on port 8080 seamlessly during development.
