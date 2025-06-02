package com.habitracker.ui;


import com.habitracker.backend.HabitService;


import com.habitracker.database.HabitDAO;       
import com.habitracker.database.ProgressoDiarioDAO; 
import com.habitracker.database.UsuarioDAO;
import com.habitracker.database.ObjetivoDAO;

import com.habitracker.model.Habit;
import com.habitracker.model.Usuario;
import com.habitracker.model.ProgressoDiario;
import com.habitracker.model.Objetivo; 
import com.habitracker.serviceapi.HabitTrackerServiceAPI;
import com.habitracker.serviceapi.dto.FeedbackMarcacaoDTO;
import com.habitracker.serviceapi.exceptions.HabitNotFoundException;
import com.habitracker.serviceapi.exceptions.PersistenceException;
import com.habitracker.serviceapi.exceptions.UserNotFoundException;
import com.habitracker.serviceapi.exceptions.ValidationException;
import com.habitracker.backend.ObjetivoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;


import java.beans.PropertyChangeListener;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar; 
import java.util.Collections;
import java.util.Comparator; 
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDayChooser;

public class MainFrame extends JFrame {

    private HabitTrackerServiceAPI habitService;
    private ObjetivoService objetivoService;
    private ProgressoDiarioDAO progressoDiarioDAO_ref; 

    private LocalDate dataSimuladaHoje = null;
    private LocalDate dataContextoHabitos; 

    private DefaultListModel<Habit> habitListModel;
    private JList<Habit> habitJList;
    private TitledBorder habitPanelTitledBorder; 
    private JPanel habitPanel; 

    private DefaultListModel<Objetivo> objetivosListModel;
    private JList<Objetivo> objetivosJList;
    private JPanel painelObjetivos;
    private JButton btnAddObjetivo, btnEditObjetivo, btnDelObjetivo, btnMarcarObjetivoConcluido;

    private JCalendar calendarioView;
    private JPanel calendarioPanel;

    private JButton editButton, deleteButton, markDoneButton, addButton, refreshButton;
    private JLabel nomeUsuarioLabel, pontosUsuarioLabel;
    private JProgressBar pontosProgressBar;
    private int usuarioIdAtual = -1;
    private static int proximoIdSimuladoObjetivo = -1; 

    private final boolean USAR_TEMA_ESCURO = true;

    
    private final Color COR_FUNDO_GERAL_CLARO = new Color(240, 240, 240);
    private final Color COR_PAINEL_CLARO = new Color(225, 225, 225);
    private final Color COR_TEXTO_PADRAO_CLARO = new Color(30, 30, 30);
    private final Color COR_TEXTO_TITULO_CLARO = new Color(0, 80, 150);
    private final Color COR_ACENTO_PRIMARIO_CLARO = new Color(255, 120, 30);
    private final Color COR_ACENTO_SECUNDARIO_CLARO = new Color(255, 150, 70);
    private final Color COR_SELECAO_LISTA_FUNDO_VERDE_CLARO = new Color(190, 240, 190);
    private final Color COR_SELECAO_LISTA_TEXTO_CLARO = Color.BLACK;

    private final Color COR_FUNDO_GERAL_ESCURO = new Color(30, 30, 30);
    private final Color COR_PAINEL_ESCURO = new Color(45, 45, 45);
    private final Color COR_TEXTO_ESCURO = new Color(220, 220, 220);
    private final Color COR_TEXTO_TITULO_ESCURO = new Color(255, 165, 0); 
    private final Color COR_ACENTO_PRIMARIO_ESCURO = new Color(255, 165, 0);
    private final Color COR_ACENTO_SECUNDARIO_ESCURO = new Color(220, 120, 0);
    private final Color COR_BORDA_ESCURO = new Color(60, 60, 60);
    private final Color COR_SELECAO_LISTA_FUNDO_VERDE_ESCURO = new Color(25, 80, 35);
    private final Color COR_SELECAO_LISTA_TEXTO_ESCURO = Color.WHITE;
    private final Color COR_PROGRESSBAR_PROGRESSO_ESCURO = COR_ACENTO_PRIMARIO_ESCURO;
    private final Color COR_PROGRESSBAR_FUNDO_ESCURO = new Color(60, 60, 60);
    private final Color COR_TEXTO_BOTAO_ESCURO = Color.WHITE;

    private final Font FONTE_TITULO_JANELA = new Font("Arial", Font.BOLD, 24);
    private final Font FONTE_TITULO_PAINEL_NOVA = new Font("Arial Black", Font.BOLD, 20);
    private final Font FONTE_LABEL_INFO_USUARIO = new Font("Segoe UI Semibold", Font.BOLD, 17);
    private final Font FONTE_TEXTO_GERAL = new Font("Segoe UI", Font.PLAIN, 16);
    private final Font FONTE_BOTAO = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    private final Font FONTE_LISTA = new Font("Segoe UI", Font.PLAIN, 16);
    

    private List<ProgressoDiario> progressoDoMesAtual;
    private List<Habit> habitosDoUsuarioAtual; 
    private Map<LocalDate, String> statusDiasCalendario;

    private Color getCorFundoGeral() { return USAR_TEMA_ESCURO ? COR_FUNDO_GERAL_ESCURO : COR_FUNDO_GERAL_CLARO; }
    private Color getCorPainelInterno() { return USAR_TEMA_ESCURO ? COR_PAINEL_ESCURO : COR_PAINEL_CLARO; }
    private Color getCorTextoPadrao() { return USAR_TEMA_ESCURO ? COR_TEXTO_ESCURO : COR_TEXTO_PADRAO_CLARO; }
    private Color getCorTextoTituloPainel() { return USAR_TEMA_ESCURO ? COR_TEXTO_TITULO_ESCURO : COR_TEXTO_TITULO_CLARO; }
    private Color getCorAcentoPrimaria() { return USAR_TEMA_ESCURO ? COR_ACENTO_PRIMARIO_ESCURO : COR_ACENTO_PRIMARIO_CLARO; }
    private Color getCorAcentoSecundaria() { return USAR_TEMA_ESCURO ? COR_ACENTO_SECUNDARIO_ESCURO : COR_ACENTO_SECUNDARIO_CLARO; }
    private Color getCorTextoBotao() { return USAR_TEMA_ESCURO ? COR_TEXTO_BOTAO_ESCURO : Color.WHITE; }


