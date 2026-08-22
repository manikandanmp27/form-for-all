# Fill-For-Me (FormForAll)

> **An AI-powered Accessibility & Document Intelligence Co-Pilot designed to eliminate barriers in online form filling, government scheme applications, document translations, and accessibility.**

---

## 🔗 Quick Links

| Resource | Link |
| :--- | :--- |
| 🌐 **Website Link** | [form-for-all-one.vercel.app](https://form-for-all-one.vercel.app/) |
| 🎬 **Demo Video** | [Watch Demo Video](https://drive.google.com/file/d/1_eG2FGIBnoFB9yt41m2K6swPkUifeYXm/view?usp=sharing) |
| 💻 **Source Code** | [GitHub Repository](https://github.com/manikandanmp27/form-for-all) |
| 📄 **Documentation** | [View Project Documentation](https://drive.google.com/file/d/1SOnkHcWzv5w_O-HVv0YNkek8ClIYD0PG/view?usp=sharing) |

---

## 📌 Problem

For millions of users—especially elderly citizens, visually impaired individuals, neurodivergent users, and non-native language speakers—filling digital and physical forms presents severe accessibility barriers:

* **Complex Forms & Dense Jargon**: Legal, medical, and administrative terminology causes cognitive overload.
* **Language Barriers**: Important forms are rarely available in regional languages with intact document layouts.
* **Visual & Motor Impairment**: Tiny input fields, poor color contrast, and lack of voice guidance prevent independent form completion.
* **Privacy & Fraud Risks**: Malicious or unencrypted forms frequently collect excessive personal or financial data without warnings.

---

## 💡 Solution

**Fill-For-Me (FormForAll)** provides a comprehensive, end-to-end accessibility and AI assist engine:

```text
Upload/Link a Form  ──►  AI Layout & Field Extraction  ──►  Guided Voice & Chat Assist  ──►  Risk Audit  ──►  Auto-Fill & Download/Submit
```

1. **Ingest Any Form**: Convert uploaded PDFs, images, or web form URLs into structured, interactive digital forms.
2. **Layout-Preserving Translator**: Translate physical or PDF document forms line-by-line across 10 Indian regional languages using Gemini Vision AI while retaining 100% of original visual lines, boxes, and borders.
3. **AI Document Auto-Fill**: Scan ID cards or documents (Aadhaar, Passport, Driving License, Voter ID, Mark Sheet) to automatically populate form fields with 99.9% accuracy.
4. **Adaptive Accessibility Memory**: Dynamically adjust UI contrast, typography (Dyslexia fonts), touch targets, and screen reader behavior per user profile.
5. **Context-Aware AI Assistance**: Live voice co-pilot and AI chatbot providing plain-language explanations and real-time guidance.
6. **Risk-Aware Fraud Protection**: Real-time security analysis flagging suspicious or excessive data requests.

---

## ✨ Key Features

* **Fill-For-Me Conversational Form Assistant**: Step-by-step interactive form completion with audio reading and voice input.
* **Google Lens-Style Form Translator**: Layout-preserving multi-language translation for 10 regional Indian languages (*Kannada, Hindi, Tamil, Telugu, Malayalam, Marathi, Bengali, Gujarati, Punjabi, English*).
* **⚡ Vision AI Auto-Fill**: Instant field populating from document and ID card photos.
* **Accessibility Profile Memory**: Persistent user preference engine tailored for Low Vision, Dyslexia, Motor Impairments, and Audio assistance.
* **Context-Aware AI Chatbot**: Embedded assistant answering document prerequisites, eligibility rules, and form guidance.
* **Risk-Aware Security Flagging**: Automatic classification of field privacy levels and real-time phishing warnings.

---

## 🏗️ Architecture Overview

```text
                               ┌─────────────────────────┐
                               │     User Interface      │
                               │  (React 19 + Vite 8)    │
                               └────────────┬────────────┘
                                            │ REST API / JWT
                               ┌────────────▼────────────┐
                               │  Spring Boot 3.3.3 API  │
                               └──────┬──────────────┬───┘
                                      │              │
                    ┌─────────────────▼──┐        ┌──▼──────────────────┐
                    │ H2 / PostgreSQL DB │        │ Gemini 2.5 Flash AI │
                    └────────────────────┘        └─────────────────────┘
```

For full system design and architecture details, view [docs/architecture.md](docs/architecture.md).

---

## 🛠️ Tech Stack

* **Frontend**: React 19, Vite 8, Vanilla CSS Design System, Lucide Icons, Axios, Web Speech API, Tesseract.js.
* **Backend**: Java 21 / 24, Spring Boot 3.3.3, Spring Security (JWT), Hibernate JPA, Apache PDFBox, Java Graphics2D.
* **Database**: H2 (In-memory development) / PostgreSQL (Production ready).
* **AI & Vision Engine**: Google Gemini 2.5 Flash / Gemini 2.0 Flash Vision AI API.
* **Deployment**: Vercel (Frontend SPA) + Render (Dockerized Spring Boot Web Service).

---

## 🚀 Quick Start

### Prerequisites
* **Node.js**: `v18+` or `v20+`
* **Java JDK**: `v21+`
* **Maven**: `v3.9+` (or included `./mvnw`)

### 1. Clone Repository
```bash
git clone https://github.com/manikandanmp27/form-for-all.git
cd form-for-all
```

### 2. Start Backend Server
```bash
cd backend
./mvnw spring-boot:run
```
*Backend runs on `http://localhost:8080`.*

### 3. Start Frontend App
```bash
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*

For detailed configuration and environment variable setup, view [docs/setup.md](docs/setup.md).

---

## 📚 Documentation Index

| Guide | Description |
| :--- | :--- |
| 📐 [Architecture](docs/architecture.md) | High-level system design, data flow, and external service contracts. |
| 🌟 [Features](docs/features.md) | In-depth breakdown of features, user flows, and implementation status. |
| 🔌 [API Specification](docs/api.md) | Complete REST API endpoint documentation with request/response schemas. |
| 🗄️ [Database Schema](docs/database.md) | Entity-Relationship diagram, JPA entities, and database configuration. |
| ⚙️ [Developer Setup](docs/setup.md) | Prerequisites, environment variables, and local run instructions. |
| 🚀 [Deployment Guide](docs/deployment.md) | Vercel (Frontend) and Render (Backend Docker) deployment guide. |
| 🧪 [Testing Strategy](docs/testing.md) | Backend unit/integration tests and frontend build verification. |
| 🎯 [Demo Script](docs/demo.md) | Step-by-step 2–3 minute demonstration guide for hackathons and evaluators. |

---

## 🌐 Live Demo & Important Links

* **Live Web Application**: [https://form-for-all-one.vercel.app/](https://form-for-all-one.vercel.app/)
* **Demo Video**: [Watch on Google Drive](https://drive.google.com/file/d/1_eG2FGIBnoFB9yt41m2K6swPkUifeYXm/view?usp=sharing)
* **Source Code**: [GitHub Repository](https://github.com/manikandanmp27/form-for-all)
* **Project Documentation**: [View Document on Google Drive](https://drive.google.com/file/d/1SOnkHcWzv5w_O-HVv0YNkek8ClIYD0PG/view?usp=sharing)
* **Interactive Presentation Script**: View [docs/demo.md](docs/demo.md).

---

## 👥 Contributors

* **FormForAll Core Engineering Team** (`manikandanmp27/form-for-all`)
