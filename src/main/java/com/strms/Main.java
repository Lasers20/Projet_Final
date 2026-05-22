package com.strms;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskCategory;
import com.strms.gui.LoginDialog;
import com.strms.gui.MainWindow;
import com.strms.manager.TaskManager;
import com.strms.model.*;

import javax.swing.*;
import java.time.LocalDate;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { /* fallback to default */ }

        SwingUtilities.invokeLater(() -> {
            TaskManager manager = new TaskManager();
            seedDemo(manager);

            LoginDialog login = new LoginDialog(null, manager);
            login.setVisible(true);
            User user = login.getSelectedUser();
            if (user == null) { System.exit(0); return; }

            MainWindow window = new MainWindow(manager, user);
            window.install();
        });
    }

    private static void seedDemo(TaskManager manager) {
        Admin alice    = new Admin("U1", "Alice",   "alice@strms.io");
        Manager bob    = new Manager("U2", "Bob",   "bob@strms.io");
        Engineer charlie = new Engineer("U3", "Charlie", "charlie@strms.io");
        Engineer diana   = new Engineer("U4", "Diana",   "diana@strms.io");
        manager.registerUser(alice);
        manager.registerUser(bob);
        manager.registerUser(charlie);
        manager.registerUser(diana);

        try {
            Task t1 = new Task("T1", "Implement login page",
                    "Front-end login + form validation",
                    PriorityLevel.HIGH, TaskCategory.FEATURE,
                    LocalDate.now().plusDays(7));
            Task t2 = new Task("T2", "Create database schema",
                    "Tables, indexes, FK constraints",
                    PriorityLevel.CRITICAL, TaskCategory.FEATURE,
                    LocalDate.now().plusDays(5));
            Task t3 = new Task("T3", "Write user manual",
                    "End-user documentation",
                    PriorityLevel.LOW, TaskCategory.DOCUMENTATION,
                    LocalDate.now().plusDays(14));
            manager.addTask(t1, alice);
            manager.addTask(t2, alice);
            manager.addTask(t3, alice);

            manager.addDependency("T1", "T2", alice);
            manager.assignTask("T1", charlie, bob);
            manager.assignTask("T2", charlie, bob);
            manager.assignTask("T3", diana,   bob);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
