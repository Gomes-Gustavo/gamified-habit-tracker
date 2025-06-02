package com.habitracker.ui;

import com.habitracker.model.Objetivo;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate; 
import java.time.format.DateTimeFormatter;

public class ObjetivoListCellRenderer extends JLabel implements ListCellRenderer<Objetivo> {

    private boolean usarTemaEscuro;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    
    private final String TEXTO_COR_ESCURO_NORMAL = "#E0E0E0";
    private final String TEXTO_COR_CLARO_NORMAL = "#1A1A1A";
    private final String DESC_COR_ESCURO_NORMAL = "#A0A0A0";
    private final String DESC_COR_CLARO_NORMAL = "#606060";
    
    
    private final String INFO_COR_ESCURO_NORMAL = "#9E9E9E"; 
    private final String INFO_COR_CLARO_NORMAL = "#757575";

    
    private final String TEXTO_COR_ESCURO_CONCLUIDO = "#A5D6A7";
    private final String TEXTO_COR_CLARO_CONCLUIDO = "#2E7D32";
    private final Color FUNDO_COR_ESCURO_CONCLUIDO = new Color(20, 50, 25);
    private final Color FUNDO_COR_CLARO_CONCLUIDO = new Color(220, 255, 230);
    
    private final String INFO_COR_ESCURO_CONCLUIDO = "#81C784"; 
    private final String INFO_COR_CLARO_CONCLUIDO = "#388E3C";

    
    private final String TEXTO_COR_ESCURO_SELECIONADO = "#FFFFFF";
    private final String TEXTO_COR_CLARO_SELECIONADO = "#000000";

    
    private final String DATA_META_COR_ATRASADO_ESCURO = "#FF7043"; 
    private final String DATA_META_COR_ATRASADO_CLARO = "#D32F2F";  
    private final String DATA_META_COR_PROXIMO_ESCURO = "#FFEE58"; 
    private final String DATA_META_COR_PROXIMO_CLARO = "#F57F17";  


    public ObjetivoListCellRenderer(boolean usarTemaEscuro) {
        this.usarTemaEscuro = usarTemaEscuro;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Objetivo> list,
                                                 Objetivo objetivo,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {

        String nomeHtmlColor;
        String descHtmlColor;
        String infoGeralHtmlColor; 
        String corParaDataMetaEspecifica; 

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_SELECIONADO : TEXTO_COR_CLARO_SELECIONADO;
            descHtmlColor = nomeHtmlColor; 
            infoGeralHtmlColor = nomeHtmlColor;
            
            corParaDataMetaEspecifica = nomeHtmlColor; 
        } else {
            if (objetivo.isConcluido()) {
                setBackground(usarTemaEscuro ? FUNDO_COR_ESCURO_CONCLUIDO : FUNDO_COR_CLARO_CONCLUIDO);
                nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_CONCLUIDO : TEXTO_COR_CLARO_CONCLUIDO;
                descHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_CONCLUIDO : TEXTO_COR_CLARO_CONCLUIDO; 
                infoGeralHtmlColor = usarTemaEscuro ? INFO_COR_ESCURO_CONCLUIDO : INFO_COR_CLARO_CONCLUIDO;
                corParaDataMetaEspecifica = infoGeralHtmlColor; 
            } else {
                setBackground(list.getBackground());
                nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_NORMAL : TEXTO_COR_CLARO_NORMAL;
                descHtmlColor = usarTemaEscuro ? DESC_COR_ESCURO_NORMAL : DESC_COR_CLARO_NORMAL;
                infoGeralHtmlColor = usarTemaEscuro ? INFO_COR_ESCURO_NORMAL : INFO_COR_CLARO_NORMAL;
                
                if (objetivo.getDataMeta() != null) {
                    LocalDate hoje = LocalDate.now();
                    if (objetivo.getDataMeta().isBefore(hoje)) {
                        corParaDataMetaEspecifica = usarTemaEscuro ? DATA_META_COR_ATRASADO_ESCURO : DATA_META_COR_ATRASADO_CLARO;
                    } else if (objetivo.getDataMeta().isBefore(hoje.plusWeeks(1))) { 
                        corParaDataMetaEspecifica = usarTemaEscuro ? DATA_META_COR_PROXIMO_ESCURO : DATA_META_COR_PROXIMO_CLARO;
                    } else {
                        corParaDataMetaEspecifica = infoGeralHtmlColor; 
                    }
                } else {
                    corParaDataMetaEspecifica = infoGeralHtmlColor; 
                }
            }
        }

        String nomeDisplay = escapeHtml(objetivo.getNome());
        String descricaoDisplay = (objetivo.getDescricao() != null && !objetivo.getDescricao().isEmpty())
                                  ? escapeHtml(objetivo.getDescricao())
                                  : ""; 

        if (objetivo.getId() == 0 && (nomeDisplay.startsWith("Nenhum objetivo") || nomeDisplay.startsWith("Erro ao carregar"))) {
            descricaoDisplay = escapeHtml(objetivo.getDescricao()); 
        }


        
        String statusConclusaoDisplay = "";
        if (objetivo.isConcluido()) {
            String checkmark = "<font color='" + nomeHtmlColor + "'><b>✔ </b></font>";
            nomeDisplay = checkmark + nomeDisplay;
            statusConclusaoDisplay = "<b>Concluído";
            if (objetivo.getDataConclusao() != null) {
                statusConclusaoDisplay += " em " + objetivo.getDataConclusao().format(dateFormatter);
            }
            statusConclusaoDisplay += "</b>";
        }

        
        String dataMetaTextoCompleto = "";
        if (objetivo.getDataMeta() != null) {
            String textoMeta = "Meta: " + objetivo.getDataMeta().format(dateFormatter);
            String corMeta = corParaDataMetaEspecifica; 

            if (!objetivo.isConcluido()) { 
                 LocalDate hoje = LocalDate.now();
                 if (objetivo.getDataMeta().isBefore(hoje)) {
                    textoMeta += " (Atrasado!)";
                } else if (objetivo.getDataMeta().isBefore(hoje.plusWeeks(1))) {
                    textoMeta += " (Próximo)";
                }
            }
            dataMetaTextoCompleto = String.format("<font style='color:%s;'>%s</font>", corMeta, textoMeta);
        }
        
        
        StringBuilder html = new StringBuilder("<html><div style='width:100%;'>"); 

        
        html.append(String.format("<span style='color:%s; font-size:14pt; font-weight:bold;'>%s</span>", nomeHtmlColor, nomeDisplay));

        
        if (!descricaoDisplay.isEmpty()) {
            html.append(String.format("<br><font style='color:%s; font-size:11pt;'>%s</font>", descHtmlColor, descricaoDisplay));
        }
        
        
        if (!dataMetaTextoCompleto.isEmpty()) {
            html.append(String.format("<br><font style='font-size:10pt;'>%s</font>", dataMetaTextoCompleto)); 
        }

        
        if (!statusConclusaoDisplay.isEmpty()) {
            html.append(String.format("<br><font style='color:%s; font-size:10pt;'>%s</font>", infoGeralHtmlColor, statusConclusaoDisplay));
        }
        
        html.append("</div></html>");
        setText(html.toString());

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