    public MainFrame() {
        this.progressoDoMesAtual = new ArrayList<>();
        this.habitosDoUsuarioAtual = new ArrayList<>();
        this.statusDiasCalendario = new HashMap<>();
        this.dataContextoHabitos = getDataAtualParaLogica(); 

        if (USAR_TEMA_ESCURO) {
            aplicarConfiguracoesUIManagerTemaEscuro();
        } else {
            aplicarConfiguracoesUIManagerTemaClaro();
        }

        HabitDAO realHabitDAO = new HabitDAO();
        UsuarioDAO realUsuarioDAO = new UsuarioDAO();
        this.progressoDiarioDAO_ref = new ProgressoDiarioDAO();
        ObjetivoDAO realObjetivoDAO = new ObjetivoDAO();
        this.habitService = new HabitService(realHabitDAO, realUsuarioDAO, this.progressoDiarioDAO_ref);
        this.objetivoService = new ObjetivoService(realObjetivoDAO, realHabitDAO);
        solicitarUsuarioId();

        setTitle("Habit Tracker Gamificado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 820); 
        setMinimumSize(new Dimension(1200, 750)); 
        setLocationRelativeTo(null);
        getContentPane().setBackground(getCorFundoGeral());
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents(); 

        if (this.usuarioIdAtual != -1) {
            loadHabits(); 
            atualizarDisplayUsuario();
            carregarDadosCalendario();
            loadObjetivos(); 
        } else {
            if (nomeUsuarioLabel != null) nomeUsuarioLabel.setText("Usuário: [Nenhum Usuário Logado]");
            if (pontosUsuarioLabel != null) pontosUsuarioLabel.setText("Pontos: -");
            if (pontosProgressBar != null) {
                pontosProgressBar.setValue(0);
                pontosProgressBar.setString("0 / 100 pts");
                pontosProgressBar.setMaximum(100);
            }
            if (habitListModel != null) habitListModel.clear();
            if (objetivosListModel != null) objetivosListModel.clear();
            limparCoresCalendario();
            if (habitPanelTitledBorder != null && habitPanel != null) {
                habitPanelTitledBorder.setTitle("Hábitos");
                habitPanel.repaint();
            }
        }
        atualizarEstadoBotoesAcao(); 
    }

    private void aplicarConfiguracoesUIManagerTemaClaro() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Falha ao definir LookAndFeel padrão do sistema: " + e.getMessage());
        }
        UIManager.put("List.background", Color.WHITE);
        UIManager.put("List.foreground", COR_TEXTO_PADRAO_CLARO);
        UIManager.put("List.selectionBackground", COR_SELECAO_LISTA_FUNDO_VERDE_CLARO); 
        UIManager.put("List.selectionForeground", COR_SELECAO_LISTA_TEXTO_CLARO); 
        UIManager.put("TitledBorder.font", FONTE_TITULO_PAINEL_NOVA);
        UIManager.put("TitledBorder.titleColor", getCorTextoTituloPainel());
    }

    private void aplicarConfiguracoesUIManagerTemaEscuro() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) { System.err.println("Nimbus LaF não encontrado."); }

        UIManager.put("control", COR_PAINEL_ESCURO);
        UIManager.put("info", COR_PAINEL_ESCURO); 
        UIManager.put("nimbusBase", COR_ACENTO_SECUNDARIO_ESCURO.darker());
        UIManager.put("nimbusFocus", COR_ACENTO_PRIMARIO_ESCURO);
        UIManager.put("nimbusLightBackground", COR_PAINEL_ESCURO);
        UIManager.put("nimbusSelectionBackground", COR_ACENTO_PRIMARIO_ESCURO); 
        UIManager.put("text", COR_TEXTO_ESCURO); 
        UIManager.put("Panel.background", getCorFundoGeral());
        UIManager.put("OptionPane.background", COR_PAINEL_ESCURO);
        UIManager.put("OptionPane.messageForeground", COR_TEXTO_ESCURO);
        UIManager.put("Button.background", COR_ACENTO_PRIMARIO_ESCURO); 
        UIManager.put("Button.foreground", COR_TEXTO_BOTAO_ESCURO);
        UIManager.put("Button.font", FONTE_BOTAO);

        UIManager.put("List.background", COR_PAINEL_ESCURO);
        UIManager.put("List.foreground", getCorTextoPadrao());
        UIManager.put("List.selectionBackground", COR_SELECAO_LISTA_FUNDO_VERDE_ESCURO); 
        UIManager.put("List.selectionForeground", COR_SELECAO_LISTA_TEXTO_ESCURO);    
        UIManager.put("List.focusCellHighlightBorder", new EmptyBorder(1,1,1,1));

        UIManager.put("TitledBorder.font", FONTE_TITULO_PAINEL_NOVA);
        UIManager.put("TitledBorder.titleColor", getCorTextoTituloPainel());
        UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(COR_BORDA_ESCURO, 1));

        UIManager.put("Label.font", FONTE_TEXTO_GERAL);
        UIManager.put("Label.foreground", COR_TEXTO_ESCURO);
        UIManager.put("ProgressBar.foreground", COR_PROGRESSBAR_PROGRESSO_ESCURO);
        UIManager.put("ProgressBar.background", COR_PROGRESSBAR_FUNDO_ESCURO);
        UIManager.put("ProgressBar.selectionForeground", COR_TEXTO_ESCURO); 
        UIManager.put("ProgressBar.selectionBackground", COR_PROGRESSBAR_PROGRESSO_ESCURO);
        UIManager.put("ToolTip.background", COR_PAINEL_ESCURO.brighter());
        UIManager.put("ToolTip.foreground", COR_TEXTO_ESCURO);
        UIManager.put("ComboBox.background", COR_PAINEL_ESCURO);
        UIManager.put("ComboBox.foreground", COR_TEXTO_ESCURO);
        UIManager.put("ComboBox.selectionBackground", COR_ACENTO_PRIMARIO_ESCURO);
        UIManager.put("ComboBox.selectionForeground", COR_TEXTO_BOTAO_ESCURO);
        UIManager.put("ScrollPane.background", getCorPainelInterno());
        UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(COR_BORDA_ESCURO));
        UIManager.put("Viewport.background", getCorPainelInterno()); 

        UIManager.put("TabbedPane.font", FONTE_BOTAO); 
        UIManager.put("TabbedPane.foreground", COR_TEXTO_ESCURO);
        UIManager.put("TabbedPane.selected", COR_ACENTO_PRIMARIO_ESCURO);
        UIManager.put("TabbedPane.background", COR_PAINEL_ESCURO);
        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.tabsOverlapBorder", true);

        UIManager.put("Calendar.background", getCorPainelInterno());
        UIManager.put("Calendar.foreground", getCorTextoPadrao());
        UIManager.put("Calendar.weekOfYearForeground", getCorTextoPadrao().darker()); 
        UIManager.put("Calendar.sundayForeground", COR_ACENTO_PRIMARIO_ESCURO); 
        UIManager.put("Calendar.weekdayForeground", COR_ACENTO_PRIMARIO_ESCURO); 
        UIManager.put("Calendar.todayColor", getCorAcentoSecundaria()); 

        UIManager.put("JMonthChooser.background", getCorPainelInterno());
        UIManager.put("JMonthChooser.foreground", getCorTextoPadrao());
        UIManager.put("JYearChooser.background", getCorPainelInterno()); 
        UIManager.put("JYearChooser.foreground", getCorTextoPadrao()); 
        UIManager.put("Spinner.background", getCorPainelInterno());
        UIManager.put("Spinner.foreground", getCorTextoPadrao());
        UIManager.put("Spinner.arrowButtonBackground", getCorAcentoPrimaria());
        UIManager.put("Spinner.border", BorderFactory.createLineBorder(COR_BORDA_ESCURO));
    }

    private LocalDate getDataAtualParaLogica() {
        return dataSimuladaHoje != null ? dataSimuladaHoje : LocalDate.now();
    }

    public void setDataSimuladaParaTeste(LocalDate dataSimulada) {
        this.dataSimuladaHoje = dataSimulada;
        
        setDataContextoHabitos(this.dataSimuladaHoje); 
        System.out.println("Data atual para lógica do app simulada para: " + this.dataSimuladaHoje);
        System.out.println("Contexto de hábitos atualizado para (via simulação): " + this.dataContextoHabitos);
    }
    
    private void solicitarUsuarioId() {
        boolean idValido = false;
        String idStr;
        while (!idValido) {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            JPanel labelPanel = new JPanel(new GridLayout(0, 1, 2, 2));
            labelPanel.add(new JLabel("Bem-vindo! Por favor, insira seu ID de Usuário"));
            labelPanel.add(new JLabel("(ou deixe em branco para criar um novo):"));
            panel.add(labelPanel, BorderLayout.NORTH);
            JTextField textField = new JTextField(10);
            panel.add(textField, BorderLayout.CENTER);

            if (USAR_TEMA_ESCURO) {
                panel.setBackground(COR_PAINEL_ESCURO);
                labelPanel.setBackground(COR_PAINEL_ESCURO);
                for(Component c : labelPanel.getComponents()){
                    if(c instanceof JLabel) c.setForeground(COR_TEXTO_ESCURO);
                }
                textField.setBackground(COR_PAINEL_ESCURO.brighter());
                textField.setForeground(COR_TEXTO_ESCURO);
                textField.setCaretColor(COR_TEXTO_ESCURO);
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(COR_BORDA_ESCURO),
                    new EmptyBorder(3,5,3,5)
                ));
            }
            
            int option = JOptionPane.showConfirmDialog(this, panel, "Identificação de Usuário", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                 idStr = textField.getText();
            } else { 
                idStr = null; 
            }

            if (idStr == null) {
                int choice = JOptionPane.showConfirmDialog(this,
                        "Nenhum ID de usuário foi fornecido.\nDeseja sair do aplicativo?",
                        "Login Cancelado",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    System.exit(0);
                    return; 
                }
                continue;
            }
            if (idStr.trim().isEmpty()) {
                idValido = tentarCriarNovoUsuario();
            } else {
                try {
                    int id = Integer.parseInt(idStr.trim());
                    if (id <= 0) {
                        JOptionPane.showMessageDialog(this, "ID de usuário inválido. Deve ser um número positivo.", "Erro de ID", JOptionPane.ERROR_MESSAGE);
                        continue;
                    }
                    Usuario usuarioExistente = habitService.getUsuarioById(id);
                    this.usuarioIdAtual = usuarioExistente.getId();
                    JOptionPane.showMessageDialog(this, "Usuário '" + usuarioExistente.getNome() + "' (ID: " + this.usuarioIdAtual + ") carregado com sucesso!", "Usuário Carregado", JOptionPane.INFORMATION_MESSAGE);
                    idValido = true;
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "ID de usuário inválido: '" + idStr.trim() + "'. Por favor, insira um número.", "Erro de Formato de ID", JOptionPane.ERROR_MESSAGE);
                } catch (UserNotFoundException e) {
                    int choice = JOptionPane.showConfirmDialog(this, "Usuário com ID '" + idStr.trim() + "' não encontrado.\nDeseja criar um novo usuário?", "Usuário Não Encontrado", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (choice == JOptionPane.YES_OPTION) {
                        idValido = tentarCriarNovoUsuario();
                    }
                } catch (PersistenceException ex) {
                    JOptionPane.showMessageDialog(this, "Erro de persistência ao buscar usuário: " + ex.getMessage() + "\nVerifique a conexão com o banco de dados e tente novamente.", "Erro de Backend", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                } catch (Exception exGeral) {
                    JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao tentar carregar o usuário: " + exGeral.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
                    exGeral.printStackTrace();
                }
            }
        }
        if (!idValido) {
            this.usuarioIdAtual = -1;
        }
        
        setDataContextoHabitos(getDataAtualParaLogica()); 
    }

    private boolean tentarCriarNovoUsuario() {
        CreateUserDialog createUserDialog = new CreateUserDialog(this, true, USAR_TEMA_ESCURO);
        createUserDialog.setVisible(true);
        String nomeNovoUsuario = createUserDialog.getNomeUsuarioCriado();
        if (nomeNovoUsuario != null) {
            try {
                Usuario novoUsuarioCriado = habitService.addUsuario(nomeNovoUsuario);
                this.usuarioIdAtual = novoUsuarioCriado.getId();
                JOptionPane.showMessageDialog(this, "Usuário '" + novoUsuarioCriado.getNome() + "' (ID: " + this.usuarioIdAtual + ") criado e carregado com sucesso!", "Usuário Criado", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } catch (ValidationException ve) {
                JOptionPane.showMessageDialog(this, "Erro ao criar usuário: " + ve.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
            } catch (PersistenceException pe) {
                JOptionPane.showMessageDialog(this, "Erro de persistência ao criar usuário: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao criar usuário: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
    }

    private void initComponents() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(null, "Informações do Usuário", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONTE_TITULO_PAINEL_NOVA, getCorTextoTituloPainel()),
                new EmptyBorder(10, 10, 10, 10) 
        ));
        infoPanel.setBackground(getCorPainelInterno());
        infoPanel.setOpaque(true);

        nomeUsuarioLabel = new JLabel("Usuário: Carregando...");
        nomeUsuarioLabel.setFont(FONTE_LABEL_INFO_USUARIO);
        pontosUsuarioLabel = new JLabel("Pontos: Carregando...");
        pontosUsuarioLabel.setFont(FONTE_LABEL_INFO_USUARIO);
        pontosProgressBar = new JProgressBar(0, 100);
        pontosProgressBar.setStringPainted(true);
        pontosProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pontosProgressBar.setPreferredSize(new Dimension(250, 30)); 
        pontosProgressBar.setMinimumSize(new Dimension(150,30));
        pontosProgressBar.setMaximumSize(new Dimension(300,30));


        infoPanel.add(nomeUsuarioLabel);
        infoPanel.add(Box.createHorizontalStrut(20));
        infoPanel.add(pontosUsuarioLabel);
        infoPanel.add(Box.createHorizontalStrut(10));
        infoPanel.add(pontosProgressBar);
        infoPanel.add(Box.createHorizontalStrut(15)); 

        refreshButton = createStyledButton("Recarregar"); 
        refreshButton.addActionListener(e -> {
            if (usuarioIdAtual != -1) {
                System.out.println("Recarregando dados para usuário ID: " + usuarioIdAtual);
                
                setDataContextoHabitos(getDataAtualParaLogica()); 
                
                atualizarDisplayUsuario(); 
                loadObjetivos(); 
                JOptionPane.showMessageDialog(this, "Dados recarregados.", "Informação", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Nenhum usuário logado para recarregar dados.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        });
        infoPanel.add(refreshButton);
        infoPanel.add(Box.createHorizontalStrut(8));

        JButton btnSimularData = createStyledButton("Simular Data"); 
        btnSimularData.addActionListener(e -> {
            String dataStr = JOptionPane.showInputDialog(this, "Digite a data simulada (AAAA-MM-DD):", getDataAtualParaLogica().toString());
            if (dataStr != null) {
                try {
                    LocalDate dataSimuladaInput = LocalDate.parse(dataStr);
                    setDataSimuladaParaTeste(dataSimuladaInput); 
                } catch (java.time.format.DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato de data inválido! Use AAAA-MM-DD.", "Erro de Formato", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        infoPanel.add(btnSimularData);
        infoPanel.add(Box.createHorizontalGlue()); 
        add(infoPanel, BorderLayout.NORTH);


        JPanel painelPrincipalConteudo = new JPanel(new GridLayout(1, 3, 15, 0)); 
        painelPrincipalConteudo.setOpaque(false);
        painelPrincipalConteudo.setBorder(new EmptyBorder(10,0,0,0)); 

        habitPanel = new JPanel(new BorderLayout(5, 10)); 
        habitPanel.setOpaque(USAR_TEMA_ESCURO ? false : true); 
        if(USAR_TEMA_ESCURO) habitPanel.setBackground(getCorPainelInterno()); else habitPanel.setBackground(getCorFundoGeral());

        habitPanelTitledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(USAR_TEMA_ESCURO ? COR_BORDA_ESCURO : Color.GRAY), 
            "Hábitos para " + dataContextoHabitos.format(DateTimeFormatter.ofPattern("dd/MM/yy")),
            TitledBorder.LEADING, 
            TitledBorder.TOP, 
            FONTE_TITULO_PAINEL_NOVA, 
            getCorTextoTituloPainel()
        );
        habitPanel.setBorder(BorderFactory.createCompoundBorder(
            habitPanelTitledBorder,
            new EmptyBorder(8, 8, 8, 8) 
        ));
        
        habitListModel = new DefaultListModel<>();
        habitJList = new JList<>(habitListModel);
        habitJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitJList.setCellRenderer(new HabitListCellRenderer(USAR_TEMA_ESCURO));
        habitJList.setFont(FONTE_LISTA);
        habitJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarEstadoBotoesAcao();
            }
        });
        JScrollPane habitScrollPane = new JScrollPane(habitJList);
        if (USAR_TEMA_ESCURO) {
            habitScrollPane.getViewport().setOpaque(false); 
            habitJList.setOpaque(false); 
            habitScrollPane.setOpaque(false);
            habitScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        } else {
            habitJList.setBackground(Color.WHITE);
            habitScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); 
        }
        habitPanel.add(habitScrollPane, BorderLayout.CENTER);
        
        JPanel painelBotoesHabitos = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5)); 
        painelBotoesHabitos.setOpaque(false);

        addButton = createStyledButton("+");
        addButton.setToolTipText("Adicionar Novo Hábito");
        addButton.addActionListener(e -> abrirDialogoAdicionarHabito());

        editButton = createStyledButton("✎");
        editButton.setToolTipText("Editar Hábito Selecionado");
        editButton.addActionListener(e -> abrirDialogoEditarHabito());

        deleteButton = createStyledButton("-");
        deleteButton.setToolTipText("Excluir Hábito Selecionado");
        deleteButton.addActionListener(e -> excluirHabitoSelecionado());

        markDoneButton = createStyledButton("✔");
        markDoneButton.setToolTipText("Marcar/Desmarcar Hábito");
        markDoneButton.addActionListener(e -> marcarHabitoSelecionadoComoFeito());
        
        painelBotoesHabitos.add(addButton);
        painelBotoesHabitos.add(editButton);
        painelBotoesHabitos.add(deleteButton);
        painelBotoesHabitos.add(markDoneButton);
        habitPanel.add(painelBotoesHabitos, BorderLayout.SOUTH);

        painelPrincipalConteudo.add(habitPanel);


        calendarioPanel = new JPanel(new BorderLayout(5,5));
        calendarioPanel.setOpaque(USAR_TEMA_ESCURO ? false : true);
        if(USAR_TEMA_ESCURO) calendarioPanel.setBackground(getCorPainelInterno()); else calendarioPanel.setBackground(getCorFundoGeral());
        calendarioPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(USAR_TEMA_ESCURO ? COR_BORDA_ESCURO : Color.GRAY), "Navegar Datas", TitledBorder.LEADING, TitledBorder.TOP, FONTE_TITULO_PAINEL_NOVA, getCorTextoTituloPainel()),
            new EmptyBorder(8, 8, 8, 8)
        ));
        
        JPanel topoCalendarioPanel = new JPanel(new BorderLayout());
        topoCalendarioPanel.setOpaque(false);
        JButton btnIrParaHoje = createStyledButton("Ir para Hoje"); 
        btnIrParaHoje.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        btnIrParaHoje.addActionListener(e -> {
            setDataContextoHabitos(LocalDate.now()); 
        });
        topoCalendarioPanel.add(btnIrParaHoje, BorderLayout.EAST); 
        calendarioPanel.add(topoCalendarioPanel, BorderLayout.NORTH);

        calendarioView = new JCalendar();
        calendarioView.setWeekOfYearVisible(false);
        if (USAR_TEMA_ESCURO) {
            calendarioView.setDecorationBackgroundColor(getCorPainelInterno().darker());
        }
        calendarioView.getYearChooser().getSpinner().setFont(FONTE_TEXTO_GERAL);
        calendarioView.getMonthChooser().getComboBox().setFont(FONTE_TEXTO_GERAL);
        
        PropertyChangeListener calendarDateChangeListener = evt -> {
            String propName = evt.getPropertyName();
            if ("day".equals(propName) || 
                ("calendar".equals(propName) && evt.getOldValue() != null && evt.getNewValue() != null)) { 
                
                java.util.Date utilDate = calendarioView.getDate();
                if (utilDate != null) {
                    LocalDate novaDataContexto = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    setDataContextoHabitos(novaDataContexto); 
                }
            }
        };
        calendarioView.getDayChooser().addPropertyChangeListener("day", calendarDateChangeListener);
        calendarioView.addPropertyChangeListener("calendar", calendarDateChangeListener);

        calendarioPanel.add(calendarioView, BorderLayout.CENTER);
        painelPrincipalConteudo.add(calendarioPanel);


        painelObjetivos = new JPanel(new BorderLayout(5, 5));
        painelObjetivos.setOpaque(USAR_TEMA_ESCURO ? false : true);
        if(USAR_TEMA_ESCURO) painelObjetivos.setBackground(getCorPainelInterno()); else painelObjetivos.setBackground(getCorFundoGeral());
        painelObjetivos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(USAR_TEMA_ESCURO ? COR_BORDA_ESCURO : Color.GRAY), "Meus Objetivos", TitledBorder.LEADING, TitledBorder.TOP, FONTE_TITULO_PAINEL_NOVA, getCorTextoTituloPainel()),
            new EmptyBorder(8, 8, 8, 8)
        ));
        objetivosListModel = new DefaultListModel<>();
        objetivosJList = new JList<>(objetivosListModel);
        objetivosJList.setCellRenderer(new ObjetivoListCellRenderer(USAR_TEMA_ESCURO)); 
        
        objetivosJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarEstadoBotoesObjetivo();
            }
        });
        JScrollPane objetivosScrollPane = new JScrollPane(objetivosJList);
         if (USAR_TEMA_ESCURO) { 
            objetivosScrollPane.getViewport().setOpaque(false); 
            objetivosJList.setOpaque(false); 
            objetivosScrollPane.setOpaque(false);
            objetivosScrollPane.setBorder(BorderFactory.createEmptyBorder());
        } else {
            objetivosJList.setBackground(Color.WHITE);
            objetivosScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        painelObjetivos.add(objetivosScrollPane, BorderLayout.CENTER);


        JPanel painelBotoesObjetivo = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5)); 
        painelBotoesObjetivo.setOpaque(false);
        btnAddObjetivo = createStyledButton("+");
        btnAddObjetivo.setToolTipText("Adicionar Novo Objetivo");
        btnAddObjetivo.addActionListener(e -> abrirDialogoAddObjetivo());
        btnEditObjetivo = createStyledButton("✎");
        btnEditObjetivo.setToolTipText("Editar Objetivo Selecionado");
        btnEditObjetivo.addActionListener(e -> abrirDialogoEditObjetivo());
        btnDelObjetivo = createStyledButton("🗑");
        btnDelObjetivo.setToolTipText("Excluir Objetivo Selecionado");
        btnDelObjetivo.addActionListener(e -> excluirObjetivoSelecionado());
        btnMarcarObjetivoConcluido = createStyledButton("✔");
        btnMarcarObjetivoConcluido.setToolTipText("Marcar/Desmarcar Objetivo Como Concluído");
        btnMarcarObjetivoConcluido.addActionListener(e -> toggleConclusaoObjetivoSelecionado());
        
        painelBotoesObjetivo.add(btnAddObjetivo);
        painelBotoesObjetivo.add(btnEditObjetivo);
        painelBotoesObjetivo.add(btnDelObjetivo);
        painelBotoesObjetivo.add(btnMarcarObjetivoConcluido);
        painelObjetivos.add(painelBotoesObjetivo, BorderLayout.SOUTH);
        
        painelPrincipalConteudo.add(painelObjetivos);

        add(painelPrincipalConteudo, BorderLayout.CENTER);
        
        atualizarEstadoBotoesAcao(); 
        atualizarEstadoBotoesObjetivo(); 
    }
    
    private void setDataContextoHabitos(LocalDate novaData) {
        if (this.dataContextoHabitos != null && this.dataContextoHabitos.equals(novaData)) {
             
             
             String tituloEsperado = "Hábitos para " + novaData.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
             if (habitPanelTitledBorder != null && !habitPanelTitledBorder.getTitle().equals(tituloEsperado)) {
                habitPanelTitledBorder.setTitle(tituloEsperado);
                if(habitPanel!=null) habitPanel.repaint();
             }
            return; 
        }
        
        this.dataContextoHabitos = novaData;
        

        if (calendarioView != null) {
            
            java.util.Date currentDateInCalendar = calendarioView.getDate();
            LocalDate localDateInCalendar = null;
            if (currentDateInCalendar != null) {
                localDateInCalendar = currentDateInCalendar.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (!novaData.equals(localDateInCalendar)) {
                 calendarioView.setDate(java.util.Date.from(novaData.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
        }

        if (habitPanelTitledBorder != null && habitPanel != null) {
            habitPanelTitledBorder.setTitle("Hábitos para " + this.dataContextoHabitos.format(DateTimeFormatter.ofPattern("dd/MM/yy")));
            habitPanel.repaint();
        }
        
        
        if (this.usuarioIdAtual != -1) {
            loadHabits(); 
            carregarDadosCalendario();
            
        } else {
            
            if(habitListModel!=null) habitListModel.clear();
            limparCoresCalendario();
            atualizarVisualizacaoCalendario();
        }
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONTE_BOTAO);
        Insets iconPadding = new Insets(8, 10, 8, 10);
        Insets textPadding = new Insets(8, 18, 8, 18);
        Insets currentPadding = text.length() > 2 ? textPadding : iconPadding; 

        if (USAR_TEMA_ESCURO) {
            button.setBackground(getCorAcentoPrimaria());
            button.setForeground(getCorTextoBotao());
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA_ESCURO, 1),
                new EmptyBorder(currentPadding)
            ));
            Color originalBg = button.getBackground(); 
            Color hoverBg = getCorAcentoSecundaria();
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { if (button.isEnabled()) button.setBackground(hoverBg); }
                public void mouseExited(java.awt.event.MouseEvent evt) { if (button.isEnabled()) button.setBackground(originalBg); }
                public void mousePressed(java.awt.event.MouseEvent evt) { if (button.isEnabled()) button.setBackground(hoverBg.darker()); }
                public void mouseReleased(java.awt.event.MouseEvent evt) {
                    if (button.isEnabled()) {
                        if (button.getBounds().contains(evt.getPoint())) button.setBackground(hoverBg);
                        else button.setBackground(originalBg);
                    }
                }
            });
        } else {
            button.setBackground(COR_ACENTO_SECUNDARIO_CLARO);
            button.setForeground(Color.BLACK); 
            button.setBorder(new EmptyBorder(currentPadding));
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
    
    private void atualizarEstadoBotoesObjetivo() {
        boolean isUserLoaded = (this.usuarioIdAtual != -1);
        Objetivo selectedObjetivo = (objetivosJList != null) ? objetivosJList.getSelectedValue() : null;
        boolean isObjetivoSelected = isUserLoaded && selectedObjetivo != null && selectedObjetivo.getId() != 0; 

        if (btnAddObjetivo != null) btnAddObjetivo.setEnabled(isUserLoaded);
        if (btnEditObjetivo != null) btnEditObjetivo.setEnabled(isObjetivoSelected);
        if (btnDelObjetivo != null) btnDelObjetivo.setEnabled(isObjetivoSelected);
        if (btnMarcarObjetivoConcluido != null) btnMarcarObjetivoConcluido.setEnabled(isObjetivoSelected);
    }


    private void loadHabits() {
    if (usuarioIdAtual == -1) {
        if (habitListModel != null) habitListModel.clear();
        if (this.habitosDoUsuarioAtual != null) this.habitosDoUsuarioAtual.clear();
        if (habitPanelTitledBorder != null && habitPanel != null) {
            habitPanelTitledBorder.setTitle("Hábitos");
            habitPanel.repaint();
        }
        atualizarEstadoBotoesAcao();
        return;
    }
    try {
        if (habitListModel == null) habitListModel = new DefaultListModel<>();
        habitListModel.clear();

        
        this.habitosDoUsuarioAtual = habitService.getHabitsByUserId(this.usuarioIdAtual);
        if (this.habitosDoUsuarioAtual == null) this.habitosDoUsuarioAtual = new ArrayList<>();

        
        final DayOfWeek diaDaSemanaContexto = dataContextoHabitos.getDayOfWeek();
        List<Habit> habitosParaExibirNaLista = this.habitosDoUsuarioAtual.stream()
            .filter(h -> {
                if (h.getCreationDate() == null || h.getCreationDate().isAfter(dataContextoHabitos)) {
                    return false; 
                }
                Set<DayOfWeek> diasProgramados = h.getDiasDaSemana();
                
                
                
                
                if (diasProgramados == null || diasProgramados.isEmpty()) {
                    
                    
                    return false; 
                }
                return diasProgramados.contains(diaDaSemanaContexto);
            })
            .collect(Collectors.toList());

        
        if (!habitosParaExibirNaLista.isEmpty()) {
            List<Integer> habitIdsParaStatus = habitosParaExibirNaLista.stream().map(Habit::getId).collect(Collectors.toList());
            Map<Integer, Boolean> statusHabitosNoContexto = habitService.getStatusHabitosPorDia(this.usuarioIdAtual, habitIdsParaStatus, dataContextoHabitos);

            for (Habit habit : habitosParaExibirNaLista) {
                boolean cumpridoNaDataContexto = statusHabitosNoContexto.getOrDefault(habit.getId(), false);
                habit.setCumpridoHoje(cumpridoNaDataContexto);

                try {
                    int sequenciaExibida;
                    if (cumpridoNaDataContexto) {
                        sequenciaExibida = habitService.getSequenciaEfetivaTerminadaEm(this.usuarioIdAtual, habit.getId(), dataContextoHabitos);
                    } else {
                        LocalDate diaAnteriorAoContexto = dataContextoHabitos.minusDays(1);
                        if (habit.getCreationDate().isAfter(diaAnteriorAoContexto)) {
                            sequenciaExibida = 0;
                        } else {
                            sequenciaExibida = habitService.getSequenciaEfetivaTerminadaEm(this.usuarioIdAtual, habit.getId(), diaAnteriorAoContexto);
                        }
                    }
                    habit.setSequenciaAtual(Math.max(0, sequenciaExibida));
                } catch (PersistenceException | UserNotFoundException | HabitNotFoundException e) {
                    System.err.println("Erro ao calcular sequência para o hábito ID " + habit.getId() +
                                       " para data " + dataContextoHabitos + ": " + e.getMessage());
                    habit.setSequenciaAtual(0);
                }
            }

            habitosParaExibirNaLista.sort(
                Comparator.comparing(Habit::isCumpridoHoje)
                    .thenComparing(Habit::getSequenciaAtual, Comparator.reverseOrder())
                    .thenComparing(Habit::getName, String.CASE_INSENSITIVE_ORDER)
            );
            for (Habit habit : habitosParaExibirNaLista) {
                habitListModel.addElement(habit);
            }
        } else if (usuarioIdAtual != -1) { 
            Habit placeholder = new Habit();
            placeholder.setId(0); 
            placeholder.setName("Nenhum hábito agendado para este dia.");
            placeholder.setDescription("");
            habitListModel.addElement(placeholder);
        }
        
        if (habitPanelTitledBorder != null && habitPanel != null) {
            habitPanelTitledBorder.setTitle("Hábitos para " + dataContextoHabitos.format(DateTimeFormatter.ofPattern("dd/MM/yy")));
            habitPanel.repaint();
        }

    } catch (UserNotFoundException | PersistenceException e) {
        JOptionPane.showMessageDialog(this, "Erro ao carregar hábitos: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        if (habitListModel != null) habitListModel.clear();
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Erro inesperado ao carregar hábitos: " + e.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        if (habitListModel != null) habitListModel.clear();
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
            return;
        }
        try {
            Usuario currentUser = habitService.getUsuarioById(this.usuarioIdAtual);
            nomeUsuarioLabel.setText("Usuário: " + currentUser.getNome() + " (ID: " + currentUser.getId() + ")");
            pontosUsuarioLabel.setText("Pontos: " + currentUser.getPontos());
            int pontos = currentUser.getPontos();
            int metaProximoNivel = ((pontos / 100) + 1) * 100;
            if (pontos == 0) metaProximoNivel = 100;
            else if (pontos % 100 == 0 && pontos > 0) metaProximoNivel = pontos + 100; 
            else if (metaProximoNivel <= pontos) metaProximoNivel = ((pontos / 100) + 2) * 100; 

            pontosProgressBar.setMaximum(metaProximoNivel);
            pontosProgressBar.setValue(pontos);
            pontosProgressBar.setString(pontos + " / " + metaProximoNivel + " pts");
        } catch (UserNotFoundException | PersistenceException e) {
            nomeUsuarioLabel.setText("Usuário ID: " + this.usuarioIdAtual + " (Erro ao carregar)");
            pontosUsuarioLabel.setText("Pontos: -");
            if (pontosProgressBar != null) pontosProgressBar.setValue(0);
            JOptionPane.showMessageDialog(this, "Dados do usuário (ID: " + this.usuarioIdAtual + ") não puderam ser carregados: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do usuário: " + e.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void abrirDialogoAdicionarHabito() {
        if (this.usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Nenhum usuário está logado. Faça login ou crie um usuário para adicionar hábitos.", "Usuário Não Logado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        AddHabitDialog dialog = new AddHabitDialog(this, true, this.habitService, this.usuarioIdAtual, USAR_TEMA_ESCURO);
        dialog.setVisible(true);
        if (dialog.isHabitoAdicionadoComSucesso()) {
            JOptionPane.showMessageDialog(this, "Hábito '" + dialog.getNovoHabito().getName() + "' adicionado com sucesso!", "Hábito Adicionado", JOptionPane.INFORMATION_MESSAGE);
            this.habitosDoUsuarioAtual = null; 
            loadHabits(); 
            atualizarDisplayUsuario(); 
            carregarDadosCalendario(); 
        }
    }

    private void abrirDialogoEditarHabito() {
        if (this.usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Faça login para editar hábitos.", "Usuário Não Logado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito para editar.", "Nenhum Hábito Selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        EditHabitDialog dialog = new EditHabitDialog(this, true, this.habitService, selectedHabit, USAR_TEMA_ESCURO);
        dialog.setVisible(true);
        if (dialog.isHabitoAtualizadoComSucesso()) {
            JOptionPane.showMessageDialog(this, "Hábito '" + dialog.getHabitoAtualizado().getName() + "' atualizado com sucesso!", "Hábito Atualizado", JOptionPane.INFORMATION_MESSAGE);
            this.habitosDoUsuarioAtual = null; 
            loadHabits();
            atualizarDisplayUsuario();
            carregarDadosCalendario();
        }
    }

    private void excluirHabitoSelecionado() {
        if (this.usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Faça login para excluir hábitos.", "Usuário Não Logado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito para excluir.", "Nenhum Hábito Selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Tem certeza que deseja excluir o hábito '" + selectedHabit.getName() + "'?\nEsta ação não pode ser desfeita e excluirá o histórico de progresso associado a ele.", "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deletado = habitService.deleteHabit(selectedHabit.getId());
                if (deletado) {
                    JOptionPane.showMessageDialog(this, "Hábito '" + selectedHabit.getName() + "' excluído com sucesso.", "Hábito Excluído", JOptionPane.INFORMATION_MESSAGE);
                    this.habitosDoUsuarioAtual = null; 
                    loadHabits();
                    atualizarDisplayUsuario();
                    carregarDadosCalendario();
                } else {
                    JOptionPane.showMessageDialog(this, "Não foi possível excluir o hábito.", "Erro na Exclusão", JOptionPane.ERROR_MESSAGE);
                }
            } catch (HabitNotFoundException hnfe) {
                JOptionPane.showMessageDialog(this, "Erro: Hábito não encontrado para exclusão. " + hnfe.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                this.habitosDoUsuarioAtual = null; loadHabits(); 
            } catch (PersistenceException pe) {
                JOptionPane.showMessageDialog(this, "Erro de persistência ao excluir o hábito: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao excluir o hábito: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void marcarHabitoSelecionadoComoFeito() {
        if (this.usuarioIdAtual == -1) {
            JOptionPane.showMessageDialog(this, "Faça login para marcar hábitos.", "Usuário Não Logado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Habit selectedHabit = habitJList.getSelectedValue();
        if (selectedHabit == null) {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um hábito para marcar como feito.", "Nenhum Hábito Selecionado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        LocalDate dataParaMarcar = this.dataContextoHabitos; 
        
        

        if (dataParaMarcar.isAfter(getDataAtualParaLogica())) { 
             JOptionPane.showMessageDialog(this, "Não é possível marcar um hábito como feito para uma data futura.", "Data Inválida", JOptionPane.WARNING_MESSAGE);
             return;
        }

        try {
            FeedbackMarcacaoDTO feedback = habitService.marcarHabitoComoFeito(this.usuarioIdAtual, selectedHabit.getId(), dataParaMarcar);
            
            StringBuilder feedbackMessage = new StringBuilder(feedback.getMensagem());
            if (feedback.getPontosGanhosNestaMarcacao() > 0) {
                feedbackMessage.append("\nVocê ganhou ").append(feedback.getPontosGanhosNestaMarcacao()).append(" pontos!");
            }
            JOptionPane.showMessageDialog(this, feedbackMessage.toString(), "Hábito Marcado", JOptionPane.INFORMATION_MESSAGE);

            this.habitosDoUsuarioAtual = null; 
            loadHabits(); 
            atualizarDisplayUsuario(); 
            carregarDadosCalendario(); 
            
            if (feedback.getPontosGanhosNestaMarcacao() > 0) {
                 loadObjetivos(); 
            }

        } catch (UserNotFoundException | HabitNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Erro ao marcar hábito: " + e.getMessage(), "Erro de Dados", JOptionPane.ERROR_MESSAGE);
        } catch (PersistenceException e) {
            JOptionPane.showMessageDialog(this, "Erro de persistência ao marcar hábito: " + e.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, "Erro de validação ao marcar hábito: " + e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao marcar o hábito: " + e.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void carregarDadosCalendario() {
        if (usuarioIdAtual == -1 || calendarioView == null || habitService == null) {
            limparCoresCalendario(); 
            if(calendarioView != null) atualizarVisualizacaoCalendario(); 
            return;
        }
        java.util.Calendar cal = calendarioView.getCalendar(); 
        int ano = cal.get(java.util.Calendar.YEAR);
        int mes = cal.get(java.util.Calendar.MONTH) + 1; 

        try {
            
            
            
            if (this.habitosDoUsuarioAtual == null) { 
                 this.habitosDoUsuarioAtual = habitService.getHabitsByUserId(this.usuarioIdAtual);
                 if (this.habitosDoUsuarioAtual == null) this.habitosDoUsuarioAtual = new ArrayList<>();
            }
            
            this.progressoDoMesAtual = habitService.getProgressoDiarioDoMes(usuarioIdAtual, ano, mes);
            if (this.progressoDoMesAtual == null) {
                this.progressoDoMesAtual = new ArrayList<>();
            }
            this.statusDiasCalendario.clear(); 

            Map<LocalDate, List<ProgressoDiario>> progressosPorDataMap = this.progressoDoMesAtual.stream()
                .collect(Collectors.groupingBy(ProgressoDiario::getDataRegistro));
            
            LocalDate primeiroDiaDoMes = LocalDate.of(ano, mes, 1);
            LocalDate ultimoDiaDoMes = primeiroDiaDoMes.withDayOfMonth(primeiroDiaDoMes.lengthOfMonth());
            LocalDate hojeLogicaApp = getDataAtualParaLogica();

            for (LocalDate dataSendoAvalidada = primeiroDiaDoMes; !dataSendoAvalidada.isAfter(ultimoDiaDoMes); dataSendoAvalidada = dataSendoAvalidada.plusDays(1)) {
                final LocalDate diaFinalLoop = dataSendoAvalidada; 
                final DayOfWeek diaDaSemanaAtualLoop = diaFinalLoop.getDayOfWeek();

                
                List<Habit> habitosAgendadosParaEsteDia = this.habitosDoUsuarioAtual.stream()
                    .filter(h -> h.getCreationDate() != null && !h.getCreationDate().isAfter(diaFinalLoop))
                    .filter(h -> {
                        Set<DayOfWeek> diasProgramados = h.getDiasDaSemana();
                        if (diasProgramados == null || diasProgramados.isEmpty()) {
                            return false; 
                        }
                        return diasProgramados.contains(diaDaSemanaAtualLoop);
                    })
                    .collect(Collectors.toList());
                
                if (habitosAgendadosParaEsteDia.isEmpty()){
                    
                    
                    
                    statusDiasCalendario.put(diaFinalLoop, "SEM_HABITOS_AGENDADOS"); 
                    continue; 
                }

                
                List<ProgressoDiario> progressosDoDiaEspecifico = progressosPorDataMap.getOrDefault(diaFinalLoop, Collections.emptyList());
                long totalHabitosAgendadosNoDia = habitosAgendadosParaEsteDia.size();
                
                Set<Integer> idsHabitosAgendados = habitosAgendadosParaEsteDia.stream()
                                                    .map(Habit::getId)
                                                    .collect(Collectors.toSet());

                long cumpridosDentreOsAgendados = progressosDoDiaEspecifico.stream()
                    .filter(pd -> idsHabitosAgendados.contains(pd.getHabitoId()) && pd.isStatusCumprido())
                    .count();

                if (cumpridosDentreOsAgendados == totalHabitosAgendadosNoDia) {
                    statusDiasCalendario.put(diaFinalLoop, "CUMPRIDO_TOTAL");
                } else if (cumpridosDentreOsAgendados > 0) {
                    statusDiasCalendario.put(diaFinalLoop, "CUMPRIDO_PARCIAL");
                } else { 
                    if (diaFinalLoop.isBefore(hojeLogicaApp) || diaFinalLoop.isEqual(hojeLogicaApp)) {
                        statusDiasCalendario.put(diaFinalLoop, "NAO_CUMPRIDO");
                    }
                    
                    
                    
                    
                }
            } 
            atualizarVisualizacaoCalendario();

        } catch (UserNotFoundException | PersistenceException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados do calendário: " + e.getMessage(), "Erro de Calendário", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            limparCoresCalendario(); 
            atualizarVisualizacaoCalendario();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro inesperado ao carregar dados do calendário: " + e.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            limparCoresCalendario();
            atualizarVisualizacaoCalendario();
        }
    }
    
    private void limparCoresCalendario() {
        if (calendarioView == null || calendarioView.getDayChooser() == null || calendarioView.getDayChooser().getDayPanel() == null) return;
        
        JDayChooser dayChooser = calendarioView.getDayChooser();
        JPanel dayPanel = dayChooser.getDayPanel();
        Component[] components = dayPanel.getComponents();
        
        Color emptyDayBg = dayChooser.getBackground(); 
        Color defaultMonthDayBg = USAR_TEMA_ESCURO ? new Color(50,50,50) : new Color(230,230,230);

        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton dayButton = (JButton) comp;
                String dayText = dayButton.getText();

                dayButton.setOpaque(true); 
                dayButton.setContentAreaFilled(true); 
                dayButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2)); 

                if (dayText == null || dayText.isEmpty() || !dayButton.isEnabled()) { 
                    dayButton.setBackground(emptyDayBg);
                } else {
                    dayButton.setBackground(defaultMonthDayBg);
                }
            }
        }
        dayPanel.repaint();
    }

    private void atualizarVisualizacaoCalendario() {
        if (calendarioView == null || calendarioView.getDayChooser() == null || calendarioView.getDayChooser().getDayPanel() == null) return;

        JDayChooser dayChooser = calendarioView.getDayChooser();
        JPanel dayPanel = dayChooser.getDayPanel();
        Component[] components = dayPanel.getComponents();

        
        Color defaultMonthDayBg = USAR_TEMA_ESCURO ? new Color(50,50,50) : new Color(230,230,230);
        Color defaultDayFg = USAR_TEMA_ESCURO ? COR_TEXTO_ESCURO : Color.BLACK;
        Color sundayFg = USAR_TEMA_ESCURO ? new Color(255, 255, 255) : new Color(0, 0, 0); 
        Color todayBorderColor = getCorAcentoPrimaria(); 
        Color emptyDayBg = dayChooser.getBackground(); 

        
        Color corCumpridoTotal = USAR_TEMA_ESCURO ? new Color(40, 110, 50) : new Color(180, 255, 180);
        Color corCumpridoParcial = USAR_TEMA_ESCURO ? new Color(110, 90, 30) : new Color(255, 230, 170);
        Color corNaoCumprido = USAR_TEMA_ESCURO ? new Color(110, 50, 50) : new Color(255, 190, 190);
        Color corSemHabitosAgendados = USAR_TEMA_ESCURO ? new Color(55,55,65) : new Color(225,225,235); 


        LocalDate todayLogic = getDataAtualParaLogica();
        int currentDisplayMonth = calendarioView.getCalendar().get(java.util.Calendar.MONTH); 
        int currentDisplayYear = calendarioView.getCalendar().get(java.util.Calendar.YEAR);

        for (Component comp : components) {
            if (comp instanceof JButton) {
                JButton dayButton = (JButton) comp;
                dayButton.setOpaque(true);
                dayButton.setContentAreaFilled(true);
                dayButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2)); 

                String dayText = dayButton.getText();
                if (dayText == null || dayText.isEmpty() || !dayButton.isEnabled()) {
                    dayButton.setBackground(emptyDayBg); 
                    dayButton.setForeground(dayChooser.getForeground()); 
                    continue;
                }
                try {
                    int dayOfMonth = Integer.parseInt(dayText);
                    LocalDate buttonDate = LocalDate.of(currentDisplayYear, currentDisplayMonth + 1, dayOfMonth);
                    
                    
                    dayButton.setBackground(defaultMonthDayBg); 
                    dayButton.setForeground(defaultDayFg); 

                    String status = statusDiasCalendario.get(buttonDate);
                    if (status != null) {
                        switch (status) {
                            case "CUMPRIDO_TOTAL": dayButton.setBackground(corCumpridoTotal); break;
                            case "CUMPRIDO_PARCIAL": dayButton.setBackground(corCumpridoParcial); break;
                            case "NAO_CUMPRIDO": dayButton.setBackground(corNaoCumprido); break;
                            case "SEM_HABITOS_AGENDADOS": 
                                dayButton.setBackground(corSemHabitosAgendados); 
                                break;
                            default:
                                dayButton.setBackground(defaultMonthDayBg); 
                                break;
                        }
                    }
                    
                    
                    if (buttonDate.equals(todayLogic)) { 
                        dayButton.setBorder(new LineBorder(todayBorderColor, 2)); 
                        
                        if (status == null || status.equals("SEM_HABITOS_AGENDADOS")) {
                           if(dayButton.getBackground().equals(defaultMonthDayBg) || dayButton.getBackground().equals(corSemHabitosAgendados) ) {
                               dayButton.setBackground(USAR_TEMA_ESCURO ? new Color(65,65,95) : new Color(200,200,235)); 
                           }
                        }
                    }

                    
                    java.util.Calendar tempCal = java.util.Calendar.getInstance();
                    tempCal.set(currentDisplayYear, currentDisplayMonth, dayOfMonth);
                    if(tempCal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY){
                        dayButton.setForeground(sundayFg);
                    }
                } catch (NumberFormatException | java.time.DateTimeException ex) {
                     dayButton.setBackground(emptyDayBg);
                     dayButton.setForeground(dayChooser.getForeground());
                }
            }
        }
        dayPanel.revalidate();
        dayPanel.repaint();
    }
    

private void loadObjetivos() {
    if (usuarioIdAtual == -1 || objetivosListModel == null || objetivoService == null) { 
        if (objetivosListModel != null) objetivosListModel.clear();
        atualizarEstadoBotoesObjetivo();
        return;
    }
    objetivosListModel.clear();
    try {
        
        List<Objetivo> objetivosDoBanco = objetivoService.getObjetivosDoUsuario(this.usuarioIdAtual);

        if (objetivosDoBanco != null && !objetivosDoBanco.isEmpty()) {
            objetivosDoBanco.sort(
                Comparator.comparing(Objetivo::isConcluido)
                          .thenComparing(Objetivo::getDataMeta, Comparator.nullsLast(Comparator.naturalOrder())) 
                          .thenComparing(Objetivo::getNome, String.CASE_INSENSITIVE_ORDER)
            );
            objetivosDoBanco.forEach(objetivosListModel::addElement);
        } else if (usuarioIdAtual != -1) { 
            Objetivo placeholder = new Objetivo(0, "Nenhum objetivo cadastrado.", "");
            
            
            
            
            
            objetivosListModel.addElement(placeholder);
        }

    } catch (PersistenceException e) { 
        JOptionPane.showMessageDialog(this, "Erro de persistência ao carregar objetivos: " + e.getMessage(), "Erro Objetivos", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        Objetivo erroPlaceholder = new Objetivo(0, "Erro ao carregar objetivos.", "Verifique o console.");
        
        objetivosListModel.addElement(erroPlaceholder);
    } catch (Exception e) { 
        JOptionPane.showMessageDialog(this, "Erro inesperado ao carregar objetivos: " + e.getMessage(), "Erro Objetivos", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
        Objetivo erroPlaceholder = new Objetivo(0, "Erro inesperado.", "Verifique o console.");
        
        objetivosListModel.addElement(erroPlaceholder);
    }
    atualizarEstadoBotoesObjetivo();
}

    
private void abrirDialogoAddObjetivo() {
    if (usuarioIdAtual == -1 || objetivoService == null) {
         JOptionPane.showMessageDialog(this, "Faça login para gerenciar objetivos ou serviço não inicializado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    AddObjetivoDialog dialog = new AddObjetivoDialog(this, true, objetivoService, usuarioIdAtual, USAR_TEMA_ESCURO);
    dialog.setVisible(true);

    if (dialog.isObjetivoAdicionadoComSucesso()) {
        JOptionPane.showMessageDialog(this, "Objetivo '" + dialog.getNovoObjetivoAdicionado().getNome() + "' adicionado com sucesso!", "Objetivo Adicionado", JOptionPane.INFORMATION_MESSAGE);
        loadObjetivos(); 
    }
}

    
private void abrirDialogoEditObjetivo() {
    Objetivo objSelecionado = objetivosJList.getSelectedValue();
    if (usuarioIdAtual == -1 || objetivoService == null) {
        JOptionPane.showMessageDialog(this, "Faça login para gerenciar objetivos ou serviço não inicializado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (objSelecionado == null || objSelecionado.getId() == 0) { 
         JOptionPane.showMessageDialog(this, "Selecione um objetivo válido para editar.", "Nenhum Objetivo Selecionado", JOptionPane.WARNING_MESSAGE);
         return;
    }

    EditObjetivoDialog dialog = new EditObjetivoDialog(this, true, objetivoService, objSelecionado, USAR_TEMA_ESCURO);
    dialog.setVisible(true);

    if (dialog.isObjetivoAtualizadoComSucesso()) {
        JOptionPane.showMessageDialog(this, "Objetivo '" + dialog.getObjetivoEditado().getNome() + "' atualizado com sucesso!", "Objetivo Atualizado", JOptionPane.INFORMATION_MESSAGE);
        loadObjetivos(); 
    }
}
    
private void excluirObjetivoSelecionado() {
    Objetivo objSelecionado = objetivosJList.getSelectedValue();
    if (usuarioIdAtual == -1 || objetivoService == null) {
        JOptionPane.showMessageDialog(this, "Faça login para gerenciar objetivos ou serviço não inicializado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (objSelecionado == null || objSelecionado.getId() == 0) { 
         JOptionPane.showMessageDialog(this, "Selecione um objetivo válido para excluir.", "Nenhum Objetivo Selecionado", JOptionPane.WARNING_MESSAGE);
         return;
    }

    int confirm = JOptionPane.showConfirmDialog(this, 
        "Tem certeza que deseja excluir o objetivo '" + objSelecionado.getNome() + "'?\nEsta ação também removerá quaisquer vínculos com hábitos.", 
        "Confirmar Exclusão", 
        JOptionPane.YES_NO_OPTION, 
        JOptionPane.WARNING_MESSAGE);
    
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            boolean deletado = objetivoService.deleteObjetivo(objSelecionado.getId(), this.usuarioIdAtual);
            if (deletado) {
                JOptionPane.showMessageDialog(this, "Objetivo '" + objSelecionado.getNome() + "' excluído com sucesso.", "Objetivo Excluído", JOptionPane.INFORMATION_MESSAGE);
                loadObjetivos(); 
            } else {
                JOptionPane.showMessageDialog(this, "Não foi possível excluir o objetivo. Pode já ter sido removido ou ocorreu um erro.", "Erro na Exclusão", JOptionPane.ERROR_MESSAGE);
            }
        } catch (PersistenceException pe) {
            JOptionPane.showMessageDialog(this, "Erro de persistência ao excluir o objetivo: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
            pe.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado ao excluir o objetivo: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}

    
private void toggleConclusaoObjetivoSelecionado() {
    Objetivo objSelecionado = objetivosJList.getSelectedValue();
     if (usuarioIdAtual == -1 || objetivoService == null) {
        JOptionPane.showMessageDialog(this, "Faça login para gerenciar objetivos ou serviço não inicializado.", "Aviso", JOptionPane.WARNING_MESSAGE);
        return;
    }
    if (objSelecionado == null || objSelecionado.getId() == 0 ) { 
         JOptionPane.showMessageDialog(this, "Selecione um objetivo válido para marcar/desmarcar.", "Nenhum Objetivo Selecionado", JOptionPane.WARNING_MESSAGE);
         return;
    }
    
    try {
        boolean novoStatusConcluido = !objSelecionado.isConcluido(); 
        boolean sucesso = objetivoService.toggleConclusaoObjetivo(objSelecionado.getId(), this.usuarioIdAtual);
        
        if (sucesso) {
            String msg = "Objetivo '" + objSelecionado.getNome() + 
                         (novoStatusConcluido ? "' marcado como concluído!" : "' desmarcado como concluído!");
            JOptionPane.showMessageDialog(this, msg, "Status do Objetivo", JOptionPane.INFORMATION_MESSAGE);
            loadObjetivos(); 
            
            
            
            
            
            
            
            
            
            
            

        } else {
            JOptionPane.showMessageDialog(this, "Não foi possível alterar o status do objetivo.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    } catch (ValidationException ve) {
        JOptionPane.showMessageDialog(this, "Erro de validação: " + ve.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
    } catch (PersistenceException pe) {
        JOptionPane.showMessageDialog(this, "Erro de persistência: " + pe.getMessage(), "Erro de Backend", JOptionPane.ERROR_MESSAGE);
        pe.printStackTrace();
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "Ocorreu um erro inesperado: " + ex.getMessage(), "Erro Inesperado", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}