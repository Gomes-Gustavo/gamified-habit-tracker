package com.habitracker.ui;

import com.habitracker.model.Habit;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EditHabitDialog extends JDialog {

    private JTextField nomeField;
    private JTextArea descricaoArea;
    private JButton salvarButton;
    private JButton cancelarButton;

    private HabitTrackerServiceAPI habitService;
    private Habit habitOriginal;
    private Habit habitoAtualizado = null;
    private boolean atualizadoComSucesso = false;
    private boolean usarTemaEscuro;

    // Cores (similar ao AddHabitDialog)
    private final Color COR_FUNDO_DIALOGO_ESCURO = new Color(55, 55, 55);
    private final Color COR_PAINEL_INTERNO_ESCURO = new Color(65, 65, 65);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_BORDA_TEXTFIELD_ESCURO = new Color(85,85,85);
    private final Color COR_TEXTFIELD_FUNDO_ESCURO = new Color(50,50,50);
    private final Color COR_BOTAO_SALVAR_ESCURO_BG = new Color(0, 100, 200); // Azul para salvar alterações
    private final Color COR_BOTAO_SALVAR_ESCURO_FG = Color.WHITE;
    private final Color COR_BOTAO_CANCELAR_ESCURO_BG = new Color(90, 90, 90);
    private final Color COR_BOTAO_CANCELAR_ESCURO_FG = COR_TEXTO_ESCURO;


    public EditHabitDialog(Frame owner, boolean modal, HabitTrackerServiceAPI service, Habit habitToEdit, boolean usarTemaEscuroGlobal) {
        super(owner, "Editar Hábito: " + habitToEdit.getName(), modal);
        this.habitService = service;
        this.habitOriginal = habitToEdit;
        this.usarTemaEscuro = usarTemaEscuroGlobal;

        initComponents(); // Chama antes de stylizeComponents
        stylizeComponents();
        preencherCampos();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                atualizadoComSucesso = false;
                habitoAtualizado = null;
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

        JLabel nomeLabel = new JLabel("Nome:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formPanel.add(nomeLabel, gbc);

        nomeField = new JTextField(25);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nomeField, gbc);
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());

        JLabel descricaoLabel = new JLabel("Descrição:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(descricaoLabel, gbc);

        descricaoArea = new JTextArea(5, 25);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane scrollPaneDescricao = new JScrollPane(descricaoArea);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPaneDescricao, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        buttonPanel.setBorder(new EmptyBorder(10,0,0,0));

        salvarButton = new JButton("Salvar Alterações");
        salvarButton.addActionListener(e -> salvarAlteracoesHabito());
        getRootPane().setDefaultButton(salvarButton);

        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> {
            atualizadoComSucesso = false;
            habitoAtualizado = null;
            dispose();
        });

        buttonPanel.add(cancelarButton);
        buttonPanel.add(salvarButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void stylizeComponents() {
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);

        JPanel mainPanel = (JPanel) getContentPane();
        JPanel formPanel = (JPanel) mainPanel.getComponent(0);
        JPanel buttonPanel = (JPanel) mainPanel.getComponent(1);
        JLabel nomeLabel = (JLabel) formPanel.getComponent(0);
        JLabel descricaoLabel = (JLabel) formPanel.getComponent(2);

        if (usarTemaEscuro) {
            mainPanel.setBackground(COR_FUNDO_DIALOGO_ESCURO);
            formPanel.setBackground(COR_PAINEL_INTERNO_ESCURO);
             formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(10,10,10,10)
            ));
            buttonPanel.setBackground(COR_FUNDO_DIALOGO_ESCURO);

            nomeLabel.setForeground(COR_TEXTO_ESCURO);
            descricaoLabel.setForeground(COR_TEXTO_ESCURO);

            nomeField.setBackground(COR_TEXTFIELD_FUNDO_ESCURO);
            nomeField.setForeground(COR_TEXTO_ESCURO);
            nomeField.setCaretColor(COR_TEXTO_ESCURO);
            nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(5, 8, 5, 8)
            ));

            descricaoArea.setBackground(COR_TEXTFIELD_FUNDO_ESCURO);
            descricaoArea.setForeground(COR_TEXTO_ESCURO);
            descricaoArea.setCaretColor(COR_TEXTO_ESCURO);

            salvarButton.setBackground(COR_BOTAO_SALVAR_ESCURO_BG);
            salvarButton.setForeground(COR_BOTAO_SALVAR_ESCURO_FG);
            salvarButton.setBorder(new EmptyBorder(8,15,8,15));


            cancelarButton.setBackground(COR_BOTAO_CANCELAR_ESCURO_BG);
            cancelarButton.setForeground(COR_BOTAO_CANCELAR_ESCURO_FG);
            cancelarButton.setBorder(new EmptyBorder(8,15,8,15));
        } else {
            formPanel.setOpaque(false);
            buttonPanel.setOpaque(false);
        }
        nomeLabel.setFont(labelFont);
        descricaoLabel.setFont(labelFont);
        nomeField.setFont(fieldFont);
        descricaoArea.setFont(fieldFont);
        salvarButton.setFont(buttonFont);
        cancelarButton.setFont(buttonFont);
    }

    private void preencherCampos() {
        if (habitOriginal != null) {
            nomeField.setText(habitOriginal.getName());
            descricaoArea.setText(habitOriginal.getDescription());
        }
    }

    private void salvarAlteracoesHabito() {
        String nome = nomeField.getText().trim();
        String descricao = descricaoArea.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do hábito não pode ser vazio.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            nomeField.requestFocusInWindow();
            return;
        }

        Habit habitParaAtualizar = new Habit(
            habitOriginal.getId(),
            nome,
            descricao,
            habitOriginal.getCreationDate(),
            habitOriginal.getUsuarioId() // Preserva o usuarioId original
        );
       
        if (habitOriginal.getUsuarioId() > 0) { // Supondo que getUsuarioId() existe e >0 é válido
             habitParaAtualizar.setUsuarioId(habitOriginal.getUsuarioId());
        }


        try {
            this.habitoAtualizado = habitService.updateHabit(habitParaAtualizar);
            this.atualizadoComSucesso = true;
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, "Erro de validação do backend: " + ve.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (HabitNotFoundException hnfe) {
            JOptionPane.showMessageDialog(this, "Erro: " + hnfe.getMessage(), "Hábito Não Encontrado", JOptionPane.ERROR_MESSAGE);
            dispose();
        } catch (PersistenceException pe) {
            JOptionPane.showMessageDialog(this, "Erro ao atualizar o hábito: " + pe.getMessage(), "Erro de Persistência", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isHabitoAtualizadoComSucesso() {
        return atualizadoComSucesso;
    }

    public Habit getHabitoAtualizado() {
        return habitoAtualizado;
    }
}