package com.strms.util;

import com.strms.enums.TaskStatus;
import com.strms.manager.TaskManager;
import com.strms.model.Task;
import com.strms.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportGenerator implements Reportable {

    private final TaskManager manager;

    public ReportGenerator(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public String generateReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("==== STRMS Report (")
          .append(LocalDateTime.now()
                  .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
          .append(") ====\n\n");

        sb.append("Users:\n");
        for (User u : manager.listUsers()) sb.append("  - ").append(u).append("\n");

        sb.append("\nTasks (").append(manager.listTasks().size()).append("):\n");
        for (Task t : manager.listTasks()) {
            sb.append("  * ").append(t).append("\n");
            sb.append("      Deadline: ").append(t.getDeadline()).append("\n");
            sb.append("      Engineer: ")
              .append(t.getEngineer() == null ? "-" : t.getEngineer().getName())
              .append("\n");
            sb.append("      Dependencies: ");
            if (t.getDependencies().isEmpty()) sb.append("none");
            else for (Task d : t.getDependencies()) sb.append(d.getId()).append(" ");
            sb.append("\n");
        }

        sb.append("\nCounters:\n");
        for (TaskStatus s : TaskStatus.values()) {
            long count = manager.listTasks().stream()
                    .filter(t -> t.getStatus() == s).count();
            sb.append("  ").append(s).append(" : ").append(count).append("\n");
        }

        sb.append("\nOverdue tasks:\n");
        LocalDate today = LocalDate.now();
        for (Task t : manager.listTasks()) {
            if (t.getDeadline() != null
                    && t.getDeadline().isBefore(today)
                    && t.getStatus() != TaskStatus.DONE) {
                sb.append("  ! ").append(t).append(" (deadline ")
                  .append(t.getDeadline()).append(")\n");
            }
        }
        return sb.toString();
    }

    public String generateReportForUser(User u) {
        StringBuilder sb = new StringBuilder();
        sb.append("==== Report for ").append(u.getName()).append(" ====\n");
        for (Task t : manager.listTasks()) {
            if (t.getEngineer() != null && t.getEngineer().equals(u)) {
                sb.append("  * ").append(t).append("\n");
            }
        }
        return sb.toString();
    }
}
