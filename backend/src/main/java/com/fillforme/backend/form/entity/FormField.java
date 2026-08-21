package com.fillforme.backend.form.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "form_fields")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private FormSession session;

    @Column(name = "field_order", nullable = false)
    private Integer fieldOrder;

    @Column(name = "field_key", nullable = false)
    private String fieldKey;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_type", nullable = false)
    private FieldType fieldType;

    @Column(name = "plain_language_explanation", columnDefinition = "TEXT")
    private String plainLanguageExplanation;

    @Column(name = "why_asked", columnDefinition = "TEXT")
    private String whyAsked;

    @Column(name = "simplified_question_text", columnDefinition = "TEXT")
    private String simplifiedQuestionText;

    @Builder.Default
    @Column(nullable = false)
    private Boolean required = true;

    @Column(columnDefinition = "TEXT")
    private String defaultHelpText;

    @OneToOne(mappedBy = "field", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private FieldResponse response;
}
