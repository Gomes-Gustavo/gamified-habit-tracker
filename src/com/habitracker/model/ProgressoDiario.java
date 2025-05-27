package com.habitracker.model;

import java.time.LocalDate;

public class ProgressoDiario {
    private int id;
    private int usuarioId;
    private int habitoId;
    private LocalDate dataRegistro;
    private boolean statusCumprido;

    public ProgressoDiario(int usuarioId, int habitoId, LocalDate dataRegistro, boolean statusCumprido) {
        this.usuarioId = usuarioId;
        this.habitoId = habitoId;
        this.dataRegistro = dataRegistro;
        this.statusCumprido = statusCumprido;
    }

    public ProgressoDiario(int id, int usuarioId, int habitoId, LocalDate dataRegistro, boolean statusCumprido) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.habitoId = habitoId;
        this.dataRegistro = dataRegistro;
        this.statusCumprido = statusCumprido;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public int getHabitoId() {
        return habitoId;
    }

    public void setHabitoId(int habitoId) {
        this.habitoId = habitoId;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public void setDataRegistro(LocalDate dataRegistro) {
        this.dataRegistro = dataRegistro;
    }

    public boolean isStatusCumprido() {
        return statusCumprido;
    }

    public void setStatusCumprido(boolean statusCumprido) {
        this.statusCumprido = statusCumprido;
    }

    @Override
    public String toString() {
        return "ProgressoDiario{" +
               "id=" + id +
               ", usuarioId=" + usuarioId +
               ", habitoId=" + habitoId +
               ", dataRegistro=" + dataRegistro +
               ", statusCumprido=" + statusCumprido +
               '}';
    }
}