package com.habitracker.model;

public class Usuario {
    private int id;
    private String nome;
    private int pontos;

    public Usuario(String nome) {
        this.nome = nome;
        this.pontos = 0; // Usuários novos começam com 0 pontos
    }

    public Usuario(int id, String nome, int pontos) {
        this.id = id;
        this.nome = nome;
        this.pontos = pontos;
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

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    @Override
    public String toString() {
        return "Usuario{" +
               "id=" + id +
               ", nome='" + nome + '\'' +
               ", pontos=" + pontos +
               '}';
    }
}