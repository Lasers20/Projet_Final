package com.strms.enums;

public enum PriorityLevel {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    CRITICAL(4, "Critical");

    private final int weight;
    private final String label;

    PriorityLevel(int weight, String label) {
        this.weight = weight;
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public String getLabel() {
        return label;
    }
}
