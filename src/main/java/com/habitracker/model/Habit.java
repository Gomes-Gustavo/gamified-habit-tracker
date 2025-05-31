package com.habitracker.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

public class Habit {
    private int id;
    private String name;
    private String description;
    private LocalDate creationDate;
    private transient boolean cumpridoHoje; // Status para dataContextoHabitos
    private LocalTime horarioOpcional;
    private int usuarioId;
    private transient int sequenciaAtual;
    private Set<DayOfWeek> diasDaSemana;
    private transient boolean atrasadoENaoCumprido; // <<< ADICIONADO AQUI

    // Construtor para novos hábitos
    public Habit(String name, String description, LocalDate creationDate, int usuarioId, LocalTime horarioOpcional) {
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.usuarioId = usuarioId;
        this.horarioOpcional = horarioOpcional;
        this.sequenciaAtual = 0;
        this.diasDaSemana = new HashSet<>();
        this.atrasadoENaoCumprido = false; // Default
    }

    // Construtor para carregar do banco
    public Habit(int id, String name, String description, LocalDate creationDate, int usuarioId, LocalTime horarioOpcional) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creationDate = creationDate;
        this.usuarioId = usuarioId;
        this.horarioOpcional = horarioOpcional;
        this.sequenciaAtual = 0;
        this.diasDaSemana = new HashSet<>();
        this.atrasadoENaoCumprido = false; // Default
    }
    
    // Construtor padrão
    public Habit() {
        this.sequenciaAtual = 0;
        this.diasDaSemana = new HashSet<>();
        this.atrasadoENaoCumprido = false; // Default
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getCreationDate() { return creationDate; }
    public int getUsuarioId() { return usuarioId; }
    public LocalTime getHorarioOpcional() { return horarioOpcional; }
    public boolean isCumpridoHoje() { return cumpridoHoje; }
    public int getSequenciaAtual() { return sequenciaAtual; }
    public Set<DayOfWeek> getDiasDaSemana() { return diasDaSemana; }
    public boolean isAtrasadoENaoCumprido() { return atrasadoENaoCumprido; } // <<< GETTER ADICIONADO

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public void setHorarioOpcional(LocalTime horarioOpcional) { this.horarioOpcional = horarioOpcional; }
    public void setCumpridoHoje(boolean cumpridoHoje) { this.cumpridoHoje = cumpridoHoje; }
    public void setSequenciaAtual(int sequenciaAtual) { this.sequenciaAtual = sequenciaAtual; }
    public void setDiasDaSemana(Set<DayOfWeek> diasDaSemana) {
        this.diasDaSemana = (diasDaSemana != null) ? new HashSet<>(diasDaSemana) : new HashSet<>();
    }
    public void setAtrasadoENaoCumprido(boolean atrasadoENaoCumprido) { // <<< SETTER ADICIONADO
        this.atrasadoENaoCumprido = atrasadoENaoCumprido;
    }

    @Override
    public String toString() {
        StringJoiner diasJoiner = new StringJoiner(", ");
        if (diasDaSemana != null) {
            for (DayOfWeek dia : diasDaSemana) {
                diasJoiner.add(dia.name().substring(0,3));
            }
        }
        return "Habit{" +
               "id=" + id +
               ", name='" + name + '\'' +
               (atrasadoENaoCumprido ? " [ATRASADO]" : "") + // Para debug
               ", dias=[" + diasJoiner.toString() + "]" +
               '}';
    }
}