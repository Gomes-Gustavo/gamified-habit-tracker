package com.habitracker.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter; 
import java.util.ArrayList;
import java.util.List;

public class Objetivo {
    private int id;
    private int usuarioId;
    private String nome;
    private String descricao;
    private boolean concluido;
    private LocalDate dataCriacao;
    private LocalDate dataConclusao;
    private LocalDate dataMeta;

    private transient List<Habit> habitosVinculados;

    
    public Objetivo(int id, int usuarioId, String nome, String descricao, boolean concluido, 
                    LocalDate dataCriacao, LocalDate dataConclusao, LocalDate dataMeta) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.descricao = descricao;
        this.concluido = concluido;
        this.dataCriacao = dataCriacao;
        this.dataConclusao = dataConclusao;
        this.dataMeta = dataMeta;
        this.habitosVinculados = new ArrayList<>();
    }

    
    public Objetivo(int usuarioId, String nome, String descricao) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.descricao = descricao;
        this.concluido = false;
        this.dataCriacao = LocalDate.now();
        this.dataMeta = null; 
        this.habitosVinculados = new ArrayList<>();
    }

    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public boolean isConcluido() { return concluido; }
    public void setConcluido(boolean concluido) { this.concluido = concluido; }
    public LocalDate getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDate dataCriacao) { this.dataCriacao = dataCriacao; }
    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }
    public LocalDate getDataMeta() { return dataMeta; }
    public void setDataMeta(LocalDate dataMeta) { this.dataMeta = dataMeta; }
    public List<Habit> getHabitosVinculados() { return habitosVinculados; }
    public void setHabitosVinculados(List<Habit> habitosVinculados) { this.habitosVinculados = habitosVinculados; }

    @Override
    public String toString() {
        
        
        String prefixo = concluido ? "✔ " : "⏳ ";
        String descCurta = (descricao != null && !descricao.isEmpty()) ? 
                           " (" + (descricao.length() > 30 ? descricao.substring(0, 27) + "..." : descricao) + ")" 
                           : "";
        String metaStr = (dataMeta != null) ? 
                         " [Meta: " + dataMeta.format(DateTimeFormatter.ofPattern("dd/MM/yy")) + "]" 
                         : "";
        return prefixo + nome + descCurta + metaStr;
    }
}