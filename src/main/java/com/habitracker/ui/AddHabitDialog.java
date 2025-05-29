package com.habitracker.ui;

import com.habitracker.model.Habit;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;

public class AddHabitDialog extends JDialog {

    private JTextField nomeField;
    private JTextArea descricaoArea;
    private JButton salvarButton;
    private JButton cancelarButton;

    private HabitTrackerServiceAPI habitService;
    private int usuarioId; // Para associar o hábito ao usuário
    private Habit novoHabito = null;
    private boolean habitoAdicionadoComSucesso = false;
    private boolean usarTemaEscuro;

    // Cores para consistência com o tema (similar ao CreateUserDialog)
    private final Color COR_FUNDO_DIALOGO_ESCURO = new Color(55, 55, 55);
    private final Color COR_PAINEL_INTERNO_ESCURO = new Color(65, 65, 65);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_BORDA_TEXTFIELD_ESCURO = new Color(85,85,85);
    private final Color COR_TEXTFIELD_FUNDO_ESCURO = new Color(50,50,50);
    private final Color COR_BOTAO_SALVAR_ESCURO_BG = new Color(0, 120, 0); // Verde
    private final Color COR_BOTAO_SALVAR_ESCURO_FG = Color.WHITE;
    private final Color COR_BOTAO_CANCELAR_ESCURO_BG = new Color(90, 90, 90);
    private final Color COR_BOTAO_CANCELAR_ESCURO_FG = COR_TEXTO_ESCURO;


    public AddHabitDialog(Frame owner, boolean modal, HabitTrackerServiceAPI service, int currentUserId, boolean usarTemaEscuroGlobal) {
        super(owner, "Adicionar Novo Hábito", modal);
        this.habitService = service;
        this.usuarioId = currentUserId; // Armazena o ID do usuário atual
        this.usarTemaEscuro = usarTemaEscuroGlobal;

        initComponents(); // Chama antes de stylizeComponents
        stylizeComponents();

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                habitoAdicionadoComSucesso = false;
                novoHabito = null;
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

        nomeField = new JTextField(25); // Aumentei um pouco a largura
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nomeField, gbc);
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());


        JLabel descricaoLabel = new JLabel("Descrição:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; // Reset fill
        gbc.anchor = GridBagConstraints.NORTHWEST; // Alinha label no topo
        formPanel.add(descricaoLabel, gbc);

        descricaoArea = new JTextArea(5, 25);
        descricaoArea.setLineWrap(true); // Quebra de linha automática
        descricaoArea.setWrapStyleWord(true); // Quebra por palavra
        JScrollPane scrollPaneDescricao = new JScrollPane(descricaoArea);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPaneDescricao, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(new EmptyBorder(10,0,0,0));

        salvarButton = new JButton("Salvar");
        salvarButton.addActionListener(e -> salvarHabito());
        getRootPane().setDefaultButton(salvarButton);

        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> {
            habitoAdicionadoComSucesso = false;
            novoHabito = null;
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
        JPanel formPanel = (JPanel) mainPanel.getComponent(0); // Assume formPanel é o primeiro
        JPanel buttonPanel = (JPanel) mainPanel.getComponent(1); // Assume buttonPanel é o segundo
        JLabel nomeLabel = (JLabel) formPanel.getComponent(0);
        JLabel descricaoLabel = (JLabel) formPanel.getComponent(2); // Após nomeField

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
            // A borda do JScrollPane é geralmente controlada pelo UIManager
            // Se precisar, pode fazer: ((JScrollPane)formPanel.getComponent(3)).setBorder(...)


            salvarButton.setBackground(COR_BOTAO_SALVAR_ESCURO_BG);
            salvarButton.setForeground(COR_BOTAO_SALVAR_ESCURO_FG);
            salvarButton.setBorder(new EmptyBorder(8,15,8,15));

            cancelarButton.setBackground(COR_BOTAO_CANCELAR_ESCURO_BG);
            cancelarButton.setForeground(COR_BOTAO_CANCELAR_ESCURO_FG);
            cancelarButton.setBorder(new EmptyBorder(8,15,8,15));
        } else {
             formPanel.setOpaque(false);
             buttonPanel.setOpaque(false);
            // Estilos para tema claro (opcional, ou deixar o UIManager padrão)
        }
        nomeLabel.setFont(labelFont);
        descricaoLabel.setFont(labelFont);
        nomeField.setFont(fieldFont);
        descricaoArea.setFont(fieldFont);
        salvarButton.setFont(buttonFont);
        cancelarButton.setFont(buttonFont);
    }


    private void salvarHabito() {
        String nome = nomeField.getText().trim();
        String descricao = descricaoArea.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "O nome do hábito não pode ser vazio.",
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
            nomeField.requestFocusInWindow();
            return;
        }

        // ASSUMINDO que sua classe Habit tem um construtor como:
        // public Habit(int usuarioId, String nome, String descricao, LocalDate creationDate)
        // Ou que você tem um método setUsuarioId(int id) em Habit.
        // Se não, você precisará ajustar a criação do objeto Habit.
        Habit habitParaAdicionar = new Habit(nome, descricao, LocalDate.now(), this.usuarioId);
        habitParaAdicionar.setUsuarioId(this.usuarioId); // ASSUMINDO que Habit tem setUsuarioId()

        try {
            this.novoHabito = habitService.addHabit(habitParaAdicionar);
            this.habitoAdicionadoComSucesso = true;
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this,
                "Erro de validação do backend: " + ve.getMessage(),
                "Erro de Validação",
                JOptionPane.ERROR_MESSAGE);
        } catch (PersistenceException pe) {
            JOptionPane.showMessageDialog(this,
                "Erro ao salvar o hábito: " + pe.getMessage(),
                "Erro de Persistência",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Ocorreu um erro inesperado: " + ex.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isHabitoAdicionadoComSucesso() {
        return habitoAdicionadoComSucesso;
    }

    public Habit getNovoHabito() {
        return novoHabito;
    }
}