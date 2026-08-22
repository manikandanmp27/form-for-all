package com.fillforme.backend.form.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "field_responses")
public class FieldResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private FormSession session;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false, unique = true)
    private FormField field;

    @Column(name = "answer_value", columnDefinition = "TEXT")
    private String answerValue;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FieldResponse() {}

    public FieldResponse(UUID id, FormSession session, FormField field, String answerValue, LocalDateTime updatedAt) {
        this.id = id;
        this.session = session;
        this.field = field;
        this.answerValue = answerValue;
        this.updatedAt = updatedAt;
    }

    public static FieldResponseBuilder builder() {
        return new FieldResponseBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public FormSession getSession() { return session; }
    public void setSession(FormSession session) { this.session = session; }

    public FormField getField() { return field; }
    public void setField(FormField field) { this.field = field; }

    public String getAnswerValue() { return answerValue; }
    public void setAnswerValue(String answerValue) { this.answerValue = answerValue; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class FieldResponseBuilder {
        private UUID id;
        private FormSession session;
        private FormField field;
        private String answerValue;
        private LocalDateTime updatedAt;

        FieldResponseBuilder() {}

        public FieldResponseBuilder id(UUID id) { this.id = id; return this; }
        public FieldResponseBuilder session(FormSession session) { this.session = session; return this; }
        public FieldResponseBuilder field(FormField field) { this.field = field; return this; }
        public FieldResponseBuilder answerValue(String answerValue) { this.answerValue = answerValue; return this; }
        public FieldResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public FieldResponse build() {
            return new FieldResponse(id, session, field, answerValue, updatedAt);
        }
    }
}
