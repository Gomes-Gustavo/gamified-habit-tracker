package com.habitracker.ui;

import com.habitracker.backend.ObjetivoService;
import com.habitracker.model.Objetivo;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.ValidationException;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date; // Para setar no JDateChooser

public class EditObjetivoDialog extends JDialog {
    private JTextField nomeField;
    private JTextArea descricaoArea;
    private JDateChooser dataMetaChooser;
    private JButton salvarButton;
    private JButton cancelarButton;

    private ObjetivoService objetivoService;
    private Objetivo objetivoParaEditar;
    private boolean objetivoAtualizadoComSucesso = false;
    private boolean usarTemaEscuro;


    public EditObjetivoDialog(Frame owner, boolean modal, ObjetivoService objetivoService, Objetivo objetivo, boolean usarTemaEscuro) {
        super(owner, modal);
        this.objetivoService = objetivoService;
        this.objetivoParaEditar = objetivo;
        this.usarTemaEscuro = usarTemaEscuro;

        setTitle("Editar Objetivo");
        initComponents();
        carregarDadosObjetivo();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        // Cores e Fontes (similar ao AddObjetivoDialog)
        Color corFundoDialog = usarTemaEscuro ? new Color(45, 45, 45) : new Color(240, 240, 240);
        Color corTextoLabel = usarTemaEscuro ? new Color(220, 220, 220) : Color.BLACK;
        Color corFundoCampo = usarTemaEscuro ? new Color(60, 60, 60) : Color.WHITE;
        Color corTextoCampo = usarTemaEscuro ? new Color(220, 220, 220) : Color.BLACK;
        Font fonteLabel = new Font("Segoe UI", Font.BOLD, 14);
        Font fonteCampo = new Font("Segoe UI", Font.PLAIN, 14);

        getContentPane().setBackground(corFundoDialog);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nome
        JLabel nomeLabel = new JLabel("Nome do Objetivo:");
        nomeLabel.setFont(fonteLabel);
        nomeLabel.setForeground(corTextoLabel);
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(nomeLabel, gbc);

        nomeField = new JTextField(25);
        nomeField.setFont(fonteCampo);
        nomeField.setBackground(corFundoCampo);
        nomeField.setForeground(corTextoCampo);
        nomeField.setCaretColor(corTextoCampo);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        formPanel.add(nomeField, gbc);

        // Descrição
        JLabel descricaoLabel = new JLabel("Descrição:");
        descricaoLabel.setFont(fonteLabel);
        descricaoLabel.setForeground(corTextoLabel);
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(descricaoLabel, gbc);

        descricaoArea = new JTextArea(4, 25);
        descricaoArea.setFont(fonteCampo);
        descricaoArea.setBackground(corFundoCampo);
        descricaoArea.setForeground(corTextoCampo);
        descricaoArea.setCaretColor(corTextoCampo);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descricaoArea);
        descScrollPane.getViewport().setOpaque(false);
        descScrollPane.setOpaque(false);
        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0;
        formPanel.add(descScrollPane, gbc);
        gbc.weighty = 0; gbc.fill = GridBagConstraints.HORIZONTAL; // Resetar

        // Data Meta
        JLabel dataMetaLabel = new JLabel("Data Meta (Opcional):");
        dataMetaLabel.setFont(fonteLabel);
        dataMetaLabel.setForeground(corTextoLabel);
        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(dataMetaLabel, gbc);

        dataMetaChooser = new JDateChooser();
        dataMetaChooser.setDateFormatString("dd/MM/yyyy");
        dataMetaChooser.setFont(fonteCampo);
        if (usarTemaEscuro) {
             dataMetaChooser.setBackground(corFundoDialog);
            dataMetaChooser.getCalendarButton().setBackground(new Color(80,80,80));
            dataMetaChooser.getCalendarButton().setForeground(Color.WHITE);
            JTextFieldDateEditor editor = (JTextFieldDateEditor) dataMetaChooser.getDateEditor();
            editor.setBackground(corFundoCampo);
            editor.setForeground(corTextoCampo);
            editor.setOpaque(true);
        }
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(dataMetaChooser, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        salvarButton = new JButton("Salvar Alterações");
        salvarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
         if (usarTemaEscuro) {
            salvarButton.setBackground(new Color(0, 120, 0));
            salvarButton.setForeground(Color.WHITE);
        } else {
            salvarButton.setBackground(new Color(200, 255, 200));
        }


        cancelarButton = new JButton("Cancelar");
        cancelarButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (usarTemaEscuro) {
            cancelarButton.setBackground(new Color(80,80,80));
            cancelarButton.setForeground(Color.WHITE);
        }


        salvarButton.addActionListener(e -> salvarAlteracoesObjetivo());
        cancelarButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelarButton);
        buttonPanel.add(salvarButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void carregarDadosObjetivo() {
        nomeField.setText(objetivoParaEditar.getNome());
        descricaoArea.setText(objetivoParaEditar.getDescricao());
        if (objetivoParaEditar.getDataMeta() != null) {
            // Convert LocalDate to java.util.Date for JDateChooser
            Date dataMetaUtil = Date.from(objetivoParaEditar.getDataMeta().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dataMetaChooser.setDate(dataMetaUtil);
        } else {
            dataMetaChooser.setDate(null);
        }
    }

    private void salvarAlteracoesObjetivo() {
        String nome = nomeField.getText().trim();
        String descricao = descricaoArea.getText().trim();

        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "O nome do objetivo não pode ser vazio.", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            return;
        }

        objetivoParaEditar.setNome(nome);
        objetivoParaEditar.setDescricao(descricao);

        LocalDate dataMeta = null;
        if (dataMetaChooser.getDate() != null) {
            dataMeta = dataMetaChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        objetivoParaEditar.setDataMeta(dataMeta);

        try {
            // A lista de IDs de hábitos vinculados seria obtida de outro componente na UI, se houver.
            // Para este exemplo, passarei uma lista vazia ou os IDs existentes se você os carregar.
            objetivoService.updateObjetivo(objetivoParaEditar, new ArrayList<>()); // ou objetivoService.getHabitoIdsForObjetivo(objetivoParaEditar.getId())
            objetivoAtualizadoComSucesso = true;
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, "Erro de Validação: " + ve.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (PersistenceException pe) {
            JOptionPane.showMessageDialog(this, "Erro de Persistência: " + pe.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            pe.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public boolean isObjetivoAtualizadoComSucesso() {
        return objetivoAtualizadoComSucesso;
    }

    public Objetivo getObjetivoEditado() { // Para retornar o objeto atualizado se necessário
        return objetivoParaEditar;
    }
}