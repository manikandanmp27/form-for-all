package com.fillforme.backend.ai.dto;

public class TextRegionData {
    private String originalText;
    private String translatedText;
    private double xPercent;
    private double yPercent;
    private double widthPercent;
    private double heightPercent;

    public TextRegionData() {}

    public TextRegionData(String originalText, String translatedText, double xPercent, double yPercent, double widthPercent, double heightPercent) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.xPercent = xPercent;
        this.yPercent = yPercent;
        this.widthPercent = widthPercent;
        this.heightPercent = heightPercent;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public double getXPercent() {
        return xPercent;
    }

    public void setXPercent(double xPercent) {
        this.xPercent = xPercent;
    }

    public double getYPercent() {
        return yPercent;
    }

    public void setYPercent(double yPercent) {
        this.yPercent = yPercent;
    }

    public double getWidthPercent() {
        return widthPercent;
    }

    public void setWidthPercent(double widthPercent) {
        this.widthPercent = widthPercent;
    }

    public double getHeightPercent() {
        return heightPercent;
    }

    public void setHeightPercent(double heightPercent) {
        this.heightPercent = heightPercent;
    }
}
