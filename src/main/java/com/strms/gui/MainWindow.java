package com.strms.gui;

import com.strms.enums.NotificationType;
import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskStatus;
import com.strms.exceptions.*;
import com.strms.manager.TaskManager;
import com.strms.model.*;
import com.strms.util.Dashboard;
import com.strms.util.NotificationManager;
import com.strms.util.ReportGenerator;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class MainWindow extends JFrame {

    // ── Palette de couleurs ────────────────────────────────────
    private static final Color BG_DARK    = new Color(15, 17, 26);
    private static final Color BG_SIDEBAR = new Color(22, 25, 37);
    private static final Color BG_CARD    = new Color(30, 33, 48);
    private static final Color BG_TABLE   = new Color(25, 28, 42);
    private static final Color BG_ROW_ALT = new Color(28, 32, 46);
    private static final Color BG_HEADER  = new Color(35, 39, 58);
    private static final Color ACCENT     = new Color(99, 102, 241);
    private static final Color ACCENT_HOV = new Color(79,  82, 210);
    private static final Color TEXT_WHITE = new Color(235, 237, 255);
    private static final Color TEXT_GRAY  = new Color(140, 145, 175);
    private static final Color TEXT_DIM   = new Color(90,  95, 130);
    private static final Color COL_GREEN  = new Color(34,  197,  94);
    private static final Color COL_YELLOW = new Color(234, 179,   8);
    private static final Color COL_RED    = new Color(239,  68,  68);
    private static final Color COL_BLUE   = new Color(59,  130, 246);
    private static final Color COL_GRAY   = new Color(100, 116, 139);
    private static final Color SEL_BG     = new Color(60,  63,  95);

    // ── Données ────────────────────────────────────────────────
    private final TaskManager       manager;
    private final User              currentUser;
    private final Runnable          onLogout;
    private final NotificationManager notifier  = new NotificationManager();
    private final ReportGenerator   reporter;
    private final Dashboard         dashboard;

    // ── Composants ────────────────────────────────────────────
    private final TaskTableModel    taskModel   = new TaskTableModel();
    private final JTable            taskTable   = new JTable(taskModel);
    private final JTextArea         historyArea = new JTextArea();
    private final JTextArea         dashArea    = new JTextArea();
    private final JLabel            statusBar   = new JLabel(" Ready");
    private final JLabel            lblUserRole = new JLabel();

    public MainWindow(TaskManager manager, User currentUser, Runnable onLogout) {
        super("STRMS — Smart Task & Resource Management System");
        this.manager     = manager;
        this.currentUser = currentUser;
        this.onLogout    = onLogout;
        this.reporter    = new ReportGenerator(manager);
        this.dashboard   = new Dashboard(manager);

        setSize(1200, 740);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_DARK);
        setLayout(new BorderLayout());

        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildStatus(),  BorderLayout.SOUTH);

        refreshAll();
    }

    // ══════════════════════════════════════════════════════════
    // TOP BAR
    // ══════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SIDEBAR);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(50, 55, 80)));
        bar.setPreferredSize(new Dimension(0, 52));

        // Logo + titre
        JLabel logo = new JLabel("  ⚡ STRMS");
        logo.setFont(new Font("Monospaced", Font.BOLD, 18));
        logo.setForeground(ACCENT);

        // Info utilisateur + logout
        Color roleColor = roleColor(currentUser);
        lblUserRole.setText(currentUser.getName() + "  ·  " + currentUser.getRole() + "   ");
        lblUserRole.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblUserRole.setForeground(roleColor);
        lblUserRole.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JButton btnLogout = sideBtn("🔓 Logout", COL_RED);
        btnLogout.addActionListener(e -> doLogout());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 8));
        right.setBackground(BG_SIDEBAR);
        right.add(lblUserRole);
        right.add(btnLogout);

        bar.add(logo, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private Color roleColor(User u) {
        if (u instanceof Admin)    return COL_RED;
        if (u instanceof Manager)  return COL_YELLOW;
        return COL_GREEN;
    }

    // ══════════════════════════════════════════════════════════
    // SIDEBAR
    // ══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(BG_SIDEBAR);
        sb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(45, 50, 75)),
                BorderFactory.createEmptyBorder(16, 10, 16, 10)));
        sb.setPreferredSize(new Dimension(185, 0));

        sectionLabel(sb, "TASKS");
        sideAction(sb, "➕  Create task",      ACCENT,     currentUser.canCreateTask(),  e -> onCreate());
        sideAction(sb, "🗑   Delete task",      COL_RED,    currentUser.canDeleteTask(),  e -> onDelete());
        sideAction(sb, "👤  Assign engineer",   COL_BLUE,   currentUser.canAssignTask(),  e -> onAssign());
        sideAction(sb, "🔗  Add dependency",    ACCENT,     currentUser.canCreateTask(),  e -> onAddDep());
        sideAction(sb, "✂   Remove dependency", TEXT_GRAY,  currentUser.canCreateTask(),  e -> onRemoveDep());

        sb.add(Box.createVerticalStrut(12));
        sectionLabel(sb, "ENGINEER");
        sideAction(sb, "▶   Start task",        COL_YELLOW, currentUser.canExecuteTask(), e -> onStart());
        sideAction(sb, "✅  Complete task",      COL_GREEN,  currentUser.canExecuteTask(), e -> onComplete());

        sb.add(Box.createVerticalStrut(12));
        sectionLabel(sb, "DATA");
        sideAction(sb, "💾  Save",              TEXT_GRAY,  true, e -> onSave());
        sideAction(sb, "📂  Load",              TEXT_GRAY,  true, e -> onLoad());
        sideAction(sb, "📄  Export Report",     COL_BLUE,   currentUser.canGenerateReport(), e -> onExport());
        sideAction(sb, "🔄  Refresh",           TEXT_GRAY,  true, e -> refreshAll());

        sb.add(Box.createVerticalGlue());
        return sb;
    }

    private void sectionLabel(JPanel p, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 10));
        lbl.setForeground(TEXT_DIM);
        lbl.setBorder(BorderFactory.createEmptyBorder(6, 4, 3, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
    }

    private void sideAction(JPanel p, String text, Color col, boolean enabled,
                             ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(enabled ? TEXT_WHITE : TEXT_DIM);
        btn.setBackground(BG_CARD);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setEnabled(enabled);
        btn.setCursor(enabled ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                              : Cursor.getDefaultCursor());
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        btn.setHorizontalAlignment(SwingConstants.LEFT);

        if (enabled) {
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(col.darker());
                    btn.setForeground(Color.WHITE);
                }
                public void mouseExited(MouseEvent e) {
                    btn.setBackground(BG_CARD);
                    btn.setForeground(TEXT_WHITE);
                }
            });
            btn.addActionListener(action);
        }
        p.add(btn);
        p.add(Box.createVerticalStrut(2));
    }

    private JButton sideBtn(String text, Color col) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(col);
        btn.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 30));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(col.darker(), 1),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(col.darker()); btn.setForeground(Color.WHITE); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(col.getRed(), col.getGreen(), col.getBlue(), 30)); btn.setForeground(col); }
        });
        return btn;
    }

    // ══════════════════════════════════════════════════════════
    // CENTRE : tableau + historique + dashboard
    // ══════════════════════════════════════════════════════════
    private JComponent buildCenter() {
        // ── Tableau ───────────────────────────────────────────
        styleTable(taskTable);
        JScrollPane tableScroll = darkScroll(taskTable);
        tableScroll.setBorder(titledBorder("  Tasks"));

        // ── Historique ────────────────────────────────────────
        historyArea.setEditable(false);
        historyArea.setBackground(BG_CARD);
        historyArea.setForeground(TEXT_GRAY);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        historyArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane histScroll = darkScroll(historyArea);
        histScroll.setBorder(titledBorder("  Task History"));

        // ── Dashboard ─────────────────────────────────────────
        dashArea.setEditable(false);
        dashArea.setBackground(BG_CARD);
        dashArea.setForeground(COL_GREEN);
        dashArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        dashArea.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane dashScroll = darkScroll(dashArea);
        dashScroll.setBorder(titledBorder("  Dashboard"));

        JSplitPane bottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                histScroll, dashScroll);
        bottom.setResizeWeight(0.55);
        bottom.setBackground(BG_DARK);
        bottom.setBorder(null);
        bottom.setDividerSize(4);

        JSplitPane main = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tableScroll, bottom);
        main.setResizeWeight(0.62);
        main.setBackground(BG_DARK);
        main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        main.setDividerSize(5);

        return main;
    }

    private void styleTable(JTable t) {
        t.setBackground(BG_TABLE);
        t.setForeground(TEXT_WHITE);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(SEL_BG);
        t.setSelectionForeground(Color.WHITE);
        t.setFillsViewportHeight(true);
        t.setGridColor(new Color(40, 44, 65));

        JTableHeader header = t.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_GRAY);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(50, 55, 80)));
        header.setReorderingAllowed(false);

        // Renderer coloré par statut / priorité
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                setBackground(sel ? SEL_BG : (row % 2 == 0 ? BG_TABLE : BG_ROW_ALT));
                setForeground(TEXT_WHITE);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 8));
                setFont(new Font("SansSerif", Font.PLAIN, 12));

                String s = val == null ? "" : val.toString();

                // Colonne Status (2) → badge coloré
                if (col == 2) {
                    setForeground(statusColor(s));
                    setFont(new Font("Monospaced", Font.BOLD, 11));
                    setText("  " + s + "  ");
                }
                // Colonne Priority (3)
                if (col == 3) {
                    setForeground(priorityColor(s));
                    setFont(new Font("Monospaced", Font.BOLD, 11));
                }
                return this;
            }
        });
    }

    private Color statusColor(String s) {
        return switch (s) {
            case "TODO"        -> TEXT_GRAY;
            case "BLOCKED"     -> COL_RED;
            case "IN_PROGRESS" -> COL_YELLOW;
            case "DONE"        -> COL_GREEN;
            default            -> TEXT_WHITE;
        };
    }

    private Color priorityColor(String s) {
        return switch (s) {
            case "CRITICAL" -> COL_RED;
            case "HIGH"     -> COL_YELLOW;
            case "MEDIUM"   -> COL_BLUE;
            case "LOW"      -> COL_GREEN;
            default         -> TEXT_WHITE;
        };
    }

    // ── Status bar ───────────────────────────────────────────
    private JPanel buildStatus() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_SIDEBAR);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
                new Color(45, 50, 75)));
        bar.setPreferredSize(new Dimension(0, 28));
        statusBar.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusBar.setForeground(TEXT_GRAY);
        bar.add(statusBar, BorderLayout.WEST);

        JLabel version = new JLabel("STRMS v1.0   ");
        version.setFont(new Font("Monospaced", Font.PLAIN, 10));
        version.setForeground(TEXT_DIM);
        bar.add(version, BorderLayout.EAST);
        return bar;
    }

    // ── Utilitaires UI ────────────────────────────────────────
    private JScrollPane darkScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBackground(BG_CARD);
        sp.setBorder(null);
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    private Border titledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 55, 80), 1),
                title);
        tb.setTitleFont(new Font("SansSerif", Font.BOLD, 12));
        tb.setTitleColor(TEXT_GRAY);
        return tb;
    }

    private void setStatus(String s) {
        statusBar.setText("  ● " + s);
        statusBar.setForeground(ACCENT);
    }

    // REFRESH
    private void refreshAll() {
        taskModel.fireTableDataChanged();
        showHistoryForSelected();
        dashArea.setText(buildDashText());
    }

    private String buildDashText() {
        StringBuilder sb = new StringBuilder();
        sb.append("  STRMS Dashboard\n");
        sb.append(String.format("  %-12s %d%n", "Total tasks:", manager.listTasks().size()));
        sb.append(String.format("  %-12s %d%n", "Total users:", manager.listUsers().size()));
        sb.append(String.format("  %-12s %d%n%n", "Overdue:", dashboard.countOverdue()));

        sb.append("  ── By Status ─────────────\n");
        dashboard.tasksByStatus().forEach((k, v) ->
                sb.append(String.format("  %-14s %d%n", k + ":", v)));

        sb.append("\n  ── By Priority ───────────\n");
        dashboard.tasksByPriority().forEach((k, v) ->
                sb.append(String.format("  %-14s %d%n", k + ":", v)));

        sb.append("\n  ── By Engineer ───────────\n");
        dashboard.tasksByUser().forEach((k, v) ->
                sb.append(String.format("  %-14s %d%n", k + ":", v)));
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
        StringBuilder sb = new StringBuilder("History of " + t.getId()
                + " — " + t.getTitle() + "\n"
                + "─".repeat(40) + "\n");
        for (TaskHistoryEntry h : t.getHistory()) sb.append(h).append('\n');
        historyArea.setText(sb.toString());
    }

    // ACTIONS
   
    private void onLogout() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        dispose();                        // ferme la fenêtre actuelle
        onLogout.run();                   // retourne au login
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
        } catch (Exception ex) { showError(ex); }
    }

    private void onDelete() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete task [" + t.getId() + "] " + t.getTitle() + "?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            manager.deleteTask(t.getId(), currentUser);
            setStatus("Task " + t.getId() + " deleted.");
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onAssign() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        List<Engineer> engineers = new ArrayList<>();
        for (User u : manager.listUsers())
            if (u instanceof Engineer) engineers.add((Engineer) u);
        if (engineers.isEmpty()) { warn("No engineer available."); return; }
        Engineer chosen = (Engineer) JOptionPane.showInputDialog(this,
                "Assign engineer to [" + t.getId() + "]:", "Assign Engineer",
                JOptionPane.QUESTION_MESSAGE, null,
                engineers.toArray(), engineers.get(0));
        if (chosen == null) return;
        try {
            manager.assignTask(t.getId(), chosen, currentUser);
            setStatus("Assigned " + chosen.getName() + " to " + t.getId());
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onAddDep() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        String depId = JOptionPane.showInputDialog(this,
                "Task [" + t.getId() + "] depends on (enter ID):");
        if (depId == null || depId.isBlank()) return;
        try {
            manager.addDependency(t.getId(), depId.trim(), currentUser);
            setStatus(t.getId() + " now depends on " + depId.trim());
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onRemoveDep() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        if (t.getDependencies().isEmpty()) { warn("No dependency on this task."); return; }
        Task dep = (Task) JOptionPane.showInputDialog(this,
                "Remove which dependency from [" + t.getId() + "]?",
                "Remove dependency", JOptionPane.QUESTION_MESSAGE, null,
                t.getDependencies().toArray(), t.getDependencies().get(0));
        if (dep == null) return;
        try {
            manager.removeDependency(t.getId(), dep.getId(), currentUser);
            setStatus("Removed dependency " + dep.getId() + " from " + t.getId());
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onStart() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        try {
            manager.startTask(t.getId(), currentUser);
            setStatus("Started " + t.getId());
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onComplete() {
        Task t = selectedTask();
        if (t == null) { warn("Select a task first."); return; }
        try {
            manager.completeTask(t.getId(), currentUser);
            setStatus("Completed " + t.getId() + " ✓");
            notifier.notifyUser(currentUser, NotificationType.EMAIL,
                    "Task " + t.getId() + " completed.");
            refreshAll();
        } catch (Exception ex) { showError(ex); }
    }

    private void onSave() {
        try {
            manager.saveTasksToFile("data/tasks.dat");
            manager.saveUsersToFile("data/users.dat");
            setStatus("Saved → data/tasks.dat & data/users.dat");
        } catch (FilePersistenceException ex) { showError(ex); }
    }

    private void onLoad() {
        try {
            manager.loadUsersFromFile("data/users.dat");
            manager.loadTasksFromFile("data/tasks.dat");
            setStatus("Loaded from data/");
            refreshAll();
        } catch (FilePersistenceException ex) { showError(ex); }
    }

    private void onExport() {
        try {
            String report = reporter.generateReport();
            com.strms.util.FileManager.writeText("data/report.txt", report);
            setStatus("Report exported → data/report.txt");
        } catch (FilePersistenceException ex) { showError(ex); }
    }

    private void doLogout() {
        int ok = JOptionPane.showConfirmDialog(this,
                "Logout from " + currentUser.getName() + "?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        dispose();
        onLogout.run();
    }

    private void warn(String s) {
        JOptionPane.showMessageDialog(this, s, "Notice",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this,
                ex.getMessage(), ex.getClass().getSimpleName(),
                JOptionPane.ERROR_MESSAGE);
        statusBar.setForeground(COL_RED);
        statusBar.setText("  ✖ " + ex.getClass().getSimpleName()
                + ": " + ex.getMessage());
    }

    // ══════════════════════════════════════════════════════════
    // TABLE MODEL
    // ══════════════════════════════════════════════════════════
    private class TaskTableModel extends AbstractTableModel {
        private final String[] cols = {
                "ID", "Title", "Status", "Priority",
                "Category", "Deadline", "Engineer", "Dependencies"
        };
        @Override public int getRowCount()    { return manager.listTasks().size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        Task rowAt(int row) {
            List<Task> list = new ArrayList<>(manager.listTasks());
            return list.get(row);
        }

        @Override
        public Object getValueAt(int row, int col) {
            Task t = rowAt(row);
            return switch (col) {
                case 0 -> t.getId();
                case 1 -> t.getTitle();
                case 2 -> t.getStatus().name();
                case 3 -> t.getPriority().name();
                case 4 -> t.getCategory().name();
                case 5 -> t.getDeadline();
                case 6 -> t.getEngineer() == null ? "—" : t.getEngineer().getName();
                case 7 -> t.getDependencies().stream()
                           .map(Task::getId)
                           .reduce((a, b) -> a + " → " + b).orElse("—");
                default -> "";
            };
        }
    }

    // ══════════════════════════════════════════════════════════
    // INSTALL
    // ══════════════════════════════════════════════════════════
    public void install() {
        taskTable.getSelectionModel().addListSelectionListener(
                e -> showHistoryForSelected());
        setVisible(true);
    }
}
