package com.habitracker.ui;

import com.habitracker.backend.ConquistaService;
import com.habitracker.backend.HabitService;
import com.habitracker.database.HabitDAO;
import com.habitracker.database.ProgressoDiarioDAO;
import com.habitracker.database.UsuarioDAO;
import com.habitracker.model.Conquista;
import com.habitracker.model.Habit;
import com.habitracker.model.Usuario;
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
// import java.time.format.DateTimeFormatter; // Removido pois não é usado diretamente aqui
import java.util.List;

public class MainFrame extends JFrame {

    private HabitTrackerServiceAPI habitService;

    private DefaultListModel<Habit> habitListModel;
    private JList<Habit> habitJList;
    private DefaultListModel<Conquista> conquistaListModel;
    private JList<Conquista> conquistaJList;
    private DefaultListModel<Conquista> todasConquistasListModel;
    private JList<Conquista> todasConquistasJList;

    private JButton editButton;
    private JButton deleteButton;
    private JButton markDoneButton;
    private JButton addButton;
    private JButton refreshButton;

    private JLabel nomeUsuarioLabel;
    private JLabel pontosUsuarioLabel;
    private JProgressBar pontosProgressBar;

    private int usuarioIdAtual = -1;

    // --- CONTROLE DE TEMA ---
    private final boolean USAR_TEMA_ESCURO = true;

    // --- CORES E FONTES PARA O TEMA (MODO CLARO) ---
    private final Color COR_FUNDO_GERAL_CLARO = new Color(235, 235, 235);
    private final Color COR_PAINEL_INFO_CLARO = new Color(220, 220, 220);
    private final Color COR_LARANJA_TEMA_PRIMARIA_CLARO = new Color(255, 140, 0);
    private final Color COR_LARANJA_TEMA_SECUNDARIA_CLARO = new Color(255, 167, 38);
    private final Color COR_TEXTO_PADRAO_CLARO = new Color(40, 40, 40);
    private final Color COR_TEXTO_TITULO_PAINEL_CLARO = COR_LARANJA_TEMA_PRIMARIA_CLARO.darker();

    // --- CORES E FONTES PARA O TEMA ESCURO ---
    private final Color COR_FUNDO_GERAL_ESCURO = new Color(43, 43, 43);
    private final Color COR_PAINEL_ESCURO = new Color(55, 55, 55);
    private final Color COR_TEXTO_ESCURO = new Color(210, 210, 210);
    private final Color COR_TEXTO_TITULO_PAINEL_ESCURO = new Color(255, 152, 0);
    private final Color COR_ACENTO_PRIMARIO_ESCURO = new Color(255, 152, 0); // Laranja para abas selecionadas
    private final Color COR_ACENTO_SECUNDARIO_ESCURO = new Color(200, 100, 0);
    private final Color COR_BORDA_ESCURO = new Color(75, 75, 75);
    private final Color COR_SELECAO_LISTA_FUNDO_ESCURO = new Color(0, 85, 140);
    private final Color COR_SELECAO_LISTA_TEXTO_ESCURO = Color.WHITE;
    private final Color COR_PROGRESSBAR_PROGRESSO_ESCURO = COR_ACENTO_PRIMARIO_ESCURO;
    private final Color COR_PROGRESSBAR_FUNDO_ESCURO = new Color(70, 70, 70);
    private final Color COR_TEXTO_BOTAO_ESCURO = Color.WHITE;

    // Seletores de cor baseados no tema
    private Color getCorFundoGeral() {
        return USAR_TEMA_ESCURO ? COR_FUNDO_GERAL_ESCURO : COR_FUNDO_GERAL_CLARO;
    }

    private Color getCorPainelInfo() {
        return USAR_TEMA_ESCURO ? COR_PAINEL_ESCURO : COR_PAINEL_INFO_CLARO;
    }

    private Color getCorTextoPadrao() {
        return USAR_TEMA_ESCURO ? COR_TEXTO_ESCURO : COR_TEXTO_PADRAO_CLARO;
    }

    private Color getCorTextoTituloPainel() {
        return USAR_TEMA_ESCURO ? COR_TEXTO_TITULO_PAINEL_ESCURO : COR_TEXTO_TITULO_PAINEL_CLARO;
    }

    private Color getCorAcentoPrimaria() {
        return USAR_TEMA_ESCURO ? COR_ACENTO_PRIMARIO_ESCURO : COR_LARANJA_TEMA_PRIMARIA_CLARO;
    }

