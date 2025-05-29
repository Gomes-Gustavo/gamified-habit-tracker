package com.habitracker.ui; // Ou com.habitracker se você criou a classe lá

import javax.swing.SwingUtilities;
import javax.swing.UIManager; // Import necessário para o Look and Feel

// Imports para as exceções do bloco catch
import javax.swing.UnsupportedLookAndFeelException;
import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.IllegalAccessException;

public class App {

    public static void main(String[] args) {

        // ---- INÍCIO DO CÓDIGO PARA MUDAR O LOOK AND FEEL ----
        try {
            // Procura pelo Look and Feel "Nimbus" entre os instalados
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break; // Encontrou e aplicou o Nimbus, pode sair do loop
                }
            }
        // A linha do catch agora deve funcionar com os imports corretos
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // Se o Nimbus não estiver disponível ou houver outro erro ao configurá-lo,
            // o Swing usará o Look and Feel padrão do sistema operacional.
            System.err.println("Não foi possível aplicar o Look and Feel Nimbus. Usando o padrão: " + e.getMessage());
            // e.printStackTrace(); // Descomente para ver o stack trace completo do erro no console
        }
        // ---- FIM DO CÓDIGO PARA MUDAR O LOOK AND FEEL ----

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}