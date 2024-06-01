package utils;

import java.util.List;

import java.util.List;

public class TemplateEntry {
    private String templateNumber;
    private String templateName;
    private String category;
    private String order;
    private int sequence;
    private double frequency;
    private double minDuration;
    private double maxDuration;
    private String accountType;
    private List<String> triples;

    public TemplateEntry(String templateNumber, String templateName, String category, String order, int sequence,
                         double frequency, double minDuration, double maxDuration, String accountType, List<String> triples) {
        this.templateNumber = templateNumber;
        this.templateName = templateName;
        this.category = category;
        this.order = order;
        this.sequence = sequence;
        this.frequency = frequency;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
        this.accountType = accountType;
        this.triples = triples;
    }

    // Getters and setters for all fields
    public String getTemplateNumber() {
        return templateNumber;
    }

    public void setTemplateNumber(String templateNumber) {
        this.templateNumber = templateNumber;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    public double getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(double minDuration) {
        this.minDuration = minDuration;
    }

    public double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public List<String> getTriples() {
        return triples;
    }

    public void setTriples(List<String> triples) {
        this.triples = triples;
    }
    
}
