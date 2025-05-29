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
    private String nomeUsuarioCriado = null; // Armazena o nome se o usuário clicar em "Criar"

    // Cores para consistência com o tema do MainFrame
    // Idealmente, estas cores viriam de uma classe de tema compartilhada
    // ou seriam passadas como parâmetro se fossem mais dinâmicas.
    private final Color COR_FUNDO_DIALOGO_ESCURO = new Color(55, 55, 55);
    private final Color COR_PAINEL_INTERNO_ESCURO = new Color(65, 65, 65);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_BORDA_TEXTFIELD_ESCURO = new Color(85,85,85);
    private final Color COR_TEXTFIELD_FUNDO_ESCURO = new Color(50,50,50);

    private final Color COR_BOTAO_CRIAR_ESCURO_BG = new Color(0, 120, 0); // Verde escuro
    private final Color COR_BOTAO_CRIAR_ESCURO_FG = Color.WHITE;
    private final Color COR_BOTAO_CANCELAR_ESCURO_BG = new Color(90, 90, 90);
    private final Color COR_BOTAO_CANCELAR_ESCURO_FG = COR_TEXTO_ESCURO;

    private final boolean usarTemaEscuro;

    public CreateUserDialog(Frame owner, boolean modal, boolean usarTemaEscuroGlobal) {
        super(owner, "Criar Novo Usuário", modal);
        this.usarTemaEscuro = usarTemaEscuroGlobal;

        initComponents();
        stylizeComponents();

        // Define o tamanho e a posição do diálogo
        setSize(380, 200);
        setResizable(false);
        setLocationRelativeTo(owner); // Centraliza em relação à janela pai
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Libera recursos ao fechar

        // Garante que nomeUsuarioCriado seja null se o diálogo for fechado no 'X'
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                nomeUsuarioCriado = null;
            }
        });
    }

    private void initComponents() {
        // Painel principal com padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        setContentPane(mainPanel);

        // Painel do formulário para o nome
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel nomeLabel = new JLabel("Nome do Usuário:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0; // Não estica o label
        formPanel.add(nomeLabel, gbc);

        nomeField = new JTextField(20); // Largura sugerida de 20 caracteres
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // Permite que o campo de texto estique horizontalmente
        formPanel.add(nomeField, gbc);
        // Define o foco inicial no campo de nome
        SwingUtilities.invokeLater(() -> nomeField.requestFocusInWindow());


        // Painel dos botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        // Remove o padding inferior padrão do FlowLayout para melhor ajuste
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));


        criarButton = new JButton("Criar");
        criarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCriar();
            }
        });
        // Define o botão "Criar" como o botão padrão (ativado com Enter)
        getRootPane().setDefaultButton(criarButton);


        cancelarButton = new JButton("Cancelar");
        cancelarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancelar();
            }
        });

        buttonPanel.add(cancelarButton);
        buttonPanel.add(criarButton); // Ordem visual: Cancelar, Criar

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void stylizeComponents() {
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 12);

        // Aplica estilos se o tema escuro estiver ativo
        if (usarTemaEscuro) {
            getContentPane().setBackground(COR_FUNDO_DIALOGO_ESCURO);
            
            JPanel formPanel = (JPanel) ((JPanel) getContentPane()).getComponent(0); // Assume que formPanel é o primeiro
            formPanel.setBackground(COR_PAINEL_INTERNO_ESCURO);
            formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(10,10,10,10)
            ));


            JLabel nomeLabel = (JLabel) formPanel.getComponent(0); // Assume que nomeLabel é o primeiro no formPanel
            nomeLabel.setForeground(COR_TEXTO_ESCURO);
            nomeLabel.setFont(labelFont);

            nomeField.setBackground(COR_TEXTFIELD_FUNDO_ESCURO);
            nomeField.setForeground(COR_TEXTO_ESCURO);
            nomeField.setCaretColor(COR_TEXTO_ESCURO); // Cor do cursor
            nomeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_TEXTFIELD_ESCURO),
                new EmptyBorder(5, 8, 5, 8) // Padding interno
            ));
            nomeField.setFont(labelFont);

            JPanel buttonPanel = (JPanel) ((JPanel) getContentPane()).getComponent(1); // Assume que buttonPanel é o segundo
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
            // Estilos para tema claro (opcional, ou deixar o UIManager padrão)
            // CORREÇÃO: Fazer cast para JComponent antes de chamar setOpaque
            Component formComp = ((JPanel) getContentPane()).getComponent(0); // Assumindo que formPanel é o primeiro
            if (formComp instanceof JComponent) {
                ((JComponent) formComp).setOpaque(false);
            }

            Component buttonComp = ((JPanel) getContentPane()).getComponent(1); // Assumindo que buttonPanel é o segundo
            if (buttonComp instanceof JComponent) {
                ((JComponent) buttonComp).setOpaque(false);
            }
        }
    }

    private void onCriar() {
        String nome = nomeField.getText().trim();
        if (nome.isEmpty()) {
            // Personalizar JOptionPane para tema escuro se necessário, mas é complexo
            // Por enquanto, usará o estilo do UIManager definido no MainFrame
            JOptionPane.showMessageDialog(this,
                    "O nome do usuário não pode ser vazio.",
                    "Erro de Validação",
                    JOptionPane.ERROR_MESSAGE);
            nomeField.requestFocusInWindow(); // Devolve o foco ao campo
            return;
        }
        // Adicionar mais validações se necessário (ex: comprimento máximo, caracteres especiais)

        nomeUsuarioCriado = nome;
        dispose(); // Fecha o diálogo
    }

    private void onCancelar() {
        nomeUsuarioCriado = null; // Garante que nenhum nome seja retornado
        dispose(); // Fecha o diálogo
    }

    /**
     * Retorna o nome do usuário digitado se o botão "Criar" foi pressionado
     * e o nome era válido. Retorna null caso contrário (cancelado, fechado no 'X',
     * ou nome inválido não submetido).
     *
     * @return O nome do usuário a ser criado, ou null.
     */
    public String getNomeUsuarioCriado() {
        return nomeUsuarioCriado;
    }
}