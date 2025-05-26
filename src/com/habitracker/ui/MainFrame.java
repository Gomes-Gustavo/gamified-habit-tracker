package com.habitracker.ui; 

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;

public class MainFrame extends JFrame {

    private HabitPanel habitPanel;

    public MainFrame() throws HeadlessException {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("HabitTracker - Gamified");
        setSize(800, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        habitPanel = new HabitPanel();
        add(habitPanel, BorderLayout.CENTER);
    }
}