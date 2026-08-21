package com.fillforme.backend.document.dto;

import com.fillforme.backend.form.entity.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedFieldData {
    private String fieldKey;
    private String label;
    private FieldType fieldType;
    private boolean required;
    private int orderIndex;
    private String defaultHelpText;
    private List<String> options;
}
