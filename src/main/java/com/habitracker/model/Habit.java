package com.habitracker.model;

import java.time.LocalDate; // Para usar datas mais modernas

public class Habit {
    private int id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private int usuarioId; // NOVO CAMPO para associar ao usuário

    // Construtor para criar um novo hábito (o ID pode ser gerado pelo banco)
    // Antigo: public Habit(String name, String description, LocalDate creationDate)
    public Habit(String name, String description, LocalDate creationDate, int usuarioId) {
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.usuarioId = usuarioId; // ATRIBUIR usuarioId
    }

    // Construtor para quando você lê do banco (incluindo o ID)
    // Antigo: public Habit(int id, String name, String description, LocalDate creationDate)
    public Habit(int id, String name, String description, LocalDate creationDate, int usuarioId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.usuarioId = usuarioId; // ATRIBUIR usuarioId
    }
    
    // Construtor padrão pode ser útil para alguns frameworks ou lógicas, opcional
    public Habit() {
    }


    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public int getUsuarioId() { // NOVO GETTER
        return usuarioId;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public void setUsuarioId(int usuarioId) { // NOVO SETTER
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() { // Útil para debugging
        return "Habit{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", creationDate=" + creationDate +
               ", usuarioId=" + usuarioId + // INCLUÍDO NO toString
               '}';
    }
}