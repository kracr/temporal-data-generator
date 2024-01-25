package genact.temporal.data.generator;

import java.util.List;

public class TemplateEntry {
    private String templateName;
    private double frequency;
    private double minDuration;
    private double maxDuration;
    private List<String> placeholders;

    public TemplateEntry(String templateName, double frequency, double minDuration, double maxDuration, List<String> placeholders) {
        this.templateName = templateName;
        this.frequency = frequency;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.placeholders = placeholders;
    }

    public String getTemplateName() {
        return templateName;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public List<String> getPlaceholders() {
        return placeholders;
    }
}