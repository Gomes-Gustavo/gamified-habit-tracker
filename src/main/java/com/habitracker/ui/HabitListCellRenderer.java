package com.habitracker.ui;

import com.habitracker.model.Habit;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class HabitListCellRenderer extends JLabel implements ListCellRenderer<Habit> {

    private boolean usarTemaEscuro;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cores para o HTML que se adaptam ao tema E À SELEÇÃO
    // Cores para o estado NÃO SELECIONADO
    private final String NOME_HABITO_COR_ESCURO_NORMAL = "#FFFFFF";
    private final String NOME_HABITO_COR_CLARO_NORMAL = "#000000";
    private final String DESCRICAO_COR_ESCURO_NORMAL = "#B0B0B0";
    private final String DESCRICAO_COR_CLARO_NORMAL = "#505050";
    private final String DATA_COR_ESCURO_NORMAL = "#909090";
    private final String DATA_COR_CLARO_NORMAL = "gray";

    // Cores para o estado SELECIONADO (para contraste com o fundo de seleção azul)
    private final String TEXTO_COR_ESCURO_SELECIONADO = "#FFFFFF"; // Branco no fundo azul
    private final String TEXTO_COR_CLARO_SELECIONADO = "#FFFFFF";  // Branco no fundo azul padrão do tema claro
    private final String SUBTEXTO_COR_ESCURO_SELECIONADO = "#E0E0E0"; // Cinza bem claro
    private final String SUBTEXTO_COR_CLARO_SELECIONADO = "#F0F0F0";  // Cinza bem claro


    public HabitListCellRenderer(boolean usarTemaEscuro) {
        this.usarTemaEscuro = usarTemaEscuro;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10)); // Padding ajustado
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Habit> list,
                                                 Habit habit,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {

        String nomeHabitoColor;
        String descricaoColor;
        String dataColor;

        if (isSelected) {
            setBackground(list.getSelectionBackground()); // Cor de fundo da seleção (azul no tema escuro)
            setForeground(list.getSelectionForeground()); // Cor principal do texto (branco no tema escuro)

            // Definir cores HTML para o estado selecionado
            nomeHabitoColor = usarTemaEscuro ? TEXTO_COR_ESCURO_SELECIONADO : TEXTO_COR_CLARO_SELECIONADO;
            descricaoColor = usarTemaEscuro ? SUBTEXTO_COR_ESCURO_SELECIONADO : SUBTEXTO_COR_CLARO_SELECIONADO;
            dataColor = usarTemaEscuro ? SUBTEXTO_COR_ESCURO_SELECIONADO : SUBTEXTO_COR_CLARO_SELECIONADO;

        } else {
            setBackground(list.getBackground()); // Cor de fundo normal
            setForeground(list.getForeground()); // Cor de texto normal

            // Definir cores HTML para o estado não selecionado
            nomeHabitoColor = usarTemaEscuro ? NOME_HABITO_COR_ESCURO_NORMAL : NOME_HABITO_COR_CLARO_NORMAL;
            descricaoColor = usarTemaEscuro ? DESCRICAO_COR_ESCURO_NORMAL : DESCRICAO_COR_CLARO_NORMAL;
            dataColor = usarTemaEscuro ? DATA_COR_ESCURO_NORMAL : DATA_COR_CLARO_NORMAL;
        }

        String descriptionText = (habit.getDescription() != null && !habit.getDescription().isEmpty())
                ? escapeHtml(habit.getDescription())
                : "<i>Sem descrição</i>";

        String creationDateFormatted = habit.getCreationDate() != null
                ? habit.getCreationDate().format(dateFormatter)
                : "<i>Data desconhecida</i>";

        String text = String.format(
                "<html><div style='width: 280px;'>" + // Ajuste a largura se necessário
                "<b style='color:%s; font-size: 10pt;'>%s</b><br/>" + // Aumentei um pouco a fonte do nome
                "<font style='color:%s; font-size: 9pt;'>%s</font><br/>" +
                "<font style='color:%s; font-size: 8pt;'>Criado em: %s</font>" +
                "</div></html>",
                nomeHabitoColor,
                escapeHtml(habit.getName()),
                descricaoColor,
                descriptionText,
                dataColor,
                creationDateFormatted
        );
        setText(text);

        setEnabled(list.isEnabled());
        // setFont(list.getFont()); // O HTML define as fontes agora, mas pode ser base

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