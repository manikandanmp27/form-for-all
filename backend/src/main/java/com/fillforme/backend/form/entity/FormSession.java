package com.fillforme.backend.form.entity;

import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.risk.entity.RiskFlag;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "form_sessions")
public class FormSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "form_title", nullable = false)
    private String formTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "form_source_type", nullable = false)
    private FormSourceType formSourceType;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "stored_filename")
    private String storedFilename;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private SessionStatus sessionStatus;

    @Column(name = "current_field_index", nullable = false)
    private Integer currentFieldIndex = 0;

    @Column(name = "total_fields", nullable = false)
    private Integer totalFields = 0;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fieldOrder ASC")
    private List<FormField> fields = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RiskFlag> riskFlags = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FormSession() {}

    public FormSession(UUID id, User user, String formTitle, FormSourceType formSourceType, String sourceUrl, String storedFilename, SessionStatus sessionStatus, Integer currentFieldIndex, Integer totalFields, List<FormField> fields, List<RiskFlag> riskFlags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.formTitle = formTitle;
        this.formSourceType = formSourceType;
        this.sourceUrl = sourceUrl;
        this.storedFilename = storedFilename;
        this.sessionStatus = sessionStatus;
        this.currentFieldIndex = currentFieldIndex != null ? currentFieldIndex : 0;
        this.totalFields = totalFields != null ? totalFields : 0;
        this.fields = fields != null ? fields : new ArrayList<>();
        this.riskFlags = riskFlags != null ? riskFlags : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FormSessionBuilder builder() {
        return new FormSessionBuilder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFormTitle() { return formTitle; }
    public void setFormTitle(String formTitle) { this.formTitle = formTitle; }

    public FormSourceType getFormSourceType() { return formSourceType; }
    public void setFormSourceType(FormSourceType formSourceType) { this.formSourceType = formSourceType; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public SessionStatus getSessionStatus() { return sessionStatus; }
    public void setSessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; }

    public Integer getCurrentFieldIndex() { return currentFieldIndex; }
    public void setCurrentFieldIndex(Integer currentFieldIndex) { this.currentFieldIndex = currentFieldIndex; }

    public Integer getTotalFields() { return totalFields; }
    public void setTotalFields(Integer totalFields) { this.totalFields = totalFields; }

    public List<FormField> getFields() { return fields; }
    public void setFields(List<FormField> fields) { this.fields = fields; }

    public List<RiskFlag> getRiskFlags() { return riskFlags; }
    public void setRiskFlags(List<RiskFlag> riskFlags) { this.riskFlags = riskFlags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class FormSessionBuilder {
        private UUID id;
        private User user;
        private String formTitle;
        private FormSourceType formSourceType;
        private String sourceUrl;
        private String storedFilename;
        private SessionStatus sessionStatus;
        private Integer currentFieldIndex = 0;
        private Integer totalFields = 0;
        private List<FormField> fields = new ArrayList<>();
        private List<RiskFlag> riskFlags = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        FormSessionBuilder() {}

        public FormSessionBuilder id(UUID id) { this.id = id; return this; }
        public FormSessionBuilder user(User user) { this.user = user; return this; }
        public FormSessionBuilder formTitle(String formTitle) { this.formTitle = formTitle; return this; }
        public FormSessionBuilder formSourceType(FormSourceType formSourceType) { this.formSourceType = formSourceType; return this; }
        public FormSessionBuilder sourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; return this; }
        public FormSessionBuilder storedFilename(String storedFilename) { this.storedFilename = storedFilename; return this; }
        public FormSessionBuilder sessionStatus(SessionStatus sessionStatus) { this.sessionStatus = sessionStatus; return this; }
        public FormSessionBuilder currentFieldIndex(Integer currentFieldIndex) { this.currentFieldIndex = currentFieldIndex; return this; }
        public FormSessionBuilder totalFields(Integer totalFields) { this.totalFields = totalFields; return this; }
        public FormSessionBuilder fields(List<FormField> fields) { this.fields = fields; return this; }
        public FormSessionBuilder riskFlags(List<RiskFlag> riskFlags) { this.riskFlags = riskFlags; return this; }
        public FormSessionBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public FormSessionBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public FormSession build() {
            return new FormSession(id, user, formTitle, formSourceType, sourceUrl, storedFilename, sessionStatus, currentFieldIndex, totalFields, fields, riskFlags, createdAt, updatedAt);
        }
    }
}
