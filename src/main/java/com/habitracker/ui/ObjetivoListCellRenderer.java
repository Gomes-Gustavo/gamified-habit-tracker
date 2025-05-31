package com.habitracker.ui;

import com.habitracker.model.Objetivo;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate; // Importar LocalDate
import java.time.format.DateTimeFormatter;

public class ObjetivoListCellRenderer extends JLabel implements ListCellRenderer<Objetivo> {

    private boolean usarTemaEscuro;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

    // --- SUAS DEFINIÇÕES DE CORES (mantenha-as como estão ou ajuste conforme necessário) ---
    private final String TEXTO_COR_ESCURO_NORMAL = "#E0E0E0";
    private final String TEXTO_COR_CLARO_NORMAL = "#1A1A1A";
    private final String DESC_COR_ESCURO_NORMAL = "#A0A0A0";
    private final String DESC_COR_CLARO_NORMAL = "#606060";
    
    // Cor para informações gerais como data meta (não concluída, não em alerta) e status pendente
    private final String INFO_COR_ESCURO_NORMAL = "#9E9E9E"; 
    private final String INFO_COR_CLARO_NORMAL = "#757575";

    // Cores para estado Concluído
    private final String TEXTO_COR_ESCURO_CONCLUIDO = "#A5D6A7";
    private final String TEXTO_COR_CLARO_CONCLUIDO = "#2E7D32";
    private final Color FUNDO_COR_ESCURO_CONCLUIDO = new Color(20, 50, 25);
    private final Color FUNDO_COR_CLARO_CONCLUIDO = new Color(220, 255, 230);
    // Cor para status "Concluído em..."
    private final String INFO_COR_ESCURO_CONCLUIDO = "#81C784"; 
    private final String INFO_COR_CLARO_CONCLUIDO = "#388E3C";

    // Cores para estado Selecionado
    private final String TEXTO_COR_ESCURO_SELECIONADO = "#FFFFFF";
    private final String TEXTO_COR_CLARO_SELECIONADO = "#000000";

    // Cores para alertas da Data Meta
    private final String DATA_META_COR_ATRASADO_ESCURO = "#FF7043"; // Laranja avermelhado escuro
    private final String DATA_META_COR_ATRASADO_CLARO = "#D32F2F";  // Vermelho escuro
    private final String DATA_META_COR_PROXIMO_ESCURO = "#FFEE58"; // Amarelo claro
    private final String DATA_META_COR_PROXIMO_CLARO = "#F57F17";  // Laranja/Amarelo escuro


