package com.strms.gui;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskCategory;
import com.strms.model.Task;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {

    private final JTextField idField        = new JTextField(15);
    private final JTextField titleField     = new JTextField(20);
    private final JTextArea  descArea       = new JTextArea(3, 20);
    private final JComboBox<PriorityLevel> prioBox =
            new JComboBox<>(PriorityLevel.values());
    private final JComboBox<TaskCategory>  catBox  =
            new JComboBox<>(TaskCategory.values());
    private final JTextField deadlineField  = new JTextField(10);

    private Task result;

    public TaskDialog(Frame owner) {
        super(owner, "Create Task", true);
        setSize(440, 360);
        setLocationRelativeTo(owner);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        int y = 0;
        addRow(form, g, y++, "Task ID:",   idField);
        addRow(form, g, y++, "Title:",     titleField);
        addRow(form, g, y++, "Description:", new JScrollPane(descArea));
        addRow(form, g, y++, "Priority:",  prioBox);
        addRow(form, g, y++, "Category:",  catBox);
        deadlineField.setText(LocalDate.now().plusDays(7).toString());
        addRow(form, g, y++, "Deadline (YYYY-MM-DD):", deadlineField);

        add(form, BorderLayout.CENTER);

        JButton ok = new JButton("Create");
        JButton cancel = new JButton("Cancel");
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(cancel); south.add(ok);
        add(south, BorderLayout.SOUTH);

        ok.addActionListener(e -> tryBuild());
        cancel.addActionListener(e -> { result = null; dispose(); });
    }

    private void addRow(JPanel form, GridBagConstraints g, int y,
                        String label, Component comp) {
        g.gridx = 0; g.gridy = y; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        form.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        form.add(comp, g);
    }

    private void tryBuild() {
        try {
            if (idField.getText().isBlank() || titleField.getText().isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "ID and Title are required.",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            LocalDate deadline = LocalDate.parse(deadlineField.getText().trim());
            result = new Task(
                    idField.getText().trim(),
                    titleField.getText().trim(),
                    descArea.getText().trim(),
                    (PriorityLevel) prioBox.getSelectedItem(),
                    (TaskCategory) catBox.getSelectedItem(),
                    deadline);
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date format. Use YYYY-MM-DD.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Task getResult() {
        return result;
    }
}
