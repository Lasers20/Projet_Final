package com.strms.model;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskCategory;
import com.strms.enums.TaskStatus;
import com.strms.exceptions.InvalidTaskStateException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Task implements Comparable<Task>, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String title;
    private String description;
    private PriorityLevel priority;
    private TaskStatus status;
    private TaskCategory category;
    private LocalDate deadline;
    private Engineer engineer;
    private final List<Task> dependencies;
    private final List<TaskHistoryEntry> history;

    public Task(String id, String title, String description,
                PriorityLevel priority, TaskCategory category, LocalDate deadline) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.deadline = deadline;
        this.status = TaskStatus.TODO;
        this.dependencies = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public String getId()                 { return id; }
    public String getTitle()              { return title; }
    public String getDescription()        { return description; }
    public PriorityLevel getPriority()    { return priority; }
    public TaskStatus getStatus()         { return status; }
    public TaskCategory getCategory()     { return category; }
    public LocalDate getDeadline()        { return deadline; }
    public Engineer getEngineer()         { return engineer; }
    public List<Task> getDependencies()   { return new ArrayList<>(dependencies); }
    public List<TaskHistoryEntry> getHistory() { return new ArrayList<>(history); }

    public void setTitle(String title)            { this.title = title; }
    public void setDescription(String description){ this.description = description; }
    public void setDeadline(LocalDate deadline)   { this.deadline = deadline; }
    public void setEngineer(Engineer engineer)    { this.engineer = engineer; }

    public void updateStatus(TaskStatus newStatus) throws InvalidTaskStateException {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidTaskStateException(
                    String.format("Cannot transition task '%s' from %s to %s",
                            id, this.status, newStatus));
        }
        this.status = newStatus;
    }

    public void forceStatus(TaskStatus newStatus) {
        this.status = newStatus;
    }

    public void changePriority(PriorityLevel newPriority) {
        this.priority = newPriority;
    }

    public void updateDescription(String newDescription) {
        this.description = newDescription;
    }

    public void addDependencyInternal(Task t) {
        if (!dependencies.contains(t)) {
            dependencies.add(t);
        }
    }

    public void removeDependencyInternal(Task t) {
        dependencies.remove(t);
    }

    public void addHistoryEntry(TaskHistoryEntry entry) {
        history.add(entry);
    }

    public boolean allDependenciesDone() {
        for (Task t : dependencies) {
            if (t.getStatus() != TaskStatus.DONE) return false;
        }
        return true;
    }

    public void markAsDone() throws InvalidTaskStateException {
        updateStatus(TaskStatus.DONE);
    }

    public String displayTask() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task ").append(id).append(" - ").append(title).append("\n");
        sb.append("  Status: ").append(status).append("\n");
        sb.append("  Priority: ").append(priority).append("\n");
        sb.append("  Category: ").append(category).append("\n");
        sb.append("  Deadline: ").append(deadline).append("\n");
        sb.append("  Engineer: ")
                .append(engineer == null ? "Unassigned" : engineer.getName()).append("\n");
        sb.append("  Dependencies: ");
        if (dependencies.isEmpty()) {
            sb.append("none");
        } else {
            for (Task t : dependencies) sb.append(t.getId()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Task other) {
        return Integer.compare(other.priority.getWeight(), this.priority.getWeight());
    }

    @Override
    public String toString() {
        return id + " - " + title + " [" + status + "/" + priority + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
