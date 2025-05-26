package com.habitracker.model;

import java.time.LocalDate; // Para usar datas mais modernas

public class Habit {
    private int id;
    private String name;
    private String description;
    private LocalDate creationDate;
    // Adicione outros atributos se necessário (ex: frequência, tipo, etc.)

    // Construtor para criar um novo hábito (o ID pode ser gerado pelo banco)
    public Habit(String name, String description, LocalDate creationDate) {
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
    }

    // Construtor para quando você lê do banco (incluindo o ID)
    public Habit(int id, String name, String description, LocalDate creationDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
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

    // Setters (o setId pode não ser necessário se o banco gerar o ID)
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

    @Override
    public String toString() { // Útil para debugging
        return "Habit{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", creationDate=" + creationDate +
               '}';
    }
}