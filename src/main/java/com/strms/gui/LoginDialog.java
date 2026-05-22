package com.strms.gui;

import com.strms.manager.TaskManager;
import com.strms.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LoginDialog extends JDialog {

    private User selected;
    private final JComboBox<UserItem> combo;

    public LoginDialog(Frame owner, TaskManager manager) {
        super(owner, "STRMS - Login", true);
        setSize(360, 160);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        List<UserItem> items = new ArrayList<>();
        for (User u : manager.listUsers()) items.add(new UserItem(u));
        combo = new JComboBox<>(items.toArray(new UserItem[0]));

        JPanel center = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        center.add(new JLabel("Select user:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        center.add(combo, g);
        add(center, BorderLayout.CENTER);

        JButton ok     = new JButton("Login");
        JButton cancel = new JButton("Cancel");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancel); south.add(ok);
        add(south, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            UserItem item = (UserItem) combo.getSelectedItem();
            if (item != null) selected = item.user;
            dispose();
        });
        cancel.addActionListener(e -> { selected = null; dispose(); });
    }

    public User getSelectedUser() {
        return selected;
    }

    private static class UserItem {
        final User user;
        UserItem(User u) { this.user = u; }
        @Override public String toString() {
            return user.getName() + " (" + user.getRole() + ")";
        }
    }
}
