package com.habitracker.ui;

import com.habitracker.model.Habit;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.DayOfWeek; 
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;   
import java.util.Set;       
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class EditHabitDialog extends JDialog {

    private JTextField nomeField;
    private JTextArea descricaoArea;
    private JCheckBox chkDefinirHorario;
    private JSpinner spnHoras;
    private JSpinner spnMinutos;
    private JLabel lblHorarioSeparador;
    private JLabel lblHorario;

    
    private JCheckBox chkSeg, chkTer, chkQua, chkQui, chkSex, chkSab, chkDom;

    private JButton salvarButton;
    private JButton cancelarButton;

    private HabitTrackerServiceAPI habitService;
    private Habit habitOriginal; 
    private Habit habitoAtualizado = null; 
    private boolean atualizadoComSucesso = false;
    private boolean usarTemaEscuro;

    
    private final Color COR_FUNDO_DIALOGO_ESCURO = new Color(55, 55, 55);
    private final Color COR_PAINEL_INTERNO_ESCURO = new Color(65, 65, 65);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_BORDA_ESCURO = new Color(85,85,85);
    private final Color COR_TEXTFIELD_FUNDO_ESCURO = new Color(50,50,50);
    private final Color COR_BOTAO_SALVAR_ESCURO_BG = new Color(0, 100, 200); 
    private final Color COR_BOTAO_SALVAR_ESCURO_FG = Color.WHITE;
    private final Color COR_BOTAO_CANCELAR_ESCURO_BG = new Color(90, 90, 90);
    private final Color COR_BOTAO_CANCELAR_ESCURO_FG = COR_TEXTO_ESCURO;


    public EditHabitDialog(Frame owner, boolean modal, HabitTrackerServiceAPI service, Habit habitToEdit, boolean usarTemaEscuroGlobal) {
        super(owner, "Editar Hábito: " + habitToEdit.getName(), modal);
        this.habitService = service;
        this.habitOriginal = habitToEdit; 
        this.usarTemaEscuro = usarTemaEscuroGlobal;

        initComponents();
        stylizeComponents();
        preencherCampos();

        setSize(450, 480); 
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
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(nomeField, gbc);
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());

        
        JLabel descricaoLabel = new JLabel("Descrição:");
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(descricaoLabel, gbc);
        descricaoArea = new JTextArea(3, 25);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane scrollPaneDescricao = new JScrollPane(descricaoArea);
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 3; gbc.weightx = 1.0; gbc.weighty = 0.8; gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(scrollPaneDescricao, gbc);

        
        chkDefinirHorario = new JCheckBox("Definir Horário?");
        chkDefinirHorario.addActionListener(e -> toggleHorarioFields(chkDefinirHorario.isSelected()));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4; gbc.weightx = 0; gbc.weighty = 0; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(chkDefinirHorario, gbc);
        
        
        lblHorario = new JLabel("Horário:");
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(lblHorario, gbc);
        SpinnerNumberModel horasModel = new SpinnerNumberModel(12, 0, 23, 1);
        spnHoras = new JSpinner(horasModel);
        spnHoras.setEditor(new JSpinner.NumberEditor(spnHoras, "00"));
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spnHoras, gbc);
        lblHorarioSeparador = new JLabel(":");
        gbc.gridx = 2; gbc.gridy = 3; gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE;
        formPanel.add(lblHorarioSeparador, gbc);
        SpinnerNumberModel minutosModel = new SpinnerNumberModel(0, 0, 59, 1);
        spnMinutos = new JSpinner(minutosModel);
        spnMinutos.setEditor(new JSpinner.NumberEditor(spnMinutos, "00"));
        gbc.gridx = 3; gbc.gridy = 3; gbc.weightx = 0.1; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(spnMinutos, gbc);

        
        JPanel diasPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        chkSeg = new JCheckBox("Seg"); chkTer = new JCheckBox("Ter"); chkQua = new JCheckBox("Qua");
        chkQui = new JCheckBox("Qui"); chkSex = new JCheckBox("Sex"); chkSab = new JCheckBox("Sáb");
        chkDom = new JCheckBox("Dom");
        JCheckBox[] checkboxesDias = {chkSeg, chkTer, chkQua, chkQui, chkSex, chkSab, chkDom};
        for(JCheckBox chk : checkboxesDias) { diasPanel.add(chk); }
        TitledBorder diasBorder = BorderFactory.createTitledBorder("Dias da Semana");
        diasPanel.setBorder(diasBorder);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(diasPanel, gbc);

        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10,0));
        buttonPanel.setBorder(new EmptyBorder(10,0,0,0));
        salvarButton = new JButton("Salvar Alterações");
        salvarButton.addActionListener(e -> salvarAlteracoesHabito());
        getRootPane().setDefaultButton(salvarButton);
        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(e -> onCancelar());
        buttonPanel.add(cancelarButton);
        buttonPanel.add(salvarButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void toggleHorarioFields(boolean habilitar) {
        lblHorario.setEnabled(habilitar);
        spnHoras.setEnabled(habilitar);
        lblHorarioSeparador.setEnabled(habilitar);
        spnMinutos.setEnabled(habilitar);
    }

    private void stylizeComponents() {
        
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 13);
        Font diasChkFont = new Font("Segoe UI", Font.PLAIN, 12);

        JPanel mainPanel = (JPanel) getContentPane();
        JPanel formPanel = (JPanel) mainPanel.getComponent(0);
        JPanel buttonPanel = (JPanel) mainPanel.getComponent(1);
        
        if (usarTemaEscuro) {
            mainPanel.setBackground(COR_FUNDO_DIALOGO_ESCURO);
            formPanel.setBackground(COR_PAINEL_INTERNO_ESCURO);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_ESCURO), new EmptyBorder(10,10,10,10)));
            buttonPanel.setBackground(COR_FUNDO_DIALOGO_ESCURO);

            for (Component comp : formPanel.getComponents()) {
                if (comp instanceof JLabel) ((JLabel) comp).setForeground(COR_TEXTO_ESCURO);
                 if (comp instanceof JPanel && ((JPanel)comp).getBorder() instanceof TitledBorder) {
                     TitledBorder tb = (TitledBorder) ((JPanel)comp).getBorder();
                     tb.setTitleColor(COR_TEXTO_ESCURO);
                     ((JPanel)comp).setOpaque(false);
                }
            }
            chkDefinirHorario.setForeground(COR_TEXTO_ESCURO);
            chkDefinirHorario.setBackground(COR_PAINEL_INTERNO_ESCURO);
            
            JCheckBox[] checkboxesDias = {chkSeg, chkTer, chkQua, chkQui, chkSex, chkSab, chkDom};
            for(JCheckBox chk : checkboxesDias) {
                 chk.setForeground(COR_TEXTO_ESCURO);
                 chk.setOpaque(false);
                 chk.setFont(diasChkFont);
            }

            nomeField.setBackground(COR_TEXTFIELD_FUNDO_ESCURO); nomeField.setForeground(COR_TEXTO_ESCURO);
            nomeField.setCaretColor(COR_TEXTO_ESCURO);
            nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_ESCURO), new EmptyBorder(5, 8, 5, 8)));

            descricaoArea.setBackground(COR_TEXTFIELD_FUNDO_ESCURO); descricaoArea.setForeground(COR_TEXTO_ESCURO);
            descricaoArea.setCaretColor(COR_TEXTO_ESCURO);
            
            stylizeSpinner(spnHoras);
            stylizeSpinner(spnMinutos);

            salvarButton.setBackground(COR_BOTAO_SALVAR_ESCURO_BG); salvarButton.setForeground(COR_BOTAO_SALVAR_ESCURO_FG);
            salvarButton.setBorder(new EmptyBorder(8,15,8,15));
            cancelarButton.setBackground(COR_BOTAO_CANCELAR_ESCURO_BG); cancelarButton.setForeground(COR_BOTAO_CANCELAR_ESCURO_FG);
            cancelarButton.setBorder(new EmptyBorder(8,15,8,15));
        } else {
             formPanel.setOpaque(false);
            buttonPanel.setOpaque(false);
            chkDefinirHorario.setOpaque(false);
            JCheckBox[] checkboxesDias = {chkSeg, chkTer, chkQua, chkQui, chkSex, chkSab, chkDom};
            for(JCheckBox chk : checkboxesDias) {
                 chk.setOpaque(false);
                 chk.setFont(diasChkFont);
            }
        }
         for (Component comp : formPanel.getComponents()) {
            if (comp instanceof JLabel) ((JLabel) comp).setFont(labelFont);
        }
        chkDefinirHorario.setFont(labelFont);
        nomeField.setFont(fieldFont);
        descricaoArea.setFont(fieldFont);
        salvarButton.setFont(buttonFont);
        cancelarButton.setFont(buttonFont);
    }

    private void stylizeSpinner(JSpinner spinner) {
        if (usarTemaEscuro) {
            spinner.setBorder(BorderFactory.createLineBorder(COR_BORDA_ESCURO));
            Component editor = spinner.getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setForeground(COR_TEXTO_ESCURO);
                tf.setBackground(COR_TEXTFIELD_FUNDO_ESCURO);
                tf.setCaretColor(COR_TEXTO_ESCURO);
                tf.setOpaque(true);
            }
            for (Component comp : spinner.getComponents()) {
                if (comp instanceof JButton) {
                    ((JButton) comp).setBackground(COR_PAINEL_INTERNO_ESCURO);
                }
            }
        }
         if (spinner.getEditor() instanceof JSpinner.DefaultEditor) {
             ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
    }

    private void onCancelar() {
        atualizadoComSucesso = false;
        habitoAtualizado = null;
        dispose();
    }

    private void preencherCampos() {
        if (habitOriginal != null) {
            nomeField.setText(habitOriginal.getName());
            descricaoArea.setText(habitOriginal.getDescription());
            if (habitOriginal.getHorarioOpcional() != null) {
                chkDefinirHorario.setSelected(true);
                toggleHorarioFields(true);
                spnHoras.setValue(habitOriginal.getHorarioOpcional().getHour());
                spnMinutos.setValue(habitOriginal.getHorarioOpcional().getMinute());
            } else {
                chkDefinirHorario.setSelected(false);
                toggleHorarioFields(false);
                spnHoras.setValue(12); 
                spnMinutos.setValue(0);
            }
            
            Set<DayOfWeek> diasSalvos = habitOriginal.getDiasDaSemana();
            if (diasSalvos != null) {
                chkSeg.setSelected(diasSalvos.contains(DayOfWeek.MONDAY));
                chkTer.setSelected(diasSalvos.contains(DayOfWeek.TUESDAY));
                chkQua.setSelected(diasSalvos.contains(DayOfWeek.WEDNESDAY));
                chkQui.setSelected(diasSalvos.contains(DayOfWeek.THURSDAY));
                chkSex.setSelected(diasSalvos.contains(DayOfWeek.FRIDAY));
                chkSab.setSelected(diasSalvos.contains(DayOfWeek.SATURDAY));
                chkDom.setSelected(diasSalvos.contains(DayOfWeek.SUNDAY));
            }
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

        LocalTime horarioOpcional = null;
        if (chkDefinirHorario.isSelected()) {
            int horas = (Integer) spnHoras.getValue();
            int minutos = (Integer) spnMinutos.getValue();
            horarioOpcional = LocalTime.of(horas, minutos);
        }

        
        Set<DayOfWeek> diasSelecionados = new HashSet<>();
        if (chkSeg.isSelected()) diasSelecionados.add(DayOfWeek.MONDAY);
        if (chkTer.isSelected()) diasSelecionados.add(DayOfWeek.TUESDAY);
        if (chkQua.isSelected()) diasSelecionados.add(DayOfWeek.WEDNESDAY);
        if (chkQui.isSelected()) diasSelecionados.add(DayOfWeek.THURSDAY);
        if (chkSex.isSelected()) diasSelecionados.add(DayOfWeek.FRIDAY);
        if (chkSab.isSelected()) diasSelecionados.add(DayOfWeek.SATURDAY);
        if (chkDom.isSelected()) diasSelecionados.add(DayOfWeek.SUNDAY);

        if (diasSelecionados.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione pelo menos um dia da semana para o hábito.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        habitOriginal.setName(nome);
        habitOriginal.setDescription(descricao);
        habitOriginal.setHorarioOpcional(horarioOpcional);
        habitOriginal.setDiasDaSemana(diasSelecionados);
        

        try {
            this.habitoAtualizado = habitService.updateHabit(habitOriginal);
            this.atualizadoComSucesso = true;
            dispose();
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, "Erro de validação: " + ve.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
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