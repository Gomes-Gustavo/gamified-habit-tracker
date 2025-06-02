package com.habitracker.serviceapi.dto;

import java.util.List; 
import java.util.Collections; 

public class FeedbackMarcacaoDTO {
    private boolean sucesso;
    private String mensagem; 
    private int pontosGanhosNestaMarcacao; 
    private int totalPontosUsuarioAposMarcacao; 
    

    
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem, int pontosGanhosNestaMarcacao, int totalPontosUsuarioAposMarcacao) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = pontosGanhosNestaMarcacao;
        this.totalPontosUsuarioAposMarcacao = totalPontosUsuarioAposMarcacao;
        
    }

    
    
    public FeedbackMarcacaoDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.pontosGanhosNestaMarcacao = 0;
        this.totalPontosUsuarioAposMarcacao = -1; 
        
    }

    
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public int getPontosGanhosNestaMarcacao() { return pontosGanhosNestaMarcacao; }
    public int getTotalPontosUsuarioAposMarcacao() { return totalPontosUsuarioAposMarcacao; }
    

    
}