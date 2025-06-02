package com.habitracker.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CreateUserDialog extends JDialog {
    private JTextField nomeField;
    private JButton criarButton;
    private JButton cancelarButton;
    private String nomeUsuarioCriado = null; 

    
    
    
    private final Color COR_FUNDO_DIALOGO_ESCURO = new Color(55, 55, 55);
    private final Color COR_PAINEL_INTERNO_ESCURO = new Color(65, 65, 65);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_BORDA_TEXTFIELD_ESCURO = new Color(85,85,85);
    private final Color COR_TEXTFIELD_FUNDO_ESCURO = new Color(50,50,50);

    private final Color COR_BOTAO_CRIAR_ESCURO_BG = new Color(0, 120, 0); 
    private final Color COR_BOTAO_CRIAR_ESCURO_FG = Color.WHITE;
    private final Color COR_BOTAO_CANCELAR_ESCURO_BG = new Color(90, 90, 90);
    private final Color COR_BOTAO_CANCELAR_ESCURO_FG = COR_TEXTO_ESCURO;

    private final boolean usarTemaEscuro;

    public CreateUserDialog(Frame owner, boolean modal, boolean usarTemaEscuroGlobal) {
        super(owner, "Criar Novo Usuário", modal);
        this.usarTemaEscuro = usarTemaEscuroGlobal;

        initComponents();
        stylizeComponents();

        
        setSize(380, 200);
        setResizable(false);
        setLocationRelativeTo(owner); 
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); 

        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                nomeUsuarioCriado = null;
            }
        });
    }

    private void initComponents() {
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nomeLabel = new JLabel("Nome do Usuário:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; 
        formPanel.add(nomeLabel, gbc);

        nomeField = new JTextField(20); 
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; 
        formPanel.add(nomeField, gbc);
        
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());


        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        criarButton = new JButton("Criar");
        criarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCriar();
            }
        });
        
        getRootPane().setDefaultButton(criarButton);


        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelar();
            }
        });

        buttonPanel.add(cancelarButton);
        buttonPanel.add(criarButton); 

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void stylizeComponents() {
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);

        
        if (usarTemaEscuro) {
            getContentPane().setBackground(COR_FUNDO_DIALOGO_ESCURO);
            
            JPanel formPanel = (JPanel) ((JPanel) getContentPane()).getComponent(0); 
            formPanel.setBackground(COR_PAINEL_INTERNO_ESCURO);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(10,10,10,10)
            ));


            JLabel nomeLabel = (JLabel) formPanel.getComponent(0); 
            nomeLabel.setForeground(COR_TEXTO_ESCURO);
            nomeLabel.setFont(labelFont);

            nomeField.setBackground(COR_TEXTFIELD_FUNDO_ESCURO);
            nomeField.setForeground(COR_TEXTO_ESCURO);
            nomeField.setCaretColor(COR_TEXTO_ESCURO); 
            nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(5, 8, 5, 8) 
            ));
            nomeField.setFont(labelFont);

            JPanel buttonPanel = (JPanel) ((JPanel) getContentPane()).getComponent(1); 
            buttonPanel.setBackground(COR_FUNDO_DIALOGO_ESCURO);

            criarButton.setBackground(COR_BOTAO_CRIAR_ESCURO_BG);
            criarButton.setForeground(COR_BOTAO_CRIAR_ESCURO_FG);
            criarButton.setFont(buttonFont);
            criarButton.setBorder(new EmptyBorder(8,15,8,15));


            cancelarButton.setBackground(COR_BOTAO_CANCELAR_ESCURO_BG);
            cancelarButton.setForeground(COR_BOTAO_CANCELAR_ESCURO_FG);
            cancelarButton.setFont(buttonFont);
            cancelarButton.setBorder(new EmptyBorder(8,15,8,15));

        } else {
            
            
            Component formComp = ((JPanel) getContentPane()).getComponent(0); 
            if (formComp instanceof JComponent) {
                ((JComponent) formComp).setOpaque(false);
            }

            Component buttonComp = ((JPanel) getContentPane()).getComponent(1); 
            if (buttonComp instanceof JComponent) {
                ((JComponent) buttonComp).setOpaque(false);
            }
        }
    }

    private void onCriar() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            
            
            JOptionPane.showMessageDialog(this,
                    "O nome do usuário não pode ser vazio.",
                    "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
            nomeField.requestFocusInWindow(); 
            return;
        }
        

        nomeUsuarioCriado = nome;
        dispose(); 
    }

    private void onCancelar() {
        nomeUsuarioCriado = null; 
        dispose(); 
    }

    
    public String getNomeUsuarioCriado() {
        return nomeUsuarioCriado;
    }
}