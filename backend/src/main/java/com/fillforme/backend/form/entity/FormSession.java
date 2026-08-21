package com.fillforme.backend.form.entity;

import com.fillforme.backend.auth.entity.User;
import com.fillforme.backend.risk.entity.RiskFlag;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "form_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Builder.Default
    @Column(name = "current_field_index", nullable = false)
    private Integer currentFieldIndex = 0;

    @Builder.Default
    @Column(name = "total_fields", nullable = false)
    private Integer totalFields = 0;

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fieldOrder ASC")
    private List<FormField> fields = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RiskFlag> riskFlags = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
