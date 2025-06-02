package com.habitracker.ui;

import com.habitracker.model.Habit;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class HabitListCellRenderer extends JLabel implements ListCellRenderer<Habit> {

    private boolean usarTemaEscuro;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    
    private final String NOME_H_COR_ESCURO_NORMAL = "#E0E0E0";
    private final String NOME_H_COR_CLARO_NORMAL = "#1A1A1A";
    private final String DESC_H_COR_ESCURO_NORMAL = "#A0A0A0";
    private final String DESC_H_COR_CLARO_NORMAL = "#606060";
    private final String INFO_EXTRA_COR_ESCURO_NORMAL = "#808080";
    private final String INFO_EXTRA_COR_CLARO_NORMAL = "#707070";

    private final String TEXTO_H_COR_ESCURO_SELECIONADO = "#FFFFFF";
    private final String TEXTO_H_COR_CLARO_SELECIONADO = "#003300";
    private final String SUBTEXTO_H_COR_ESCURO_SELECIONADO = "#E0E0E0";
    private final String SUBTEXTO_H_COR_CLARO_SELECIONADO = "#004D00";

    private final Color COR_FUNDO_H_CUMPRIDO_ESCURO = new Color(20, 65, 25);
    private final Color COR_FUNDO_H_CUMPRIDO_CLARO = new Color(220, 255, 220);
    private final String NOME_H_COR_ESCURO_CUMPRIDO = "#C8E6C9";
    private final String NOME_H_COR_CLARO_CUMPRIDO = "#004D00";
    private final String DESC_H_COR_ESCURO_CUMPRIDO = "#A5D6A7";
    private final String DESC_H_COR_CLARO_CUMPRIDO = "#2E7D32";
    private final String INFO_EXTRA_COR_ESCURO_CUMPRIDO = "#81C784";
    private final String INFO_EXTRA_COR_CLARO_CUMPRIDO = "#388E3C";
    
    
    private final Color COR_FUNDO_H_ATRASADO_ESCURO = new Color(100, 30, 30); 
    private final Color COR_FUNDO_H_ATRASADO_CLARO = new Color(255, 205, 210); 
    private final String TEXTO_H_COR_ESCURO_ATRASADO = "#FFCDD2"; 
    private final String TEXTO_H_COR_CLARO_ATRASADO = "#B71C1C";  
    private final String X_MARK_COR_ESCURO_ATRASADO = "#FFEBEE"; 
    private final String X_MARK_COR_CLARO_ATRASADO = "#C62828";  
    
    private final String PLACEHOLDER_TEXT_COLOR_DARK = "#B0B0B0";
    private final String PLACEHOLDER_TEXT_COLOR_LIGHT = "#707070";


    public HabitListCellRenderer(boolean usarTemaEscuro) {
        this.usarTemaEscuro = usarTemaEscuro;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Habit> list,
                                                 Habit habit,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {
        
        if (habit == null) { 
            setText("");
            setBackground(list.getBackground());
            return this;
        }
        
        
        if (habit.getId() == 0 && habit.getName() != null && habit.getName().startsWith("Nenhum hábito")) {
            setBackground(list.getBackground()); 
            String placeholderColor = usarTemaEscuro ? PLACEHOLDER_TEXT_COLOR_DARK : PLACEHOLDER_TEXT_COLOR_LIGHT;
            String placeholderText = escapeHtml(habit.getName());
            String placeholderDesc = (habit.getDescription() != null && !habit.getDescription().isEmpty()) 
                                     ? "<br><font size='-1'>" + escapeHtml(habit.getDescription()) + "</font>" 
                                     : ""; 

            setText(String.format("<html><div style='width:100%%; text-align:center; color:%s;'>%s%s</div></html>",
                    placeholderColor, placeholderText, placeholderDesc));
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                
            }
            setEnabled(list.isEnabled());
            return this;
        }

        
        String nomeHabitoHtmlColor;
        String descricaoHtmlColor;
        String infoExtraHtmlColor;
        String prefixoNome = ""; 

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            
            nomeHabitoHtmlColor = usarTemaEscuro ? TEXTO_H_COR_ESCURO_SELECIONADO : TEXTO_H_COR_CLARO_SELECIONADO;
            descricaoHtmlColor = usarTemaEscuro ? SUBTEXTO_H_COR_ESCURO_SELECIONADO : SUBTEXTO_H_COR_CLARO_SELECIONADO;
            infoExtraHtmlColor = usarTemaEscuro ? SUBTEXTO_H_COR_ESCURO_SELECIONADO : SUBTEXTO_H_COR_CLARO_SELECIONADO;

            
            if (habit.isCumpridoHoje()) {
                 prefixoNome = "<font color='" + nomeHabitoHtmlColor + "'>✔ </font>";
            } else if (habit.isAtrasadoENaoCumprido()) { 
                 prefixoNome = "<font color='" + nomeHabitoHtmlColor + "'>X </font>";
            }

        } else if (habit.isAtrasadoENaoCumprido()) { 
            setBackground(usarTemaEscuro ? COR_FUNDO_H_ATRASADO_ESCURO : COR_FUNDO_H_ATRASADO_CLARO);
            nomeHabitoHtmlColor = usarTemaEscuro ? TEXTO_H_COR_ESCURO_ATRASADO : TEXTO_H_COR_CLARO_ATRASADO;
            descricaoHtmlColor = nomeHabitoHtmlColor; 
            infoExtraHtmlColor = nomeHabitoHtmlColor;
            String xMarkColor = usarTemaEscuro ? X_MARK_COR_ESCURO_ATRASADO : X_MARK_COR_CLARO_ATRASADO;
            prefixoNome = "<font color='" + xMarkColor + "'>X </font>";

        } else if (habit.isCumpridoHoje()) { 
            setBackground(usarTemaEscuro ? COR_FUNDO_H_CUMPRIDO_ESCURO : COR_FUNDO_H_CUMPRIDO_CLARO);
            nomeHabitoHtmlColor = usarTemaEscuro ? NOME_H_COR_ESCURO_CUMPRIDO : NOME_H_COR_CLARO_CUMPRIDO;
            descricaoHtmlColor = usarTemaEscuro ? DESC_H_COR_ESCURO_CUMPRIDO : DESC_H_COR_CLARO_CUMPRIDO;
            infoExtraHtmlColor = usarTemaEscuro ? INFO_EXTRA_COR_ESCURO_CUMPRIDO : INFO_EXTRA_COR_CLARO_CUMPRIDO;
            prefixoNome = "<font color='" + nomeHabitoHtmlColor + "'>✔ </font>"; 
        
        } else { 
            setBackground(list.getBackground());
            nomeHabitoHtmlColor = usarTemaEscuro ? NOME_H_COR_ESCURO_NORMAL : NOME_H_COR_CLARO_NORMAL;
            descricaoHtmlColor = usarTemaEscuro ? DESC_H_COR_ESCURO_NORMAL : DESC_H_COR_CLARO_NORMAL;
            infoExtraHtmlColor = usarTemaEscuro ? INFO_EXTRA_COR_ESCURO_NORMAL : INFO_EXTRA_COR_CLARO_NORMAL;
            
        }

        String nomeHabitoDisplay = prefixoNome + escapeHtml(habit.getName());

        String horarioStr = "";
        if (habit.getHorarioOpcional() != null) {
            horarioStr = habit.getHorarioOpcional().format(timeFormatter);
        }
        String sequenciaStr = "";
        int sequenciaAtual = habit.getSequenciaAtual();
        if (sequenciaAtual > 0) {
            sequenciaStr = sequenciaAtual + "x";
        }
        StringBuilder horarioSequenciaBuilder = new StringBuilder();
        if (!horarioStr.isEmpty()) {
            horarioSequenciaBuilder.append(String.format("<b>%s</b>", horarioStr));
        }
        if (!sequenciaStr.isEmpty()) {
            if (horarioSequenciaBuilder.length() > 0) {
                horarioSequenciaBuilder.append("&nbsp;&nbsp;");
            }
            horarioSequenciaBuilder.append(String.format("<b>%s</b>", sequenciaStr));
        }
        String horarioSequenciaHtml = horarioSequenciaBuilder.toString();

        String descriptionTextHtml = (habit.getDescription() != null && !habit.getDescription().isEmpty())
                                     ? escapeHtml(habit.getDescription())
                                     : "<i>Sem descrição</i>";
        
        
        String creationDateFormatted = "";
        if (habit.getId() != 0 && habit.getCreationDate() != null) { 
             creationDateFormatted = "Criado: " + habit.getCreationDate().format(dateFormatter);
        } else if (habit.getId() != 0) {
            creationDateFormatted = "<i>Data desconhecida</i>";
        }


        String text = String.format(
            "<html><div style='width:100%%;'>" +
            "<b style='color:%s; font-size:14pt;'>%s</b><br/>" +
            (horarioSequenciaHtml.isEmpty() ? "" : "<p align='left' style='margin:0px; padding:0px;'><font style='color:%s; font-size:11pt;'>%s</font></p>") +
            "<font style='color:%s; font-size:10pt;'>%s</font><br/>" +
            (creationDateFormatted.isEmpty() ? "" : "<font style='color:%s; font-size:9pt;'>%s</font>") +
            "</div></html>",
            nomeHabitoHtmlColor, nomeHabitoDisplay,
            infoExtraHtmlColor, horarioSequenciaHtml, 
            descricaoHtmlColor, descriptionTextHtml,  
            infoExtraHtmlColor, creationDateFormatted 
        );
        
        setText(text);
        setEnabled(list.isEnabled());
        return this;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}