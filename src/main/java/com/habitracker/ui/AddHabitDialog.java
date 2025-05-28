package com.habitracker.ui;

import com.habitracker.model.Habit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddHabitDialog extends JDialog {

    private JTextField habitNameField;
    private JTextField habitDescriptionField;
    private JButton saveButton;
    private JButton cancelButton;
    private String habitName;
    private String habitDescription;
    private boolean isSaved;

    public AddHabitDialog(JFrame owner) {
        this(owner, null);
    }

    public AddHabitDialog(JFrame owner, Habit habitToEdit) {
        super(owner, (habitToEdit == null ? "Add New Habit" : "Edit Habit"), true);
        this.habitName = null;
        this.habitDescription = null;
        this.isSaved = false;
        initializeUI();

        if (habitToEdit != null) {
            habitNameField.setText(habitToEdit.getName());
            if (habitToEdit.getDescription() != null) {
                habitDescriptionField.setText(habitToEdit.getDescription());
            }
        }
    }

    private void initializeUI() {
        setSize(400, 220);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        fieldsPanel.add(new JLabel("Habit Name:"));
        habitNameField = new JTextField(20);
        fieldsPanel.add(habitNameField);

        fieldsPanel.add(new JLabel("Description (Optional):"));
        habitDescriptionField = new JTextField();
        fieldsPanel.add(habitDescriptionField);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        saveButton = new JButton("Save");
        cancelButton = new JButton("Cancel");

        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);

        add(buttonsPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentHabitName = habitNameField.getText().trim();
                String currentHabitDescription = habitDescriptionField.getText().trim();

                if (!currentHabitName.isEmpty()) {
                    AddHabitDialog.this.habitName = currentHabitName;
                    AddHabitDialog.this.habitDescription = currentHabitDescription;
                    AddHabitDialog.this.isSaved = true;
                    dispose();
                } else {
                    System.out.println("Habit name cannot be empty.");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddHabitDialog.this.isSaved = false;
                dispose();
            }
        });
    }

    public String getHabitName() {
        return this.habitName;
    }

    public String getHabitDescription() {
        return this.habitDescription;
    }

    public boolean isSaved() {
        return this.isSaved;
    }
}