    public ObjetivoListCellRenderer(boolean usarTemaEscuro) {
        this.usarTemaEscuro = usarTemaEscuro;
        setOpaque(true);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        // setVerticalAlignment(SwingConstants.TOP); // JLabel já é TOP por padrão para múltiplas linhas HTML
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends Objetivo> list,
                                                 Objetivo objetivo,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus) {

        String nomeHtmlColor;
        String descHtmlColor;
        String infoGeralHtmlColor; // Para data meta (normal) e status de conclusão
        String corParaDataMetaEspecifica; // Para alertas de data meta

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_SELECIONADO : TEXTO_COR_CLARO_SELECIONADO;
            descHtmlColor = nomeHtmlColor; 
            infoGeralHtmlColor = nomeHtmlColor;
            // Para alertas de data meta em itens selecionados, podemos usar a cor do texto selecionado ou uma cor de alerta mais suave
            corParaDataMetaEspecifica = nomeHtmlColor; // Ou uma cor específica para alerta em seleção
        } else {
            if (objetivo.isConcluido()) {
                setBackground(usarTemaEscuro ? FUNDO_COR_ESCURO_CONCLUIDO : FUNDO_COR_CLARO_CONCLUIDO);
                nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_CONCLUIDO : TEXTO_COR_CLARO_CONCLUIDO;
                descHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_CONCLUIDO : TEXTO_COR_CLARO_CONCLUIDO; 
                infoGeralHtmlColor = usarTemaEscuro ? INFO_COR_ESCURO_CONCLUIDO : INFO_COR_CLARO_CONCLUIDO;
                corParaDataMetaEspecifica = infoGeralHtmlColor; // Se concluído, a data meta não precisa de alerta
            } else {
                setBackground(list.getBackground());
                nomeHtmlColor = usarTemaEscuro ? TEXTO_COR_ESCURO_NORMAL : TEXTO_COR_CLARO_NORMAL;
                descHtmlColor = usarTemaEscuro ? DESC_COR_ESCURO_NORMAL : DESC_COR_CLARO_NORMAL;
                infoGeralHtmlColor = usarTemaEscuro ? INFO_COR_ESCURO_NORMAL : INFO_COR_CLARO_NORMAL;
                // Define a cor de alerta para data meta (se aplicável)
                if (objetivo.getDataMeta() != null) {
                    LocalDate hoje = LocalDate.now();
                    if (objetivo.getDataMeta().isBefore(hoje)) {
                        corParaDataMetaEspecifica = usarTemaEscuro ? DATA_META_COR_ATRASADO_ESCURO : DATA_META_COR_ATRASADO_CLARO;
                    } else if (objetivo.getDataMeta().isBefore(hoje.plusWeeks(1))) { // Próximo em 1 semana
                        corParaDataMetaEspecifica = usarTemaEscuro ? DATA_META_COR_PROXIMO_ESCURO : DATA_META_COR_PROXIMO_CLARO;
                    } else {
                        corParaDataMetaEspecifica = infoGeralHtmlColor; // Cor normal se não estiver em alerta
                    }
                } else {
                    corParaDataMetaEspecifica = infoGeralHtmlColor; // Cor normal se não houver data meta
                }
            }
        }

        String nomeDisplay = escapeHtml(objetivo.getNome());
        String descricaoDisplay = (objetivo.getDescricao() != null && !objetivo.getDescricao().isEmpty())
                                  ? escapeHtml(objetivo.getDescricao())
                                  : ""; // String vazia se não houver descrição para evitar "Sem descrição" desnecessário

        if (objetivo.getId() == 0 && (nomeDisplay.startsWith("Nenhum objetivo") || nomeDisplay.startsWith("Erro ao carregar"))) {
            descricaoDisplay = escapeHtml(objetivo.getDescricao()); // Para placeholders, mostrar a descrição do placeholder
        }


        // Status de Conclusão e Checkmark
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

        // Data Meta
        String dataMetaTextoCompleto = "";
        if (objetivo.getDataMeta() != null) {
            String textoMeta = "Meta: " + objetivo.getDataMeta().format(dateFormatter);
            String corMeta = corParaDataMetaEspecifica; // Já definida acima com lógica de alerta

            if (!objetivo.isConcluido()) { // Só adiciona sufixo de alerta se não estiver concluído
                 LocalDate hoje = LocalDate.now();
                 if (objetivo.getDataMeta().isBefore(hoje)) {
                    textoMeta += " (Atrasado!)";
                } else if (objetivo.getDataMeta().isBefore(hoje.plusWeeks(1))) {
                    textoMeta += " (Próximo)";
                }
            }
            dataMetaTextoCompleto = String.format("<font style='color:%s;'>%s</font>", corMeta, textoMeta);
        }
        
        // Montar o HTML
        StringBuilder html = new StringBuilder("<html><div style='width:100%;'>"); // Lembre-se do %% para String.format se usar

        // Nome
        html.append(String.format("<span style='color:%s; font-size:14pt; font-weight:bold;'>%s</span>", nomeHtmlColor, nomeDisplay));

        // Descrição
        if (!descricaoDisplay.isEmpty()) {
            html.append(String.format("<br><font style='color:%s; font-size:11pt;'>%s</font>", descHtmlColor, descricaoDisplay));
        }
        
        // Data Meta
        if (!dataMetaTextoCompleto.isEmpty()) {
            html.append(String.format("<br><font style='font-size:10pt;'>%s</font>", dataMetaTextoCompleto)); // A cor já está no dataMetaTextoCompleto
        }

        // Status de Conclusão
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