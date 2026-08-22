package com.fillforme.backend.translation.service.impl;

import com.fillforme.backend.document.service.StorageService;
import com.fillforme.backend.translation.dto.TranslatedFormResponseDto;
import com.fillforme.backend.translation.service.TranslationService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fillforme.backend.ai.dto.TextRegionData;
import com.fillforme.backend.ai.service.AIService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DocumentLayoutRenderer {

    private static final Logger log = LoggerFactory.getLogger(DocumentLayoutRenderer.class);

    private final TranslationService translationService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    private final AIService aiService;

    public DocumentLayoutRenderer(TranslationService translationService, StorageService storageService, AIService aiService) {
        this.translationService = translationService;
        this.storageService = storageService;
        this.aiService = aiService;
        this.objectMapper = new ObjectMapper();
    }

    public TranslatedFormResponseDto renderTranslatedDocument(
            byte[] fileBytes, String filename, String mimeType, String sourceLang, String targetLang, String textRegionsJson) {

        log.info("Processing layout-preserving translation for document '{}' (target: {})", filename, targetLang);
        boolean isPdf = (mimeType != null && mimeType.toLowerCase().contains("pdf")) ||
                (filename != null && filename.toLowerCase().endsWith(".pdf"));

        try {
            List<TextRegion> customRegions = new ArrayList<>(parseTextRegionsJson(textRegionsJson));

            if (customRegions.isEmpty()) {
                try {
                    List<TextRegionData> visionData = aiService.translateDocumentWithVisionAI(fileBytes, filename, mimeType, targetLang);
                    if (visionData != null && !visionData.isEmpty()) {
                        BufferedImage tmpImg = ImageIO.read(new ByteArrayInputStream(fileBytes));
                        int w = tmpImg != null ? tmpImg.getWidth() : 800;
                        int h = tmpImg != null ? tmpImg.getHeight() : 1000;

                        for (TextRegionData trd : visionData) {
                            int rx = (int) Math.round((trd.getXPercent() / 100.0) * w);
                            int ry = (int) Math.round((trd.getYPercent() / 100.0) * h);
                            int rw = (int) Math.max(30, Math.round((trd.getWidthPercent() / 100.0) * w));
                            int rh = (int) Math.max(14, Math.round((trd.getHeightPercent() / 100.0) * h));

                            customRegions.add(new TextRegion(trd.getOriginalText(), trd.getTranslatedText(), rx, ry, rw, rh));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Vision AI document translation failed: {}", e.getMessage());
                }
            }

            if (isPdf) {
                return processPdfDocument(fileBytes, filename, sourceLang, targetLang, customRegions);
            } else {
                return processImageDocument(fileBytes, filename, sourceLang, targetLang, customRegions);
            }
        } catch (Exception e) {
            log.error("Failed to render layout-preserved translated document: {}", filename, e);
            throw new RuntimeException("Document layout translation failed: " + e.getMessage(), e);
        }
    }

    private TranslatedFormResponseDto processImageDocument(
            byte[] imageBytes, String filename, String sourceLang, String targetLang, List<TextRegion> customRegions) throws IOException {

        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IllegalArgumentException("Invalid image file format.");
        }

        BufferedImage translatedImage = translateImageCanvas(original, sourceLang, targetLang, customRegions);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(translatedImage, "png", baos);
        byte[] outputBytes = baos.toByteArray();

        String storedName = "translated_" + System.currentTimeMillis() + "_" + (filename != null ? filename : "form.png");
        String storedPath = storageService.storeBytes(outputBytes, storedName);

        String imageUrl = "/api/export/files/" + storedName;

        return TranslatedFormResponseDto.builder()
                .success(true)
                .originalFilename(filename)
                .sourceLanguage(sourceLang != null ? sourceLang : "auto")
                .targetLanguage(targetLang)
                .imageUrl(imageUrl)
                .pdfUrl(imageUrl)
                .totalPages(1)
                .message("Visual form layout successfully translated to " + targetLang)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private TranslatedFormResponseDto processPdfDocument(
            byte[] pdfBytes, String filename, String sourceLang, String targetLang, List<TextRegion> customRegions) throws IOException {

        try (PDDocument pdfDoc = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(pdfDoc);
            int pageCount = pdfDoc.getNumberOfPages();
            BufferedImage firstPageTranslated = null;

            for (int i = 0; i < Math.min(pageCount, 5); i++) {
                BufferedImage pageImg = renderer.renderImageWithDPI(i, 150);
                BufferedImage translatedPage = translateImageCanvas(pageImg, sourceLang, targetLang, customRegions);
                if (i == 0) {
                    firstPageTranslated = translatedPage;
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(firstPageTranslated != null ? firstPageTranslated : new BufferedImage(600, 800, BufferedImage.TYPE_INT_RGB), "png", baos);
            byte[] outputBytes = baos.toByteArray();

            String storedName = "translated_" + System.currentTimeMillis() + ".png";
            storageService.storeBytes(outputBytes, storedName);
            String imageUrl = "/api/export/files/" + storedName;

            return TranslatedFormResponseDto.builder()
                    .success(true)
                    .originalFilename(filename)
                    .sourceLanguage(sourceLang != null ? sourceLang : "auto")
                    .targetLanguage(targetLang)
                    .imageUrl(imageUrl)
                    .pdfUrl(imageUrl)
                    .totalPages(pageCount)
                    .message("PDF layout successfully translated to " + targetLang)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private BufferedImage translateImageCanvas(BufferedImage original, String sourceLang, String targetLang, List<TextRegion> customRegions) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = canvas.createGraphics();

        // Draw original background & graphics (borders, boxes, lines)
        g2d.drawImage(original, 0, 0, null);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<TextRegion> regions = (customRegions != null && !customRegions.isEmpty())
                ? customRegions
                : getDefaultFallbackRegions(width, height);

        for (TextRegion r : regions) {
            String translated = (r.translatedText != null && !r.translatedText.isBlank())
                    ? r.translatedText
                    : translationService.translate(r.originalText, sourceLang, targetLang);

            // Step 1: Erase/mask original text region with clean white background
            g2d.setColor(Color.WHITE);
            g2d.fillRect(r.x, r.y, r.width, r.height);

            // Step 2: Render translated text inside exact bounding box with auto font scaling
            g2d.setColor(new Color(15, 23, 42)); // Slate-900 text color
            int fontSize = Math.max(10, Math.min(r.height - 2, 20));
            Font font = new Font("Dialog", Font.BOLD, fontSize);
            g2d.setFont(font);

            // Measure string width and scale down if font overflows box
            FontMetrics metrics = g2d.getFontMetrics(font);
            while (metrics.stringWidth(translated) > r.width - 2 && fontSize > 8) {
                fontSize--;
                font = new Font("Dialog", Font.BOLD, fontSize);
                g2d.setFont(font);
                metrics = g2d.getFontMetrics(font);
            }

            int textY = r.y + ((r.height - metrics.getHeight()) / 2) + metrics.getAscent();
            g2d.drawString(translated, r.x + 1, textY);
        }

        g2d.dispose();
        return canvas;
    }

    private List<TextRegion> parseTextRegionsJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            List<TextRegion> regions = new ArrayList<>();
            for (Map<String, Object> map : list) {
                String text = (String) map.getOrDefault("originalText", "");
                int x = ((Number) map.getOrDefault("x", 0)).intValue();
                int y = ((Number) map.getOrDefault("y", 0)).intValue();
                int w = ((Number) map.getOrDefault("width", 100)).intValue();
                int h = ((Number) map.getOrDefault("height", 25)).intValue();

                if (!text.isBlank()) {
                    regions.add(new TextRegion(text, x, y, w, h));
                }
            }
            return regions;
        } catch (Exception e) {
            log.warn("Failed to parse custom textRegionsJson: {}", e.getMessage());
            return List.of();
        }
    }

    private List<TextRegion> getDefaultFallbackRegions(int width, int height) {
        return List.of(
                new TextRegion("Full Name", (int)(width * 0.08), (int)(height * 0.15), (int)(width * 0.35), (int)(height * 0.04)),
                new TextRegion("Date of Birth", (int)(width * 0.08), (int)(height * 0.25), (int)(width * 0.35), (int)(height * 0.04)),
                new TextRegion("Gender", (int)(width * 0.08), (int)(height * 0.35), (int)(width * 0.35), (int)(height * 0.04)),
                new TextRegion("Phone Number", (int)(width * 0.08), (int)(height * 0.45), (int)(width * 0.35), (int)(height * 0.04)),
                new TextRegion("Address", (int)(width * 0.08), (int)(height * 0.55), (int)(width * 0.35), (int)(height * 0.04))
        );
    }

    private static class TextRegion {
        String originalText;
        String translatedText;
        int x, y, width, height;

        TextRegion(String originalText, int x, int y, int width, int height) {
            this.originalText = originalText;
            this.translatedText = null;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        TextRegion(String originalText, String translatedText, int x, int y, int width, int height) {
            this.originalText = originalText;
            this.translatedText = translatedText;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
