package com.habitracker.ui;

import com.habitracker.model.Conquista;
import javax.swing.*;
import java.awt.*;

public class ConquistaListCellRenderer extends JLabel implements ListCellRenderer<Conquista> {

    private boolean usarTemaEscuro;

    // Cores para o HTML que se adaptam ao tema E À SELEÇÃO
    // Cores para o estado NÃO SELECIONADO
    private final String NOME_CONQUISTA_COR_ESCURO_NORMAL; // Usará getCorTextoTituloPainel() do MainFrame implicitamente
    private final String NOME_CONQUISTA_COR_CLARO_NORMAL = "#D66B00"; // Laranja escuro
    private final String DESCRICAO_CONQUISTA_COR_ESCURO_NORMAL = "#B0B0B0";
    private final String DESCRICAO_CONQUISTA_COR_CLARO_NORMAL = "#505050";
    private final String BONUS_CONQUISTA_COR_ESCURO_NORMAL = "#81C784"; // Verde claro
    private final String BONUS_CONQUISTA_COR_CLARO_NORMAL = "green";

    // Cores para o estado SELECIONADO
    private final String TEXTO_COR_ESCURO_SELECIONADO = "#FFFFFF";
    private final String TEXTO_COR_CLARO_SELECIONADO = "#FFFFFF";
    private final String SUBTEXTO_COR_ESCURO_SELECIONADO = "#E0E0E0";
    private final String SUBTEXTO_COR_CLARO_SELECIONADO = "#F0F0F0";


    public ConquistaListCellRenderer(boolean usarTemaEscuro) {
        this.usarTemaEscuro = usarTemaEscuro;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        // Definindo a cor do nome da conquista no tema escuro (baseado nas cores do MainFrame)
        NOME_CONQUISTA_COR_ESCURO_NORMAL = colorToHex(new Color(255, 152, 0)); // Cor laranja do MainFrame
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Conquista> list,
                                                 Conquista conquista,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {

        String nomeConquistaColor;
        String descricaoColor;
        String bonusColor;

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());

            nomeConquistaColor = usarTemaEscuro ? TEXTO_COR_ESCURO_SELECIONADO : TEXTO_COR_CLARO_SELECIONADO;
            descricaoColor = usarTemaEscuro ? SUBTEXTO_COR_ESCURO_SELECIONADO : SUBTEXTO_COR_CLARO_SELECIONADO;
            bonusColor = usarTemaEscuro ? SUBTEXTO_COR_ESCURO_SELECIONADO : SUBTEXTO_COR_CLARO_SELECIONADO; // Bônus também claro na seleção
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());

            nomeConquistaColor = usarTemaEscuro ? NOME_CONQUISTA_COR_ESCURO_NORMAL : NOME_CONQUISTA_COR_CLARO_NORMAL;
            descricaoColor = usarTemaEscuro ? DESCRICAO_CONQUISTA_COR_ESCURO_NORMAL : DESCRICAO_CONQUISTA_COR_CLARO_NORMAL;
            bonusColor = usarTemaEscuro ? BONUS_CONQUISTA_COR_ESCURO_NORMAL : BONUS_CONQUISTA_COR_CLARO_NORMAL;
        }

        String text = String.format(
                "<html><div style='width: 280px;'>" +
                "<b style='color:%s; font-size: 10pt;'>%s</b><br/>" +
                "<font style='color:%s; font-size: 9pt;'>%s</font><br/>" +
                "<font style='color:%s; font-size: 8pt;'>Bônus: %d pontos</font>" +
                "</div></html>",
                nomeConquistaColor,
                escapeHtml(conquista.getNome()),
                descricaoColor,
                escapeHtml(conquista.getDescricao()),
                bonusColor,
                conquista.getPontosBonus()
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
    
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}