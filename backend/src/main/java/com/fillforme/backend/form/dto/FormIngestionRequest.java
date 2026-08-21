package com.fillforme.backend.form.dto;

import com.fillforme.backend.form.entity.FormSourceType;
import lombok.Data;

@Data
public class FormIngestionRequest {
    private FormSourceType sourceType;
    private String formUrl;
    private String formTitle;
}
