package com.habitracker.ui;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HabitPanel extends JPanel {

    private JList<String> habitList;
    private DefaultListModel<String> habitListModel;
    private JButton addHabitButton;

    public HabitPanel() {
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        habitListModel = new DefaultListModel<>();
        habitListModel.addElement("Habit 1: Drink 2L of water");
        habitListModel.addElement("Habit 2: Exercise for 30 minutes");
        habitListModel.addElement("Habit 3: Read for 15 minutes");

        habitList = new JList<>(habitListModel);
        habitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitList.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane listScrollPane = new JScrollPane(habitList);

        addHabitButton = new JButton("Add New Habit");
        addHabitButton.setFont(new Font("Arial", Font.BOLD, 14));

        addHabitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(HabitPanel.this);
                AddHabitDialog addDialog = new AddHabitDialog(parentFrame);
                addDialog.setVisible(true);

                String newHabitName = addDialog.getHabitName();
                String newHabitDescription = addDialog.getHabitDescription();

                if (newHabitName != null && !newHabitName.isEmpty()) {
                    String habitEntry = newHabitName;
                    if (newHabitDescription != null && !newHabitDescription.isEmpty()) {
                        habitEntry += " (Description: " + newHabitDescription + ")";
                    }
                    habitListModel.addElement(habitEntry);
                    System.out.println("Added to list: " + habitEntry);
                } else {
                    System.out.println("No new habit added from dialog.");
                }
            }
        });

        add(listScrollPane, BorderLayout.CENTER);
        add(addHabitButton, BorderLayout.SOUTH);
    }
}