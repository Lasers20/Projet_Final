package com.strms.enums;

public enum TaskStatus {
    TODO("To Do"),
    BLOCKED("Blocked"),
    IN_PROGRESS("In Progress"),
    DONE("Done");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitionTo(TaskStatus next) {
        if (this == DONE) return false;
        if (this == next) return false;
        switch (this) {
            case TODO:        return next == BLOCKED || next == IN_PROGRESS;
            case BLOCKED:     return next == TODO || next == IN_PROGRESS;
            case IN_PROGRESS: return next == BLOCKED || next == DONE;
            default:          return false;
        }
    }
}
