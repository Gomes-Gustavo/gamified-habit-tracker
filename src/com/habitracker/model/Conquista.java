package com.habitracker.model;

public class Conquista {
    private int id;
    private String nome;
    private String descricao;
    private String criterioDesbloqueio; // Pode ser melhorado no futuro
    private int pontosBonus;

    public Conquista(String nome, String descricao, String criterioDesbloqueio, int pontosBonus) {
        this.nome = nome;
        this.descricao = descricao;
        this.criterioDesbloqueio = criterioDesbloqueio;
        this.pontosBonus = pontosBonus;
    }

    public Conquista(int id, String nome, String descricao, String criterioDesbloqueio, int pontosBonus) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.criterioDesbloqueio = criterioDesbloqueio;
        this.pontosBonus = pontosBonus;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCriterioDesbloqueio() {
        return criterioDesbloqueio;
    }

    public void setCriterioDesbloqueio(String criterioDesbloqueio) {
        this.criterioDesbloqueio = criterioDesbloqueio;
    }

    public int getPontosBonus() {
        return pontosBonus;
    }

    public void setPontosBonus(int pontosBonus) {
        this.pontosBonus = pontosBonus;
    }

    @Override
    public String toString() {
        return "Conquista{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", descricao='" + descricao + '\'' +
               ", criterioDesbloqueio='" + criterioDesbloqueio + '\'' +
               ", pontosBonus=" + pontosBonus +
               '}';
    }
}