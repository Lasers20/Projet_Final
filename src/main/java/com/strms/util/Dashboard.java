package com.strms.util;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskStatus;
import com.strms.manager.TaskManager;
import com.strms.model.Task;
import com.strms.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Dashboard {

    private final TaskManager manager;

    public Dashboard(TaskManager manager) {
        this.manager = manager;
    }

    public Map<TaskStatus, Long> tasksByStatus() {
        Map<TaskStatus, Long> m = new HashMap<>();
        for (TaskStatus s : TaskStatus.values()) m.put(s, 0L);
        for (Task t : manager.listTasks()) m.merge(t.getStatus(), 1L, Long::sum);
        return m;
    }

    public Map<PriorityLevel, Long> tasksByPriority() {
        Map<PriorityLevel, Long> m = new HashMap<>();
        for (PriorityLevel p : PriorityLevel.values()) m.put(p, 0L);
        for (Task t : manager.listTasks()) m.merge(t.getPriority(), 1L, Long::sum);
        return m;
    }

    public Map<String, Long> tasksByUser() {
        Map<String, Long> m = new HashMap<>();
        for (User u : manager.listUsers()) m.put(u.getName(), 0L);
        for (Task t : manager.listTasks()) {
            if (t.getEngineer() != null) {
                m.merge(t.getEngineer().getName(), 1L, Long::sum);
            }
        }
        return m;
    }

    public long countOverdue() {
        LocalDate today = LocalDate.now();
        return manager.listTasks().stream()
                .filter(t -> t.getDeadline() != null
                          && t.getDeadline().isBefore(today)
                          && t.getStatus() != TaskStatus.DONE)
                .count();
    }

    public void display() {
        System.out.println("===== STRMS DASHBOARD =====");
        System.out.println("Total tasks  : " + manager.listTasks().size());
        System.out.println("Total users  : " + manager.listUsers().size());
        System.out.println("Overdue      : " + countOverdue());
        System.out.println("\nBy status:");
        tasksByStatus().forEach((k, v) ->
                System.out.println("  " + k + " : " + v));
        System.out.println("\nBy priority:");
        tasksByPriority().forEach((k, v) ->
                System.out.println("  " + k + " : " + v));
        System.out.println("\nBy engineer:");
        tasksByUser().forEach((k, v) ->
                System.out.println("  " + k + " : " + v));
    }
}
