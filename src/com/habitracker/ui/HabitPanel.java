package com.habitracker.ui;

import com.habitracker.model.Habit;
import com.habitracker.service.HabitService;
import com.habitracker.service.MockHabitService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class HabitPanel extends JPanel {

    private JList<Habit> habitList;
    private DefaultListModel<Habit> habitListModel;
    private JButton addHabitButton;
    private JButton removeHabitButton;
    private JButton editHabitButton;

    private HabitService habitService;

    public HabitPanel() {
        this.habitService = new MockHabitService();
        initializePanel();
        loadHabits();
    }

    private void initializePanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        habitListModel = new DefaultListModel<>();
        habitList = new JList<>(habitListModel);
        habitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitList.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane listScrollPane = new JScrollPane(habitList);

        addHabitButton = new JButton("Add New Habit");
        addHabitButton.setFont(new Font("Arial", Font.BOLD, 14));

        editHabitButton = new JButton("Edit Selected Habit");
        editHabitButton.setFont(new Font("Arial", Font.BOLD, 14));

        removeHabitButton = new JButton("Remove Selected Habit");
        removeHabitButton.setFont(new Font("Arial", Font.BOLD, 14));

        addHabitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAddHabit();
            }
        });

        editHabitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleEditHabit();
            }
        });

        removeHabitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleRemoveHabit();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(addHabitButton);
        buttonPanel.add(editHabitButton);
        buttonPanel.add(removeHabitButton);

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadHabits() {
        List<Habit> habits = habitService.getAllHabits();
        habitListModel.clear();
        for (Habit habit : habits) {
            habitListModel.addElement(habit);
        }
    }

    private void handleAddHabit() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        AddHabitDialog addDialog = new AddHabitDialog(parentFrame);
        addDialog.setVisible(true);

        if (addDialog.isSaved()) {
            String newHabitName = addDialog.getHabitName();
            String newHabitDescription = addDialog.getHabitDescription();
            Habit habitToAdd = new Habit(newHabitName, newHabitDescription);
            
            Habit addedHabit = habitService.addHabit(habitToAdd);
            if (addedHabit != null) {
                habitListModel.addElement(addedHabit);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add habit.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditHabit() {
        int selectedIndex = habitList.getSelectedIndex();
        if (selectedIndex != -1) {
            Habit habitToEdit = habitListModel.getElementAt(selectedIndex);
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            AddHabitDialog editDialog = new AddHabitDialog(parentFrame, habitToEdit);
            editDialog.setVisible(true);

            if (editDialog.isSaved()) {
                String updatedName = editDialog.getHabitName();
                String updatedDescription = editDialog.getHabitDescription();

                Habit habitWithUpdates = new Habit(habitToEdit.getId(), updatedName, updatedDescription);
                
                Habit updatedHabit = habitService.updateHabit(habitWithUpdates);
                if (updatedHabit != null) {
                    habitListModel.setElementAt(updatedHabit, selectedIndex);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update habit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select a habit to edit.",
                "No Habit Selected",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void handleRemoveHabit() {
        int selectedIndex = habitList.getSelectedIndex();
        if (selectedIndex != -1) {
            Habit selectedHabit = habitListModel.getElementAt(selectedIndex);
            int confirmation = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove the habit: \"" + selectedHabit.getName() + "\"?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirmation == JOptionPane.YES_OPTION) {
                boolean deleted = habitService.deleteHabit(selectedHabit.getId());
                if (deleted) {
                    habitListModel.remove(selectedIndex);
                } else {
                     JOptionPane.showMessageDialog(this, "Failed to remove habit.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Please select a habit to remove.",
                "No Habit Selected",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}