    private Color getCorAcentoSecundaria() {
        return USAR_TEMA_ESCURO ? COR_ACENTO_SECUNDARIO_ESCURO : COR_LARANJA_TEMA_SECUNDARIA_CLARO;
    }

    private Color getCorTextoBotao() {
        return USAR_TEMA_ESCURO ? COR_TEXTO_BOTAO_ESCURO : Color.WHITE;
    }

    // Fontes
    private final Font FONTE_TITULO_JANELA = new Font("Segoe UI", Font.BOLD, 20);
    private final Font FONTE_TITULO_PAINEL = new Font("Segoe UI Semibold", Font.BOLD, 16);
    private final Font FONTE_LABEL_INFO_USUARIO = new Font("Segoe UI", Font.BOLD, 14);
    private final Font FONTE_TEXTO_GERAL = new Font("Segoe UI", Font.PLAIN, 13);
    private final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 12);
    private final Font FONTE_LISTA = new Font("Segoe UI", Font.PLAIN, 13);


    public MainFrame() {
        if (USAR_TEMA_ESCURO) {
            aplicarConfiguracoesUIManagerTemaEscuro();
        }

        HabitDAO realHabitDAO = new HabitDAO();
        UsuarioDAO realUsuarioDAO = new UsuarioDAO();
        ProgressoDiarioDAO realProgressoDiarioDAO = new ProgressoDiarioDAO();
        ConquistaService realConquistaService = new ConquistaService();
        this.habitService = new HabitService(
                realHabitDAO, realUsuarioDAO, realProgressoDiarioDAO, realConquistaService
        );

        solicitarUsuarioId(); // MÉTODO MODIFICADO ABAIXO

        setTitle("Habit Tracker Gamificado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(getCorFundoGeral());
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();

        if (this.usuarioIdAtual != -1) {
            loadHabits();
            atualizarDisplayUsuario();
            loadTodasConquistasGuia();
        } else {
            // Estado da UI se nenhum usuário for selecionado (após falha em identificar/criar)
            if (nomeUsuarioLabel != null) nomeUsuarioLabel.setText("Usuário: [Nenhum Usuário Logado]");
            if (pontosUsuarioLabel != null) pontosUsuarioLabel.setText("Pontos: -");
            if (pontosProgressBar != null) pontosProgressBar.setValue(0);
            if (habitListModel != null) habitListModel.clear();
            if (conquistaListModel != null) conquistaListModel.clear();
            if (todasConquistasListModel != null) todasConquistasListModel.clear();
            // Desabilitar botões de ação se nenhum usuário estiver logado
            if (addButton != null) addButton.setEnabled(false);
            if (editButton != null) editButton.setEnabled(false);
            if (deleteButton != null) deleteButton.setEnabled(false);
            if (markDoneButton != null) markDoneButton.setEnabled(false);
            if (refreshButton != null) refreshButton.setEnabled(false);

        }
        // atualizarEstadoBotoesAcao() é chamado no final de initComponents e outras operações
    }

    private void aplicarConfiguracoesUIManagerTemaEscuro() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Nimbus LaF não encontrado, usando padrão. A aparência pode variar.");
        }

        UIManager.put("control", COR_PAINEL_ESCURO);
        UIManager.put("Panel.background", getCorFundoGeral());
        UIManager.put("Window.background", getCorFundoGeral());
        UIManager.put("Frame.background", getCorFundoGeral());
        UIManager.put("Dialog.background", getCorFundoGeral());

        UIManager.put("OptionPane.background", COR_PAINEL_ESCURO);
        UIManager.put("OptionPane.messageForeground", getCorTextoPadrao());
        UIManager.put("OptionPane.buttonAreaBorder", BorderFactory.createEmptyBorder());

        UIManager.put("Button.background", getCorAcentoPrimaria());
        UIManager.put("Button.foreground", getCorTextoBotao());
        UIManager.put("Button.select", getCorAcentoSecundaria());
        UIManager.put("Button.focus", new Color(0,0,0,0));
        UIManager.put("Button.border", new EmptyBorder(5, 15, 5, 15));
        UIManager.put("Button.font", FONTE_BOTAO);

        UIManager.put("List.background", COR_PAINEL_ESCURO);
        UIManager.put("List.foreground", getCorTextoPadrao());
        UIManager.put("List.selectionBackground", COR_SELECAO_LISTA_FUNDO_ESCURO);
        UIManager.put("List.selectionForeground", COR_SELECAO_LISTA_TEXTO_ESCURO);
        UIManager.put("List.focusCellHighlightBorder", new EmptyBorder(1,1,1,1));

        UIManager.put("ProgressBar.foreground", COR_PROGRESSBAR_PROGRESSO_ESCURO);
        UIManager.put("ProgressBar.background", COR_PROGRESSBAR_FUNDO_ESCURO);
        UIManager.put("ProgressBar.selectionForeground", getCorTextoPadrao());
        UIManager.put("ProgressBar.selectionBackground", getCorTextoBotao());
        UIManager.put("ProgressBar.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("ProgressBar.border", BorderFactory.createLineBorder(COR_BORDA_ESCURO, 1));

        UIManager.put("TabbedPane.selected", getCorAcentoPrimaria());
        UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
        UIManager.put("TabbedPane.foreground", getCorTextoPadrao());
        UIManager.put("TabbedPane.background", COR_PAINEL_ESCURO);
        UIManager.put("TabbedPane.tabAreaBackground", COR_FUNDO_GERAL_ESCURO);
        UIManager.put("TabbedPane.contentAreaColor", COR_PAINEL_ESCURO);
        UIManager.put("TabbedPane.borderHightlightColor", COR_BORDA_ESCURO);
        UIManager.put("TabbedPane.darkShadow", COR_FUNDO_GERAL_ESCURO);
        UIManager.put("TabbedPane.light", COR_BORDA_ESCURO);
        UIManager.put("TabbedPane.shadow", COR_FUNDO_GERAL_ESCURO);
        UIManager.put("TabbedPane.focus", getCorAcentoPrimaria().darker());
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 10, 4, 10));
        UIManager.put("TabbedPane.font", FONTE_BOTAO.deriveFont(14f));

        UIManager.put("ScrollPane.background", getCorFundoGeral());
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
        UIManager.put("Viewport.background", getCorFundoGeral());

        UIManager.put("ScrollBar.background", COR_PAINEL_ESCURO);
        UIManager.put("ScrollBar.foreground", getCorAcentoPrimaria());
        UIManager.put("ScrollBar.track", COR_BORDA_ESCURO);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.thumb", new Color(100, 100, 100));
        UIManager.put("ScrollBar.thumbDarkShadow", new Color(70, 70, 70));
        UIManager.put("ScrollBar.thumbHighlight", new Color(130, 130, 130));
        UIManager.put("ScrollBar.thumbShadow", new Color(60,60,60));

        UIManager.put("TextField.background", COR_BORDA_ESCURO);
        UIManager.put("TextField.foreground", getCorTextoPadrao());
        UIManager.put("TextField.caretForeground", getCorAcentoPrimaria());
        UIManager.put("TextField.selectionBackground", COR_SELECAO_LISTA_FUNDO_ESCURO);
        UIManager.put("TextField.selectionForeground", COR_SELECAO_LISTA_TEXTO_ESCURO);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_BORDA_ESCURO.brighter()),
            new EmptyBorder(5,5,5,5)
        ));
        UIManager.put("PasswordField.background", COR_BORDA_ESCURO);
        UIManager.put("PasswordField.foreground", getCorTextoPadrao());
        UIManager.put("PasswordField.caretForeground", getCorAcentoPrimaria());
        UIManager.put("PasswordField.selectionBackground", COR_SELECAO_LISTA_FUNDO_ESCURO);
        UIManager.put("PasswordField.selectionForeground", COR_SELECAO_LISTA_TEXTO_ESCURO);
        UIManager.put("PasswordField.border", UIManager.getBorder("TextField.border"));

        UIManager.put("Label.foreground", getCorTextoPadrao());
        UIManager.put("Label.font", FONTE_TEXTO_GERAL);

        UIManager.put("TitledBorder.titleColor", getCorTextoTituloPainel());
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(COR_BORDA_ESCURO, 1));
        UIManager.put("TitledBorder.font", FONTE_TITULO_PAINEL);
    }

    // MÉTODO MODIFICADO PARA INCLUIR CRIAÇÃO DE USUÁRIO
    private void solicitarUsuarioId() {
        boolean idValido = false;
        String idStr = null;

        while (!idValido) {
            idStr = JOptionPane.showInputDialog(this,
                    "Bem-vindo! Por favor, insira seu ID de Usuário (ou deixe em branco para criar um novo):",
                    "Identificação de Usuário",
                    JOptionPane.PLAIN_MESSAGE);

            if (idStr == null) { // Usuário cancelou o diálogo inicial
                this.usuarioIdAtual = -1;
                int sair = JOptionPane.showConfirmDialog(this,
                        "Nenhuma identificação fornecida. Deseja sair da aplicação?",
                        "Sair", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (sair == JOptionPane.YES_OPTION) {
                    System.exit(0); // Fecha a aplicação
                }
                // Se não sair, o loop continua, permitindo nova tentativa ou fechar a janela
                continue;
            }

            if (idStr.trim().isEmpty()) { // Usuário deixou em branco para criar novo
                idValido = tentarCriarNovoUsuario();
            } else { // Usuário inseriu um ID
                try {
                    int idTentativa = Integer.parseInt(idStr.trim());
                    if (idTentativa <= 0) {
                        JOptionPane.showMessageDialog(this, "ID deve ser um número inteiro positivo.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                        continue; // Pede o ID novamente
                    }
                    Usuario usuarioValidado = habitService.getUsuarioById(idTentativa);
                    this.usuarioIdAtual = usuarioValidado.getId();
                    idValido = true;
                    JOptionPane.showMessageDialog(this,
                            "Usuário " + usuarioValidado.getNome() + " (ID: " + this.usuarioIdAtual + ") carregado com sucesso!",
                            "Usuário Identificado", JOptionPane.INFORMATION_MESSAGE);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "ID inválido: '" + idStr + "'. Por favor, insira um número inteiro positivo.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                } catch (UserNotFoundException e) {
                    int choice = JOptionPane.showConfirmDialog(this,
                            "Usuário com ID '" + idStr.trim() + "' não encontrado.\nDeseja criar um novo usuário?",
                            "Usuário Não Encontrado",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        idValido = tentarCriarNovoUsuario();
                    }
                    // Se 'Não', o loop continua e pedirá o ID novamente.
                } catch (PersistenceException e) {
                    JOptionPane.showMessageDialog(this, "Erro de persistência ao buscar usuário: " + e.getMessage() + "\nVerifique a conexão com o banco de dados.", "Erro de Backend", JOptionPane.ERROR_MESSAGE);
                    // Aqui você pode decidir se quer que o loop continue ou oferecer para sair
                } catch (Exception e) { // Outras exceções inesperadas
                    JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao validar usuário: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        } // fim do while (!idValido)

        if (!idValido) { // Se, por algum motivo, sair do loop sem um usuário válido (ex: cancelou tudo)
            this.usuarioIdAtual = -1;
            // A mensagem de funcionalidades limitadas já é tratada no construtor
        }
    }

    // NOVO MÉTODO AUXILIAR PARA CRIAR USUÁRIO
    private boolean tentarCriarNovoUsuario() {
        CreateUserDialog createUserDialog = new CreateUserDialog(this, true, USAR_TEMA_ESCURO);
        createUserDialog.setVisible(true);
        String nomeNovoUsuario = createUserDialog.getNomeUsuarioCriado();

        if (nomeNovoUsuario != null) { // Usuário clicou em "Criar" e forneceu um nome
            try {
                Usuario novoUsuarioCriado = habitService.addUsuario(nomeNovoUsuario);
                this.usuarioIdAtual = novoUsuarioCriado.getId();
                JOptionPane.showMessageDialog(this,
                        "Usuário '" + novoUsuarioCriado.getNome() + "' (ID: " + this.usuarioIdAtual + ") criado e carregado com sucesso!",
                        "Usuário Criado", JOptionPane.INFORMATION_MESSAGE);
                return true; // Usuário criado e logado com sucesso
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, "Erro ao criar usuário: " + ve.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            } catch (PersistenceException pe) {
                JOptionPane.showMessageDialog(this, "Erro de persistência ao criar usuário: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) { // Outras exceções inesperadas
                JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao criar usuário: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
        return false; // Criação falhou ou foi cancelada pelo usuário
    }


    private void initComponents() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        null, "Informações do Usuário", 0, 0, FONTE_TITULO_PAINEL, getCorTextoTituloPainel()),
                new EmptyBorder(10, 10, 10, 10)
        ));
        infoPanel.setBackground(getCorPainelInfo());
        infoPanel.setOpaque(true);

        nomeUsuarioLabel = new JLabel("Usuário: Carregando...");
        nomeUsuarioLabel.setFont(FONTE_LABEL_INFO_USUARIO);
        nomeUsuarioLabel.setForeground(getCorTextoPadrao());
        pontosUsuarioLabel = new JLabel("Pontos: Carregando...");
        pontosUsuarioLabel.setFont(FONTE_LABEL_INFO_USUARIO);
        pontosUsuarioLabel.setForeground(getCorTextoPadrao());

        pontosProgressBar = new JProgressBar(0, 100);
        pontosProgressBar.setStringPainted(true);
        pontosProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pontosProgressBar.setPreferredSize(new Dimension(220, 28));

        infoPanel.add(nomeUsuarioLabel);
        infoPanel.add(Box.createHorizontalStrut(30));
        infoPanel.add(pontosUsuarioLabel);
        infoPanel.add(Box.createHorizontalStrut(15));
        infoPanel.add(pontosProgressBar);
        infoPanel.add(Box.createHorizontalGlue());
        add(infoPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel painelMeuProgresso = new JPanel(new GridLayout(1, 2, 20, 0));
        painelMeuProgresso.setOpaque(false);

        JPanel habitPanel = new JPanel(new BorderLayout(5, 10));
        JLabel tituloHabitos = new JLabel("Meus Hábitos", SwingConstants.CENTER);
        tituloHabitos.setFont(FONTE_TITULO_PAINEL);
        tituloHabitos.setForeground(getCorTextoTituloPainel());
        tituloHabitos.setBorder(new EmptyBorder(10, 0, 15, 0));
        habitPanel.add(tituloHabitos, BorderLayout.NORTH);
        habitListModel = new DefaultListModel<>();
        habitJList = new JList<>(habitListModel);
        habitJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitJList.setCellRenderer(new HabitListCellRenderer(USAR_TEMA_ESCURO));
        habitJList.setFont(FONTE_LISTA);
        habitJList.addListSelectionListener(e -> atualizarEstadoBotoesAcao());
        JScrollPane habitScrollPane = new JScrollPane(habitJList);
        if (USAR_TEMA_ESCURO) {
            habitScrollPane.setOpaque(false);
            habitScrollPane.getViewport().setOpaque(false);
            habitJList.setOpaque(false);
        }
        habitPanel.add(habitScrollPane, BorderLayout.CENTER);
        habitPanel.setOpaque(false);
        painelMeuProgresso.add(habitPanel);

        JPanel conquistaPanel = new JPanel(new BorderLayout(5, 10));
        JLabel tituloMinhasConquistas = new JLabel("Minhas Conquistas", SwingConstants.CENTER);
        tituloMinhasConquistas.setFont(FONTE_TITULO_PAINEL);
        tituloMinhasConquistas.setForeground(getCorTextoTituloPainel());
        tituloMinhasConquistas.setBorder(new EmptyBorder(10, 0, 15, 0));
        conquistaPanel.add(tituloMinhasConquistas, BorderLayout.NORTH);
        conquistaListModel = new DefaultListModel<>();
        conquistaJList = new JList<>(conquistaListModel);
        conquistaJList.setCellRenderer(new ConquistaListCellRenderer(USAR_TEMA_ESCURO));
        conquistaJList.setFont(FONTE_LISTA);
        JScrollPane conquistaScrollPane = new JScrollPane(conquistaJList);
        if (USAR_TEMA_ESCURO) {
            conquistaScrollPane.setOpaque(false);
            conquistaScrollPane.getViewport().setOpaque(false);
            conquistaJList.setOpaque(false);
        }
        conquistaPanel.add(conquistaScrollPane, BorderLayout.CENTER);
        conquistaPanel.setOpaque(false);
        painelMeuProgresso.add(conquistaPanel);
        tabbedPane.addTab(" Meu Progresso ", painelMeuProgresso);

        JPanel guiaConquistasPanel = new JPanel(new BorderLayout(5,10));
        guiaConquistasPanel.setOpaque(false);
        JLabel tituloGuiaConquistas = new JLabel("Guia de Todas as Conquistas", SwingConstants.CENTER);
        tituloGuiaConquistas.setFont(FONTE_TITULO_PAINEL);
        tituloGuiaConquistas.setForeground(getCorTextoTituloPainel());
        tituloGuiaConquistas.setBorder(new EmptyBorder(10,0,15,0));
        guiaConquistasPanel.add(tituloGuiaConquistas, BorderLayout.NORTH);
        todasConquistasListModel = new DefaultListModel<>();
        todasConquistasJList = new JList<>(todasConquistasListModel);
        todasConquistasJList.setCellRenderer(new ConquistaListCellRenderer(USAR_TEMA_ESCURO));
        todasConquistasJList.setFont(FONTE_LISTA);
        JScrollPane todasConquistasScrollPane = new JScrollPane(todasConquistasJList);
        if (USAR_TEMA_ESCURO) {
            todasConquistasScrollPane.setOpaque(false);
            todasConquistasScrollPane.getViewport().setOpaque(false);
            todasConquistasJList.setOpaque(false);
        }
        guiaConquistasPanel.add(todasConquistasScrollPane, BorderLayout.CENTER);
        tabbedPane.addTab(" Guia de Conquistas ", guiaConquistasPanel);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        buttonPanel.setOpaque(false);
        refreshButton = createStyledButton("Recarregar Tudo");
        refreshButton.addActionListener(e -> {
            if (usuarioIdAtual != -1) {
                loadHabits();
                atualizarDisplayUsuario();
                loadTodasConquistasGuia();
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum usuário selecionado para recarregar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        buttonPanel.add(refreshButton);

        addButton = createStyledButton("Adicionar Hábito");
        addButton.addActionListener(e -> abrirDialogoAdicionarHabito());
        buttonPanel.add(addButton);

        editButton = createStyledButton("Editar Hábito");
        editButton.addActionListener(e -> abrirDialogoEditarHabito());
        buttonPanel.add(editButton);

        deleteButton = createStyledButton("Excluir Hábito");
        deleteButton.addActionListener(e -> excluirHabitoSelecionado());
        buttonPanel.add(deleteButton);

        markDoneButton = createStyledButton("Marcar Como Feito");
        markDoneButton.addActionListener(e -> marcarHabitoSelecionadoComoFeito());
        buttonPanel.add(markDoneButton);
        add(buttonPanel, BorderLayout.SOUTH);
        atualizarEstadoBotoesAcao(); // Chamado aqui para estado inicial correto
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONTE_BOTAO);

        if (USAR_TEMA_ESCURO) {
            button.setBackground(getCorAcentoPrimaria());
            button.setForeground(getCorTextoBotao());
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_ESCURO, 1),
                new EmptyBorder(8, 20, 8, 20)
            ));
            Color originalBg = getCorAcentoPrimaria();
            Color hoverBg = getCorAcentoSecundaria();

            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) button.setBackground(hoverBg);
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) button.setBackground(originalBg);
                }
                public void mousePressed(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) button.setBackground(hoverBg.darker());
                }
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) {
                         if (button.getBounds().contains(evt.getPoint())) {
                            button.setBackground(hoverBg);
                        } else {
                            button.setBackground(originalBg);
                        }
                    }
                }
            });
        } else {
            button.setBackground(COR_LARANJA_TEMA_SECUNDARIA_CLARO);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 20, 8, 20));
        }
        button.setFocusPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void atualizarEstadoBotoesAcao() {
        boolean isUserLoaded = (this.usuarioIdAtual != -1);
        boolean isHabitSelectedInList = isUserLoaded && (habitJList != null && habitJList.getSelectedIndex() != -1);

        if (refreshButton != null) refreshButton.setEnabled(isUserLoaded);
        if (addButton != null) addButton.setEnabled(isUserLoaded);
        if (editButton != null) editButton.setEnabled(isHabitSelectedInList);
        if (deleteButton != null) deleteButton.setEnabled(isHabitSelectedInList);
        if (markDoneButton != null) markDoneButton.setEnabled(isHabitSelectedInList);
    }

    // Dentro de MainFrame.java

