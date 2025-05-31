package com.habitracker.serviceapi.dto;

// import com.habitracker.model.Conquista; // REMOVIDO
import java.util.List; // Ainda pode ser usado por outros DTOs, mas não por este para Conquista
import java.util.Collections; // Mantido para o caso de precisar de uma lista vazia em outro contexto, mas não para conquistas aqui.

public class FeedbackMarcacaoDTO {
    private boolean sucesso;
    private String mensagem; // Mensagem de sucesso ou erro leve
    private int pontosGanhosNestaMarcacao; // Pontos ganhos especificamente por esta ação de marcar
    private int totalPontosUsuarioAposMarcacao; // Total de pontos do usuário atualizado
    // private List<Conquista> novasConquistasDesbloqueadas; // REMOVIDO

    // Construtor para sucesso (sem conquistas)
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem, int pontosGanhosNestaMarcacao, int totalPontosUsuarioAposMarcacao) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = pontosGanhosNestaMarcacao;
        this.totalPontosUsuarioAposMarcacao = totalPontosUsuarioAposMarcacao;
        // this.novasConquistasDesbloqueadas = (novasConquistasDesbloqueadas != null) ? novasConquistasDesbloqueadas : Collections.emptyList(); // REMOVIDO
    }

    // Construtor para falha simples ou quando não há ganho de pontos/mudança relevante além da mensagem
    // (onde ainda se retorna um DTO)
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = 0;
        this.totalPontosUsuarioAposMarcacao = -1; // Ou valor indicativo de não aplicável/não alterado
        // this.novasConquistasDesbloqueadas = Collections.emptyList(); // REMOVIDO
    }

    // Getters
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public int getPontosGanhosNestaMarcacao() { return pontosGanhosNestaMarcacao; }
    public int getTotalPontosUsuarioAposMarcacao() { return totalPontosUsuarioAposMarcacao; }
    // public List<Conquista> getNovasConquistasDesbloqueadas() { return novasConquistasDesbloqueadas; } // REMOVIDO

    // Setters podem ser adicionados se necessário, mas DTOs são frequentemente imutáveis
}