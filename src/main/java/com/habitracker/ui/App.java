package com.habitracker.ui; 

import javax.swing.SwingUtilities;
import javax.swing.UIManager; 


import javax.swing.UnsupportedLookAndFeelException;
import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;

public class App {

    public static void main(String[] args) {

        
        try {
            
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break; 
                }
            }
        
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            
            
            System.err.println("Não foi possível aplicar o Look and Feel Nimbus. Usando o padrão: " + e.getMessage());
            
        }
        

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}