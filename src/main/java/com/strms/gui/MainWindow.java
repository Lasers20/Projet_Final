package com.strms.gui;

import com.strms.enums.NotificationType;
import com.strms.enums.TaskStatus;
import com.strms.exceptions.*;
import com.strms.manager.TaskManager;
import com.strms.model.*;
import com.strms.util.Dashboard;
import com.strms.util.NotificationManager;
import com.strms.util.ReportGenerator;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {

    private final TaskManager manager;
    private final User currentUser;
    private final NotificationManager notifier = new NotificationManager();
    private final ReportGenerator reporter;
    private final Dashboard dashboard;

    private final TaskTableModel taskModel = new TaskTableModel();
    private final JTable taskTable = new JTable(taskModel);
    private final JTextArea historyArea = new JTextArea(8, 60);
    private final JTextArea dashboardArea = new JTextArea(8, 60);
    private final JLabel statusBar = new JLabel(" ");

    public MainWindow(TaskManager manager, User currentUser) {
        super("STRMS - Smart Task & Resource Management System");
        this.manager = manager;
        this.currentUser = currentUser;
        this.reporter = new ReportGenerator(manager);
        this.dashboard = new Dashboard(manager);

        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildToolbar(),  BorderLayout.NORTH);
        add(buildCenter(),   BorderLayout.CENTER);
        add(buildStatusBar(),BorderLayout.SOUTH);

        refreshAll();
    }

    private JComponent buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JLabel who = new JLabel(" Connected: "
                + currentUser.getName() + " (" + currentUser.getRole() + ")   ");
        who.setFont(who.getFont().deriveFont(Font.BOLD));
        tb.add(who);
        tb.addSeparator();

        addBtn(tb, "Create task",      e -> onCreate(),     currentUser.canCreateTask());
        addBtn(tb, "Delete task",      e -> onDelete(),     currentUser.canDeleteTask());
        addBtn(tb, "Assign engineer",  e -> onAssign(),     currentUser.canAssignTask());
        addBtn(tb, "Add dependency",   e -> onAddDep(),     currentUser.canCreateTask());
        addBtn(tb, "Remove dependency",e -> onRemoveDep(),  currentUser.canCreateTask());
        tb.addSeparator();
        addBtn(tb, "Start task",       e -> onStart(),      currentUser.canExecuteTask());
        addBtn(tb, "Complete task",    e -> onComplete(),   currentUser.canExecuteTask());
        tb.addSeparator();
        addBtn(tb, "Save",             e -> onSave(),       true);
        addBtn(tb, "Load",             e -> onLoad(),       true);
        addBtn(tb, "Export Report",    e -> onExport(),     currentUser.canGenerateReport());
        addBtn(tb, "Refresh",          e -> refreshAll(),   true);

        return tb;
    }

    private void addBtn(JToolBar tb, String label,
                        java.awt.event.ActionListener l, boolean enabled) {
        JButton b = new JButton(label);
        b.addActionListener(l);
        b.setEnabled(enabled);
        tb.add(b);
    }

    private JComponent buildCenter() {
        JScrollPane taskScroll = new JScrollPane(taskTable);
        taskScroll.setBorder(BorderFactory.createTitledBorder("Tasks"));

        historyArea.setEditable(false);
        JScrollPane historyScroll = new JScrollPane(historyArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Task history"));

        dashboardArea.setEditable(false);
        dashboardArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane dashScroll = new JScrollPane(dashboardArea);
        dashScroll.setBorder(BorderFactory.createTitledBorder("Dashboard"));

        JSplitPane bottom = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, historyScroll, dashScroll);
        bottom.setResizeWeight(0.55);

        JSplitPane main = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT, taskScroll, bottom);
        main.setResizeWeight(0.6);
        return main;
    }

    private JComponent buildStatusBar() {
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        return statusBar;
    }

    private void setStatus(String s) {
        statusBar.setText(" " + s);
    }

    private void refreshAll() {
        taskModel.fireTableDataChanged();
        showHistoryForSelected();
        dashboardArea.setText(buildDashboardText());
    }

    private String buildDashboardText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Total tasks : ").append(manager.listTasks().size()).append('\n');
        sb.append("Total users : ").append(manager.listUsers().size()).append('\n');
        sb.append("Overdue     : ").append(dashboard.countOverdue()).append("\n\n");
        sb.append("By status:\n");
        dashboard.tasksByStatus().forEach((k, v) ->
                sb.append("  ").append(k).append(" : ").append(v).append('\n'));
        sb.append("\nBy priority:\n");
        dashboard.tasksByPriority().forEach((k, v) ->
                sb.append("  ").append(k).append(" : ").append(v).append('\n'));
        sb.append("\nBy engineer:\n");
        dashboard.tasksByUser().forEach((k, v) ->
                sb.append("  ").append(k).append(" : ").append(v).append('\n'));
        return sb.toString();
    }

    private Task selectedTask() {
        int row = taskTable.getSelectedRow();
        if (row < 0) return null;
        return taskModel.rowAt(row);
    }

    private void showHistoryForSelected() {
        Task t = selectedTask();
        if (t == null) { historyArea.setText(""); return; }
        StringBuilder sb = new StringBuilder("History of " + t.getId() + "\n");
        for (TaskHistoryEntry h : t.getHistory()) sb.append(h).append('\n');
        historyArea.setText(sb.toString());
    }

    private void onCreate() {
        TaskDialog dlg = new TaskDialog(this);
        dlg.setVisible(true);
        Task t = dlg.getResult();
        if (t == null) return;
        try {
            manager.addTask(t, currentUser);
            notifier.notifyUser(currentUser, NotificationType.CONSOLE,
                    "Task " + t.getId() + " created.");
            setStatus("Task " + t.getId() + " created.");
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onDelete() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        try {
            manager.deleteTask(t.getId(), currentUser);
            setStatus("Task " + t.getId() + " deleted.");
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onAssign() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        java.util.List<Engineer> engineers = new java.util.ArrayList<>();
        for (User u : manager.listUsers())
            if (u instanceof Engineer) engineers.add((Engineer) u);
        if (engineers.isEmpty()) { warn("No engineer available."); return; }
        Engineer chosen = (Engineer) JOptionPane.showInputDialog(this,
                "Assign engineer:", "Assign",
                JOptionPane.QUESTION_MESSAGE, null,
                engineers.toArray(), engineers.get(0));
        if (chosen == null) return;
        try {
            manager.assignTask(t.getId(), chosen, currentUser);
            setStatus("Assigned " + chosen.getName() + " to " + t.getId());
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onAddDep() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        String depId = JOptionPane.showInputDialog(this,
                "ID of the task that '" + t.getId() + "' depends on:");
        if (depId == null || depId.isBlank()) return;
        try {
            manager.addDependency(t.getId(), depId.trim(), currentUser);
            setStatus(t.getId() + " now depends on " + depId);
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onRemoveDep() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        if (t.getDependencies().isEmpty()) { warn("No dependency."); return; }
        Task dep = (Task) JOptionPane.showInputDialog(this,
                "Choose dependency to remove:", "Remove dependency",
                JOptionPane.QUESTION_MESSAGE, null,
                t.getDependencies().toArray(), t.getDependencies().get(0));
        if (dep == null) return;
        try {
            manager.removeDependency(t.getId(), dep.getId(), currentUser);
            setStatus("Removed dependency " + dep.getId() + " from " + t.getId());
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onStart() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        try {
            manager.startTask(t.getId(), currentUser);
            setStatus("Started " + t.getId());
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onComplete() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        try {
            manager.completeTask(t.getId(), currentUser);
            setStatus("Completed " + t.getId());
            notifier.notifyUser(currentUser, NotificationType.EMAIL,
                    "Task " + t.getId() + " completed.");
            refreshAll();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onSave() {
        try {
            manager.saveTasksToFile("data/tasks.dat");
            manager.saveUsersToFile("data/users.dat");
            setStatus("Saved data/tasks.dat and data/users.dat");
        } catch (FilePersistenceException ex) {
            showError(ex);
        }
    }

    private void onLoad() {
        try {
            manager.loadUsersFromFile("data/users.dat");
            manager.loadTasksFromFile("data/tasks.dat");
            setStatus("Loaded from data/");
            refreshAll();
        } catch (FilePersistenceException ex) {
            showError(ex);
        }
    }

    private void onExport() {
        try {
            String report = reporter.generateReport();
            com.strms.util.FileManager.writeText("data/report.txt", report);
            setStatus("Report exported: data/report.txt");
        } catch (FilePersistenceException ex) {
            showError(ex);
        }
    }

    private void warn(String s) {
        JOptionPane.showMessageDialog(this, s, "Notice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getMessage(), ex.getClass().getSimpleName(),
                JOptionPane.ERROR_MESSAGE);
        setStatus(ex.getClass().getSimpleName() + " : " + ex.getMessage());
    }

    private class TaskTableModel extends AbstractTableModel {
        private final String[] cols = {
                "ID", "Title", "Status", "Priority", "Category",
                "Deadline", "Engineer", "Dependencies"
        };

        @Override public int getRowCount() { return manager.listTasks().size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        Task rowAt(int row) {
            List<Task> tasks = new java.util.ArrayList<>(manager.listTasks());
            return tasks.get(row);
        }

        @Override
        public Object getValueAt(int row, int col) {
            Task t = rowAt(row);
            switch (col) {
                case 0: return t.getId();
                case 1: return t.getTitle();
                case 2: return t.getStatus();
                case 3: return t.getPriority();
                case 4: return t.getCategory();
                case 5: return t.getDeadline();
                case 6: return t.getEngineer() == null ? "-" : t.getEngineer().getName();
                case 7: return depString(t);
                default: return "";
            }
        }

        private String depString(Task t) {
            StringBuilder sb = new StringBuilder();
            for (Task d : t.getDependencies()) sb.append(d.getId()).append(" ");
            return sb.toString().trim();
        }
    }

    public void install() {
        taskTable.getSelectionModel().addListSelectionListener(
                e -> showHistoryForSelected());
        for (Task t : manager.listTasks()) {
            if (t.getStatus() == TaskStatus.DONE) continue;
        }
        setVisible(true);
    }
}
