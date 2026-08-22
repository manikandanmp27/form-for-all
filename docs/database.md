# Database Schema & Data Layer Documentation

This document describes the database schema, entity relationships, and persistence configuration implemented in the Spring Boot backend (`com.fillforme.backend`).

---

## 1. Database Architecture

* **Development Engine**: H2 In-Memory Relational Database (`jdbc:h2:mem:fillformedb`).
* **H2 Console**: Accessible locally at `/h2-console` (Username: `SA`, Password: empty).
* **Production Compatibility**: Standard ANSI SQL schema fully compatible with PostgreSQL.
* **Schema Management**: Managed automatically via Spring Data JPA and Hibernate ORM (`spring.jpa.hibernate.ddl-auto=update`).

---

## 2. Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    USERS ||--o| ACCESSIBILITY_PROFILES : "has profile"
    USERS ||--o{ FORM_SESSIONS : "owns"
    FORM_SESSIONS ||--o{ FORM_FIELDS : "contains"
    FORM_SESSIONS ||--o{ FIELD_RESPONSES : "has responses"
    FORM_SESSIONS ||--o{ RISK_FLAGS : "has risks"
    FORM_FIELDS ||--o| FIELD_RESPONSES : "answered by"
    FORM_FIELDS ||--o{ RISK_FLAGS : "flagged by"

    USERS {
        uuid id PK
        string email UK
        string password_hash
        string full_name
        string preferred_language
        timestamp created_at
    }

    ACCESSIBILITY_PROFILES {
        uuid id PK
        uuid user_id FK
        double font_scale
        boolean high_contrast
        boolean dyslexia_font
        double voice_speed
        boolean auto_read_help
        boolean simplified_language
        string preferred_language
        boolean screen_reader_mode
    }

    FORM_SESSIONS {
        uuid id PK
        uuid user_id FK
        string form_title
        string source_url
        string source_file_name
        string status
        int progress_percentage
        timestamp created_at
        timestamp updated_at
    }

    FORM_FIELDS {
        uuid id PK
        uuid form_session_id FK
        string field_key
        string label
        string field_type
        boolean required
        int order_index
        string default_help_text
        boolean is_risk_flagged
        string risk_reason
    }

    FIELD_RESPONSES {
        uuid id PK
        uuid form_session_id FK
        uuid form_field_id FK
        string field_value
        boolean is_auto_filled
        double confidence_score
        timestamp answered_at
    }

    RISK_FLAGS {
        uuid id PK
        uuid form_session_id FK
        uuid form_field_id FK
        string risk_level
        string flag_type
        string description
        boolean acknowledged_by_user
    }
```

---

## 3. Entity Overview Table

| Entity Class | Table Name | Description | Key Relationships |
| :--- | :--- | :--- | :--- |
| `User` | `app_users` | Stores user credentials and authentication data. | 1:1 with `AccessibilityProfile`, 1:N with `FormSession` |
| `AccessibilityProfile` | `accessibility_profiles` | Persists user UI styling and accessibility preferences. | N:1 with `User` |
| `FormSession` | `form_sessions` | Tracks active form ingestion sessions and completion progress. | N:1 with `User`, 1:N with `FormField`, `FieldResponse`, `RiskFlag` |
| `FormField` | `form_fields` | Definitions of extracted fields in a form session. | N:1 with `FormSession`, 1:1 with `FieldResponse` |
| `FieldResponse` | `field_responses` | User or AI auto-filled responses to specific form fields. | N:1 with `FormSession`, N:1 with `FormField` |
| `RiskFlag` | `risk_flags` | Security analysis warnings associated with sensitive fields. | N:1 with `FormSession`, N:1 with `FormField` |

---

## 4. Schema Initialization

Hibernate auto-generates tables on startup based on JPA entity annotations (`@Entity`, `@Table`, `@Id`, `@ManyToOne`, `@OneToMany`). For production PostgreSQL migration scripts, SQL scripts can be placed in `src/main/resources/schema.sql`.