private void loadHabits() {
    if (usuarioIdAtual == -1) {
        if (habitListModel != null) habitListModel.clear();
        return;
    }
    try {
        habitListModel.clear();
        // ANTES: List<Habit> habits = habitService.getAllHabits();
        // AGORA: Chamar o método específico do usuário
        List<Habit> habits = habitService.getHabitsByUserId(this.usuarioIdAtual);

        if (habits != null) {
            for (Habit habit : habits) {
                habitListModel.addElement(habit);
            }
        }
    } catch (UserNotFoundException unfe) { // Captura se o usuário não for encontrado pelo serviço
        JOptionPane.showMessageDialog(this,
                "Usuário não encontrado ao carregar hábitos: " + unfe.getMessage(),
                "Erro de Usuário", JOptionPane.ERROR_MESSAGE);
        if (habitListModel != null) habitListModel.clear(); // Limpa a lista em caso de erro
    } catch (PersistenceException pe) { // Captura erros de persistência
        JOptionPane.showMessageDialog(this,
                "Erro de persistência ao carregar hábitos: " + pe.getMessage(),
                "Erro de Backend", JOptionPane.ERROR_MESSAGE);
        if (habitListModel != null) habitListModel.clear();
    } catch (Exception e) { // Captura outras exceções inesperadas
        JOptionPane.showMessageDialog(this,
                "Erro inesperado ao carregar hábitos: " + e.getMessage(),
                "Erro", JOptionPane.ERROR_MESSAGE);
        if (habitListModel != null) habitListModel.clear();
        e.printStackTrace();
    }
    atualizarEstadoBotoesAcao();
}

    private void atualizarDisplayUsuario() {
        if (usuarioIdAtual == -1) {
            if (nomeUsuarioLabel != null) nomeUsuarioLabel.setText("Usuário: [Nenhum Selecionado]");
            if (pontosUsuarioLabel != null) pontosUsuarioLabel.setText("Pontos: -");
            if (pontosProgressBar != null) {
                pontosProgressBar.setValue(0);
                pontosProgressBar.setString("0 / 100 pts");
                pontosProgressBar.setMaximum(100);
            }
            if (conquistaListModel != null) conquistaListModel.clear();
            return;
        }
        try {
            Usuario currentUser = habitService.getUsuarioById(this.usuarioIdAtual);
            nomeUsuarioLabel.setText("Usuário: " + currentUser.getNome() + " (ID: " + currentUser.getId() + ")");
            pontosUsuarioLabel.setText("Pontos: " + currentUser.getPontos());

            int pontos = currentUser.getPontos();
            int metaProximoNivel = ((pontos / 100) + 1) * 100;
            if (pontos == 0) metaProximoNivel = 100;
            if (metaProximoNivel == pontos && pontos > 0) metaProximoNivel +=100;

            pontosProgressBar.setMaximum(metaProximoNivel);
            pontosProgressBar.setValue(pontos);
            pontosProgressBar.setString(pontos + " / " + metaProximoNivel + " pts");

            if (conquistaListModel != null) conquistaListModel.clear();
            List<Conquista> conquistas = habitService.getConquistasDesbloqueadasUsuario(this.usuarioIdAtual);
            if (conquistas != null) {
                for (Conquista conquista : conquistas) {
                    conquistaListModel.addElement(conquista);
                }
            }
        } catch (UserNotFoundException | PersistenceException e) {
            nomeUsuarioLabel.setText("Usuário ID: " + this.usuarioIdAtual + " (Erro ao carregar)");
            pontosUsuarioLabel.setText("Pontos: -");
            if (pontosProgressBar != null) pontosProgressBar.setValue(0);
            if (conquistaListModel != null) conquistaListModel.clear();
            JOptionPane.showMessageDialog(this, "Dados do usuário (ID: " + this.usuarioIdAtual + ") não puderam ser carregados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do usuário: " + e.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        atualizarEstadoBotoesAcao(); // Garante que os botões reflitam o estado do usuário
    }

    private void loadTodasConquistasGuia() {
        if (this.usuarioIdAtual == -1) {
            if (todasConquistasListModel != null) todasConquistasListModel.clear();
            return;
        }
        try {
            if (todasConquistasListModel != null) todasConquistasListModel.clear();
            List<Conquista> todasDefinicoes = habitService.getAllConquistasPossiveis();
            if (todasDefinicoes != null) {
                for (Conquista definicao : todasDefinicoes) {
                    todasConquistasListModel.addElement(definicao);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar guia de conquistas: " + e.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirDialogoAdicionarHabito() {
        if (usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, identifique um usuário primeiro.", "Usuário Necessário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Certifique-se que AddHabitDialog existe e está no pacote com.habitracker.ui
        // e que seu construtor corresponde ao chamado aqui.
        // Adapte o AddHabitDialog para o tema escuro se necessário.
        AddHabitDialog addDialog = new AddHabitDialog(this, true, this.habitService, this.usuarioIdAtual, USAR_TEMA_ESCURO);
        addDialog.setVisible(true);
        if (addDialog.isHabitoAdicionadoComSucesso()) {
            loadHabits();
            atualizarDisplayUsuario();
            JOptionPane.showMessageDialog(this, "Novo hábito '" + addDialog.getNovoHabito().getName() + "' adicionado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void abrirDialogoEditarHabito() {
        if (usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, identifique um usuário primeiro.", "Usuário Necessário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito para editar.", "Nenhum Hábito", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Certifique-se que EditHabitDialog existe e está no pacote com.habitracker.ui
        // e que seu construtor corresponde ao chamado aqui.
        // Adapte o EditHabitDialog para o tema escuro se necessário.
        EditHabitDialog editDialog = new EditHabitDialog(this, true, this.habitService, selectedHabit, USAR_TEMA_ESCURO);
        editDialog.setVisible(true);
        if (editDialog.isHabitoAtualizadoComSucesso()) {
            loadHabits(); // Recarrega para refletir a mudança
            // atualizarDisplayUsuario(); // Opcional, se a edição puder afetar pontos/conquistas
            JOptionPane.showMessageDialog(this, "Hábito '" + editDialog.getHabitoAtualizado().getName() + "' atualizado!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void excluirHabitoSelecionado() {
        if (usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, identifique um usuário primeiro.", "Usuário Necessário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito para excluir.", "Nenhum Hábito", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir o hábito '" + selectedHabit.getName() + "'?",
                "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean sucesso = habitService.deleteHabit(selectedHabit.getId());
                if (sucesso) {
                    loadHabits();
                    atualizarDisplayUsuario();
                    JOptionPane.showMessageDialog(this, "Hábito excluído!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Não foi possível excluir o hábito (o serviço indicou falha).", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HabitNotFoundException hnfe){
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + hnfe.getMessage(), "Hábito Não Encontrado", JOptionPane.ERROR_MESSAGE);
                loadHabits(); // Recarrega a lista, pois o hábito pode ter sido removido por outro meio
                atualizarDisplayUsuario();
            } catch (PersistenceException pe) {
                JOptionPane.showMessageDialog(this, "Erro de persistência ao excluir o hábito: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao excluir o hábito: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void marcarHabitoSelecionadoComoFeito() {
        if (usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, identifique um usuário primeiro.", "Usuário Necessário", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito.", "Nenhum Hábito", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate hoje = LocalDate.now();
        try {
            FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(this.usuarioIdAtual, selectedHabit.getId(), hoje);

            StringBuilder feedbackMessage = new StringBuilder();
            if (feedback.isSucesso()) {
                feedbackMessage.append(feedback.getMensagem()).append("\n");
                if (feedback.getPontosGanhosNestaMarcacao() > 0) {
                    feedbackMessage.append("Você ganhou: ").append(feedback.getPontosGanhosNestaMarcacao()).append(" pontos!\n");
                }
                feedbackMessage.append("Seu total de pontos agora é: ").append(feedback.getTotalPontosUsuarioAposMarcacao()).append(".\n");
                if (feedback.getNovasConquistasDesbloqueadas() != null && !feedback.getNovasConquistasDesbloqueadas().isEmpty()) {
                    feedbackMessage.append("\nNovas Conquistas Desbloqueadas:\n");
                    for (Conquista c : feedback.getNovasConquistasDesbloqueadas()) {
                        feedbackMessage.append("- ").append(c.getNome()).append(" (Bônus: ").append(c.getPontosBonus()).append(" pts)\n");
                    }
                }
                JOptionPane.showMessageDialog(this, feedbackMessage.toString(), "Hábito Marcado!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, feedback.getMensagem(), "Atenção", JOptionPane.WARNING_MESSAGE);
            }
            atualizarDisplayUsuario(); // Atualiza pontos e conquistas
        } catch (UserNotFoundException unfe) {
            JOptionPane.showMessageDialog(this, "Erro: Usuário (ID: " + this.usuarioIdAtual + ") não encontrado para esta ação.\n" + unfe.getMessage(), "Erro de Usuário", JOptionPane.ERROR_MESSAGE);
        } catch (HabitNotFoundException hnfe) {
            JOptionPane.showMessageDialog(this, "Erro ao marcar hábito: " + hnfe.getMessage(), "Hábito Não Encontrado", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException ve) {
            JOptionPane.showMessageDialog(this, "Erro de validação ao marcar hábito: " + ve.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        } catch (PersistenceException pe) {
            JOptionPane.showMessageDialog(this, "Erro de persistência ao marcar o hábito: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao marcar o hábito: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Para um tema escuro mais robusto e consistente, considere usar FlatLaf:
        // try {
        // UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        // } catch (Exception ex) {
        // System.err.println("Failed to initialize FlatLaf Dark LaF");
        // }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}