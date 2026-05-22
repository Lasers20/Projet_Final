package com.strms.enums;

public enum TaskCategory {
    BUGFIX("Bug Fix"),
    FEATURE("Feature"),
    DOCUMENTATION("Documentation"),
    RESEARCH("Research");

    private final String label;

    TaskCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
