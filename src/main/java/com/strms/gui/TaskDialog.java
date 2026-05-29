package com.strms.gui;

import com.strms.enums.PriorityLevel;
import com.strms.enums.TaskCategory;
import com.strms.model.Task;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskDialog extends JDialog {

    private static final Color BG      = new Color(15, 17, 26);
    private static final Color BG_CARD = new Color(22, 25, 37);
    private static final Color ACCENT  = new Color(99, 102, 241);
    private static final Color TEXT    = new Color(235, 237, 255);
    private static final Color SUBTEXT = new Color(140, 145, 175);
    private static final Color BORDER_C= new Color(50, 55, 80);
    private static final Color INPUT_BG= new Color(30, 33, 48);

    private final JTextField idField       = darkField();
    private final JTextField titleField    = darkField();
    private final JTextArea  descArea      = new JTextArea(3, 0);
    private final JComboBox<PriorityLevel> prioBox = new JComboBox<>(PriorityLevel.values());
    private final JComboBox<TaskCategory>  catBox  = new JComboBox<>(TaskCategory.values());
    private final JTextField deadlineField = darkField();

    private Task result;

    public TaskDialog(Frame owner) {
        super(owner, "Create New Task", true);
        setSize(480, 420);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_CARD);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_C),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)));
        JLabel title = new JLabel("➕  Create New Task");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT);
        header.add(title);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        GridBagConstraints g = new GridBagConstraints();
        g.insets  = new Insets(6, 0, 6, 0);
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        deadlineField.setText(LocalDate.now().plusDays(7).toString());

        styleCombo(prioBox);
        styleCombo(catBox);

        descArea.setBackground(INPUT_BG);
        descArea.setForeground(TEXT);
        descArea.setCaretColor(TEXT);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        int row = 0;
        addFormRow(form, g, row++, "Task ID",              idField);
        addFormRow(form, g, row++, "Title",                titleField);
        addFormRow(form, g, row++, "Description",          new JScrollPane(descArea) {{
            setBackground(INPUT_BG); getViewport().setBackground(INPUT_BG); setBorder(BorderFactory.createLineBorder(BORDER_C));
        }});
        addFormRow(form, g, row++, "Priority",             prioBox);
        addFormRow(form, g, row++, "Category",             catBox);
        addFormRow(form, g, row++, "Deadline (YYYY-MM-DD)", deadlineField);

        // Buttons
        JButton btnCreate = makeBtn("Create Task", ACCENT);
        JButton btnCancel = makeBtn("Cancel", new Color(55, 60, 85));
        btnCreate.addActionListener(e -> tryBuild());
        btnCancel.addActionListener(e -> { result = null; dispose(); });
        getRootPane().setDefaultButton(btnCreate);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setBackground(BG);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 16, 20));
        btnPanel.add(btnCancel);
        btnPanel.add(btnCreate);

        add(header,   BorderLayout.NORTH);
        add(form,     BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel form, GridBagConstraints g, int y,
                             String label, Component comp) {
        g.gridx = 0; g.gridy = y * 2;
        g.weighty = 0; g.fill = GridBagConstraints.HORIZONTAL;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(SUBTEXT);
        form.add(lbl, g);

        g.gridy = y * 2 + 1;
        form.add(comp, g);
    }

    private JTextField darkField() {
        JTextField f = new JTextField();
        f.setBackground(INPUT_BG);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_C),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT);
        cb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER_C));
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(0, 38));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    private void tryBuild() {
        if (idField.getText().isBlank() || titleField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "ID and Title are required.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            LocalDate deadline = LocalDate.parse(deadlineField.getText().trim());
            result = new Task(
                    idField.getText().trim(),
                    titleField.getText().trim(),
                    descArea.getText().trim(),
                    (PriorityLevel) prioBox.getSelectedItem(),
                    (TaskCategory)  catBox.getSelectedItem(),
                    deadline);
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid date. Use format YYYY-MM-DD.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
        }
    }

    public Task getResult() { return result; }
}
