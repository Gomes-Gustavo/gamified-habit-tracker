package com.habitracker.serviceapi.dto;

import com.habitracker.model.Conquista;
import java.util.List;
import java.util.Collections; // Para lista vazia

public class FeedbackMarcacaoDTO {
    private boolean sucesso;
    private String mensagem; // Mensagem de sucesso ou erro leve
    private int pontosGanhosNestaMarcacao; // Pontos ganhos especificamente por esta ação de marcar
    private int totalPontosUsuarioAposMarcacao; // Total de pontos do usuário atualizado
    private List<Conquista> novasConquistasDesbloqueadas; // Conquistas desbloqueadas por esta ação

    // Construtor para sucesso
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem, int pontosGanhosNestaMarcacao, int totalPontosUsuarioAposMarcacao, List<Conquista> novasConquistasDesbloqueadas) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = pontosGanhosNestaMarcacao;
        this.totalPontosUsuarioAposMarcacao = totalPontosUsuarioAposMarcacao;
        this.novasConquistasDesbloqueadas = (novasConquistasDesbloqueadas != null) ? novasConquistasDesbloqueadas : Collections.emptyList();
    }

    // Construtor para falha simples (onde ainda se retorna um DTO)
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = 0;
        this.totalPontosUsuarioAposMarcacao = -1; // Ou valor indicativo de não aplicável
        this.novasConquistasDesbloqueadas = Collections.emptyList();
    }

    // Getters
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public int getPontosGanhosNestaMarcacao() { return pontosGanhosNestaMarcacao; }
    public int getTotalPontosUsuarioAposMarcacao() { return totalPontosUsuarioAposMarcacao; }
    public List<Conquista> getNovasConquistasDesbloqueadas() { return novasConquistasDesbloqueadas; }

    // Setters podem ser adicionados se necessário, mas DTOs são frequentemente imutáveis
}