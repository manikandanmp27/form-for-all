# Product Features Documentation

This document describes the major product features implemented in **Fill-For-Me (FormForAll)**, detailing their purpose, user flow, technical implementation, and current status.

---

### 1. Fill-For-Me Conversational Form Filling

* **Purpose**: Provide a step-by-step interactive form completion wizard that replaces dense paper/PDF forms with accessible digital inputs.
* **User Flow**:
  1. User ingests a form via URL or File Upload on the Dashboard (`/dashboard`).
  2. The system generates an interactive session (`/forms/:sessionId`).
  3. User completes fields sequentially with assistance, audio guidance, or voice input.
  4. User reviews answers and exports the final filled document or PDF.
* **Implementation**: Backend `FormServiceImpl.java` parses form fields and creates `FormSession`. Frontend `FormFillingPage.jsx` renders dynamic controls per field type (`TEXT`, `DATE`, `EMAIL`, `PHONE`, `SELECT`, `CHECKBOX`).
* **Current Status**: **Implemented**

---

### 2. Layout-Preserving Google Lens Form Translator

* **Purpose**: Translate printed form documents across 10 Indian regional languages while retaining 100% of the original document's visual structure, lines, and boxes.
* **User Flow**:
  1. User navigates to `/translate`.
  2. User uploads a form image (Kannada, Hindi, Tamil, Telugu, Malayalam, Marathi, Bengali, Gujarati, Punjabi, or English).
  3. User selects the target translation language.
  4. System processes the image and presents a side-by-side split screen showing the original image next to the layout-preserved translated image.
* **Implementation**: `RuleAndLlmAIService.java` calls Gemini 2.5 Flash Vision AI to extract line coordinates (`xPercent`, `yPercent`, `widthPercent`, `heightPercent`) and translated text. `DocumentLayoutRenderer.java` uses Java `Graphics2D` to erase original text and overlay translated text at exact pixel coordinates.
* **Current Status**: **Implemented**

---

### 3. Vision AI Document Auto-Fill from Image / ID Card

* **Purpose**: Automatically populate form fields from photos of official documents or ID cards (Aadhaar, Passport, Driving License, Voter ID, Mark Sheet, Bank Statement).
* **User Flow**:
  1. Inside an active form session (`/forms/:sessionId`), user clicks **⚡ Auto-Fill from Document**.
  2. User uploads a document photo or captures one via webcam.
  3. Vision AI scans the image and extracts field labels and actual values.
  4. User reviews and edits extracted values in a review modal, then clicks **Apply to Form** to auto-fill fields.
* **Implementation**: `FormController.java` (`POST /api/forms/extract-values`) sends file bytes to Gemini Vision AI. `SnapToFormModal.jsx` receives extracted fields and maps them dynamically to form fields with Tesseract.js client OCR as an offline fallback.
* **Current Status**: **Implemented**

---

### 4. Accessibility Profile Memory

* **Purpose**: Persist user-specific accessibility preferences across sessions to automatically adapt UI styling and behavior.
* **User Flow**:
  1. User opens the top navigation profile selector or visits `/profile`.
  2. User selects or customizes options: Low Vision (High Contrast), Dyslexia Friendly Fonts, Motor Impairment (Large Targets), Voice Speed, or Screen Reader mode.
  3. Settings apply instantly across the entire application and persist in backend storage.
* **Implementation**: `AccessibilityProfileController.java` (`GET/PUT /api/profile`) manages backend persistence. Frontend `AccessibilityContext.jsx` applies global CSS variables (`--font-scale`, `--contrast-ratio`, `--target-padding`, `--font-family`).
* **Current Status**: **Implemented**

---

### 5. Context-Aware AI Chatbot

* **Purpose**: Provide real-time answers about form eligibility rules, required documents, legal jargon, and step-by-step completion advice.
* **User Flow**:
  1. User clicks the floating Chatbot widget on the bottom right of any page or opens `/chat`.
  2. User types a question or selects quick prompt chips.
  3. AI returns clear, structured markdown responses with guidance.
* **Implementation**: `ChatbotController.java` (`POST /api/chat/ask`) processes queries with prompt context. Frontend `ChatbotWidget.jsx` renders markdown responses.
* **Current Status**: **Implemented**

---

### 6. Regional Language + Voice Co-Pilot

* **Purpose**: Assist users who are visually impaired or low-literacy through audio field reading and voice input.
* **User Flow**:
  1. User clicks **Listen Field** on any field to hear field labels and guidance spoken aloud.
  2. User clicks **Voice Input (Microphone)** to speak their answer, which populates into the field.
* **Implementation**: Uses native browser Web Speech API (`window.speechSynthesis` and `SpeechRecognition`). Spoken guidance text is provided by `defaultHelpText` in `FormField`.
* **Current Status**: **Implemented**

---

### 7. Cognitive-Load Mode & Dynamic Contrast

* **Purpose**: Reduce mental fatigue and improve visual clarity for neurodivergent and elderly users.
* **User Flow**:
  1. Toggling Cognitive-Load mode simplifies page layouts, hides non-essential sidebars, and highlights the current active field.
* **Implementation**: Controlled via `AccessibilityContext.jsx` state and CSS rule toggles (`body.simplified-mode`).
* **Current Status**: **Implemented**

---

### 8. Risk-Aware Flagging & Privacy Audit

* **Purpose**: Protect users from phishing and excessive personal data collection.
* **User Flow**:
  1. Inside a form session, user opens the **Privacy & Risk Audit Drawer**.
  2. System displays a calculated Security Score and lists flagged fields (e.g. asking for passwords, bank PINs, or unencrypted sensitive data).
* **Implementation**: `RiskController.java` (`GET /api/forms/{sessionId}/risk-analysis`) and `RiskAnalysisServiceImpl.java` evaluate form fields against privacy risk rules.
* **Current Status**: **Implemented**
