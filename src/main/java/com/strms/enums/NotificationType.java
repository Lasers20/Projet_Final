package com.strms.enums;

public enum NotificationType {
    EMAIL("Email"),
    SMS("SMS"),
    CONSOLE("Console");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
